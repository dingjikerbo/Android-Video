package com.example.testextractaudio;

import android.Manifest;
import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import inuker.com.library.BaseActivity;

public class MainActivity extends BaseActivity {

    private static final int TIMEOUT_USEC = 3000000;

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final File file = new File("/sdcard/DCIM/Camera/video_start_time_less_than_end_time.mp4");

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    start(file);
                } catch (IOException e) {
                    Log.e("bush", "", e);
                }
            }
        });
    }

    private void start(File file) throws IOException {
        MediaExtractor audioExtractor = createExtractor(file);
        int audioTrack = getAudioTrackIndex(audioExtractor);
        MediaFormat inputFormat = audioExtractor.getTrackFormat(audioTrack);
        MediaFormat outputFormat;
        MediaCodec decoder = createAudioDecoder(inputFormat);

        ByteBuffer[] inputBuffers = decoder.getInputBuffers();
        ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        boolean audioExtractorDone = false;

        int count = 0;

        while (!audioExtractorDone) {
            int index = decoder.dequeueInputBuffer(0);
            Log.v("bush", String.format("dequeueInputBuffer: index=%d", index));

            if (++count > 10) {
                break;
            }

            if (index >= 0) {
                ByteBuffer buffer = inputBuffers[index];
                int size = audioExtractor.readSampleData(buffer, 0);
                Log.v("bush", String.format("readSampleData: size=%d", size));

                if (size >= 0) {
                    decoder.queueInputBuffer(index, 0, size, audioExtractor.getSampleTime(), audioExtractor.getSampleFlags());
                    Log.v("bush", String.format("queueInputBuffer"));
                    audioExtractorDone = !audioExtractor.advance();

                    if (audioExtractorDone) {
                        Log.v("bush", String.format("extrac done: %b", audioExtractorDone));
                    }
                } else {
                    decoder.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    Log.v("bush", String.format("end of stream"));
                }
            }

            while (true) {
                index = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                Log.v("bush", String.format("dequeueOutputBuffer: index=%d", index));

                if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break;
                }

                if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    outputBuffers = decoder.getOutputBuffers();
                    continue;
                }

                if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    outputFormat = decoder.getOutputFormat();
                    Log.v("bush", String.format("output format changed: %s", outputFormat));
                    continue;
                }

                if (index >= 0) {
                    ByteBuffer buffer = outputBuffers[index];

                    Log.v("bush", String.format("bufferInfo: offset=%d, size=%d, flags=%d, time=%d",
                            bufferInfo.offset, bufferInfo.size, bufferInfo.flags, bufferInfo.presentationTimeUs));

                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break;
                    }

                    decoder.releaseOutputBuffer(index, false);
                    continue;
                }
            }
        }

    }

    private MediaCodec createAudioDecoder(MediaFormat inputFormat) {
        MediaCodec decoder = null;
        try {
            decoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        decoder.configure(inputFormat, null, null, 0);
        decoder.start();
        return decoder;
    }

    private int getAudioTrackIndex(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            if (isAudioFormat(extractor.getTrackFormat(i))) {
                extractor.selectTrack(i);
                return i;
            }
        }
        return -1;
    }

    private boolean isAudioFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("audio/");
    }

    private String getMimeTypeFor(MediaFormat format) {
        return format.getString(MediaFormat.KEY_MIME);
    }

    private MediaExtractor createExtractor(File file) {
        try {
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(file.getAbsolutePath());
            return extractor;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String[] getRequestedPermissions() {
        return PERMISSIONS;
    }
}
