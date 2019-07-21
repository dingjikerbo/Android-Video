package com.example.testvideoplay;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.os.Environment;
import android.util.Log;

import com.example.testvideoplay.utils.MediaUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import inuker.com.library.MediaFormat;
import inuker.com.library.Utils;
import inuker.com.library.gles.GlUtil;

public class MoviePlayer {

    private static final String TAG = "bush";

    private static final boolean VERBOSE = false;

    private File mSourceFile;
    private static final MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private SpeedController mSpeedController;

    private File mImageDirs;
    private EGLContext mSharedContext;
    private CodecOutputSurface mOutputSurface;

    private int mOffscreenTexture;
    private int mFramebuffer;

    public MoviePlayer(File sourceFile, EGLContext sharedContext, int texture) {
        mSourceFile = sourceFile;
        mSharedContext = sharedContext;
        mSpeedController = new SpeedController();
        mOffscreenTexture = texture;
        Log.v(TAG, String.format("create MoviePlayer: %s", mSourceFile.getAbsolutePath()));

        mImageDirs = new File(Environment.getExternalStorageDirectory(), "images");
        if (!mImageDirs.exists() && !mImageDirs.mkdirs()) {
            throw new RuntimeException("image dirs not exist!!");
        }
        try {
            Utils.copyFile(mSourceFile, mImageDirs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    interface MoviePlayListener {
        void onDrawAvailable();
    }

    public void play(MoviePlayListener l) {
        new PlayThread(l).start();
    }

    private class PlayThread extends Thread {

        private MoviePlayListener mMoviePlayListener;

        public PlayThread(MoviePlayListener l) {
            mMoviePlayListener = l;
        }

        @Override
        public void run() {
            MediaExtractor extractor = null;
            MediaCodec decoder = null;

            if (!mSourceFile.canRead()) {
                throw new RuntimeException("Unable to read " + mSourceFile);
            }

            long start = System.currentTimeMillis();

            try {
                extractor = new MediaExtractor();
                extractor.setDataSource(mSourceFile.toString());
                int trackIndex = MediaUtils.selectVideoTrack(extractor);
                if (trackIndex < 0) {
                    throw new RuntimeException("No video track found in " + mSourceFile);
                }
                extractor.selectTrack(trackIndex);

                MediaFormat mediaFormat = Utils.getMediaFormat(extractor, trackIndex);


                String mime = mediaFormat.getMime();
                int width = mediaFormat.getWidth();
                int height = mediaFormat.getHeight();

                // 手机横屏，电源键朝上录像时video的rotation为0，如果顺时针旋转rotation就会依次增加
                int rotation = mediaFormat.getRotation();
                Log.v("bush", String.format("width = %d, height = %d, rotation = %d", width, height, rotation));

                decoder = MediaCodec.createDecoderByType(mime);

                // 这里可以直接传surfaceView的surface，这里传一个离线的pbuffer，处理之后再渲染到surfaceView上
                mOutputSurface = new CodecOutputSurface(mSharedContext, width, height);
                prepareFramebuffer(width, height);

                decoder.configure(mediaFormat.get(), mOutputSurface.getSurface(), null, 0);
                decoder.start();

                doExtract(extractor, trackIndex, decoder, mOutputSurface);
            } catch (IOException e) {
                Log.e(TAG, "", e);
            } finally {
                if (decoder != null) {
                    decoder.stop();
                    decoder.release();
                }

                if (extractor != null) {
                    extractor.release();
                }
            }

            Log.v("bush", String.format("play takes %dms", System.currentTimeMillis() - start));
        }

        private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder, CodecOutputSurface outputSurface) throws IOException {
            final int TIMEOUT_USEC = 10000;
            final int MAX_FRAMES = 10;       // stop extracting after this many
            ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
            long firstInputTimeNsec = -1;
            long frameSaveTime = 0;
            int decodeCount = 0;

            boolean outputDone = false;
            boolean inputDone = false;
            while (!outputDone) {
                // Feed more data to the decoder.
                if (!inputDone) {
                    int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputBufIndex >= 0) {
                        if (firstInputTimeNsec == -1) {
                            firstInputTimeNsec = System.nanoTime();
                        }
                        ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                        // Read the sample data into the ByteBuffer.  This neither respects nor
                        // updates inputBuf's position, limit, etc.
                        int chunkSize = extractor.readSampleData(inputBuf, 0);
                        if (chunkSize < 0) {
                            // End of stream -- send empty frame with EOS flag set.
                            decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                            if (VERBOSE) Log.d(TAG, "sent input EOS");
                        } else {
                            if (extractor.getSampleTrackIndex() != trackIndex) {
                                Log.w(TAG, "WEIRD: got sample from track " +
                                        extractor.getSampleTrackIndex() + ", expected " + trackIndex);
                            }
                            long presentationTimeUs = extractor.getSampleTime();
                            decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                    presentationTimeUs, 0 /*flags*/);
                            extractor.advance();
                        }
                    } else {
                        if (VERBOSE) Log.d(TAG, "input buffer not available");
                    }
                }

                if (!outputDone) {
                    int decoderStatus = decoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                    if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // no output available yet
                        if (VERBOSE) Log.d(TAG, "no output from decoder available");
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        // not important for us, since we're using Surface
                        if (VERBOSE) Log.d(TAG, "decoder output buffers changed");
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        android.media.MediaFormat newFormat = decoder.getOutputFormat();
                        if (VERBOSE) Log.d(TAG, "decoder output format changed: " + newFormat);
                    } else if (decoderStatus < 0) {
                        throw new RuntimeException(
                                "unexpected result from decoder.dequeueOutputBuffer: " +
                                        decoderStatus);
                    } else { // decoderStatus >= 0
                        if (firstInputTimeNsec != 0) {
                            // Log the delay from the first buffer of input to the first buffer
                            // of output.
                            long nowNsec = System.nanoTime();
                            Log.d(TAG, "startup lag " + ((nowNsec-firstInputTimeNsec) / 1000000.0) + " ms");
                            firstInputTimeNsec = 0;
                        }
                        if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                                " (size=" + mBufferInfo.size + ")");
                        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            if (VERBOSE) Log.d(TAG, "output EOS");
                            outputDone = true;
                        }

                        boolean doRender = (mBufferInfo.size != 0);

                        mSpeedController.waiting(mBufferInfo.presentationTimeUs);

                        // 一旦我们调releaseOutputBuffer，这个buffer就会被送往SurfaceTexture被转成纹理
                        // 注意这个纹理在onFrameAvailable回调之后才保证会有效
                        decoder.releaseOutputBuffer(decoderStatus, doRender);

                        if (doRender) {
                            outputSurface.awaitNewImage();

                            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
                            GlUtil.checkGlError("glBindFramebuffer");

                            outputSurface.drawImage(true);

                            // Blit to display.
                            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

                            mMoviePlayListener.onDrawAvailable();

//                            if (decodeCount < MAX_FRAMES) {
//                                File outputFile = new File(mImageDirs,
//                                        String.format("frame-%02d.png", decodeCount));
//                                long startWhen = System.nanoTime();
//                                outputSurface.saveFrame(outputFile.toString());
//                                Log.v("bush", String.format("saveFrame %d: %s", decodeCount, outputFile));
//                                frameSaveTime += System.nanoTime() - startWhen;
//                            }
//                            decodeCount++;
                        }
                    }
                }
            }

//            int numSaved = (MAX_FRAMES < decodeCount) ? MAX_FRAMES : decodeCount;
//            Log.d(TAG, "Saving " + numSaved + " frames took " +
//                    (frameSaveTime / numSaved / 1000) + " us per frame");
        }
    }

    /**
     * Prepares the off-screen framebuffer.
     */
    private void prepareFramebuffer(int width, int height) {
        GlUtil.checkGlError("prepareFramebuffer start");

        int[] values = new int[1];

        // Create a texture object and bind it.  This will be the color buffer.
        GLES20.glGenTextures(1, values, 0);
        GlUtil.checkGlError("glGenTextures");
        mOffscreenTexture = values[0];   // expected > 0
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOffscreenTexture);
        GlUtil.checkGlError("glBindTexture " + mOffscreenTexture);

        // Create texture storage.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        // Set parameters.  We're probably using non-power-of-two dimensions, so
        // some values may not be available for use.
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("glTexParameter");

        // Create framebuffer object and bind it.
        GLES20.glGenFramebuffers(1, values, 0);
        GlUtil.checkGlError("glGenFramebuffers");
        mFramebuffer = values[0];    // expected > 0
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
        GlUtil.checkGlError("glBindFramebuffer " + mFramebuffer);

        // Attach the texture (color buffer) to the framebuffer object.
        GlUtil.checkGlError("glFramebufferRenderbuffer");
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mOffscreenTexture, 0);
        GlUtil.checkGlError("glFramebufferTexture2D");

        // See if GLES is happy with all this.
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }

        // Switch back to the default framebuffer.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GlUtil.checkGlError("prepareFramebuffer done");
    }
}
