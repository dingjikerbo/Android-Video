package com.example.testvideoplay;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.util.Log;
import android.view.Surface;

import com.example.testvideoplay.utils.MediaUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import inuker.com.library.MediaFormat;
import inuker.com.library.Utils;

public class MoviePlayer {

    private static final String TAG = "bush";

    private static final boolean VERBOSE = false;

    private File mSourceFile;
    private Surface mOutputSurface;
    private static final MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private SpeedController mSpeedController;

    public MoviePlayer(File sourceFile, Surface outputSurface) {
        mSourceFile = sourceFile;
        mOutputSurface = outputSurface;
        mSpeedController = new SpeedController();
        Log.v(TAG, String.format("create MoviePlayer: %s", mSourceFile.getAbsolutePath()));
    }

    public void play() {
        new PlayThread().start();
    }

    private class PlayThread extends Thread {
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

                MediaFormat format = Utils.getMediaFormat(extractor, trackIndex);
                String mime = format.getMime();
                int width = format.getWidth();
                int height = format.getHeight();

                // 手机横屏，电源键朝上录像时video的rotation为0，如果顺时针旋转rotation就会依次增加
                int rotation = format.getRotation();

                Log.v("bush", String.format("width = %d, height = %d, rotation = %d", width, height, rotation));

                decoder = MediaCodec.createDecoderByType(mime);
                decoder.configure(format.get(), mOutputSurface, null, 0);
                decoder.start();

                doExtract(extractor, trackIndex, decoder);
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
    }

    private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder) {
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
        long firstInputTimeNsec = -1;

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

                    decoder.releaseOutputBuffer(decoderStatus, doRender);
                }
            }
        }
    }
}
