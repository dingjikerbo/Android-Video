package com.example.boomerang;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    onButtonClicked();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

//        mFile = new File("/sdcard/output/platform-based-generated.mp4");
        mFile = new File("/sdcard/output/ffmpeg-muxer-generated.mp4");
    }

    private void onButtonClicked() throws IOException {
        extractVideoInfo();
        extractVideoFrames();
    }

    private void extractVideoInfo() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, Uri.fromFile(mFile));
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Log.v("bush", String.format("duration is %sms", duration));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void extractVideoFrames() throws IOException {
        MediaExtractor extractor = null;

        try {
            int width = 720;
            int height = 1280;
            ByteBuffer dstBuf = ByteBuffer.allocateDirect(width * height * 4);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            // set up extractor
            extractor = new MediaExtractor();
            extractor.setDataSource(this, Uri.fromFile(mFile), null);
            TrackInfo trackInfo = getVideoTrack(extractor);
            if (trackInfo == null) {
                throw new IllegalArgumentException("cannot find video track");
            }
            extractor.selectTrack(trackInfo.trackIndex);

            boolean advance;

            // extract frames from original file, and write them to the output file

            extractor.seekTo(1931722, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

            do {
                bufferInfo.offset = 0;
                bufferInfo.size = extractor.readSampleData(dstBuf, 0);

                Log.v("bush", String.format("readSampleData sampleSize=%d, sampleTime=%dms, sampleFlags=%d",
                        bufferInfo.size,  extractor.getSampleTime()/1000, extractor.getSampleFlags()));

                advance = extractor.advance();

                Log.v("bush", String.format("advance=%b", advance));

                if (bufferInfo.size <= 0) {
                    break;
                }
            } while (advance);

            // cleaning up
            extractor.release();
            extractor = null;
        } finally {
            if (extractor != null) {
                extractor.release();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private TrackInfo getVideoTrack(MediaExtractor mediaExtractor) {
        int numTracks = mediaExtractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            String trackMimeType = format.getString(MediaFormat.KEY_MIME);
            if (trackMimeType.startsWith("video/")) {
                return new TrackInfo(trackMimeType, format, i);
            }
        }
        return null;
    }

    private static class TrackInfo {

        public final String mimeType;
        public final MediaFormat mediaFormat;
        public final int trackIndex;

        public TrackInfo(String mimeType, MediaFormat mediaFormat, int trackIndex) {
            this.mimeType = mimeType;
            this.mediaFormat = mediaFormat;
            this.trackIndex = trackIndex;
        }
    }

}
