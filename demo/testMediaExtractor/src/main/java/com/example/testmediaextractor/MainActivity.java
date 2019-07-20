package com.example.testmediaextractor;

import android.Manifest;
import android.content.Intent;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import inuker.com.library.BaseActivity;
import inuker.com.library.Utils;

import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_Format16bitARGB4444;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 1;

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Uri mSelectedVideoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.select).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(Utils.getSelectVideoIntent(), REQUEST_TAKE_GALLERY_VIDEO);
            }
        });

        findViewById(R.id.extractor).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                testMediaExtractor(mSelectedVideoUri);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_GALLERY_VIDEO:
                if (resultCode == RESULT_OK) {
                    if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                        mSelectedVideoUri = data.getData();
                    }
                }
                break;
        }
    }

    private MediaExtractor getMediaExtractor(Uri uri) throws IOException {
        MediaExtractor mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(this, uri, null);
        return mediaExtractor;
    }

    /* 这两种方案都行
    private MediaExtractor getMediaExtractor(Uri uri) throws IOException {
        return Utils.getMediaExtractor(this, uri);
    }*/

    private void testMediaExtractor(Uri uri) {
        Log.v("bush", String.format("mediaExtractor: %s", uri));

        MediaExtractor mediaExtractor = null;

        try {
            mediaExtractor = getMediaExtractor(uri);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "failed", Toast.LENGTH_LONG).show();
        }

        if (mediaExtractor == null) {
            return;
        }

        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);

            if (mediaFormat == null) {
                Log.w("bush", String.format("mediaFormat for track %d is null"));
                continue;
            }

            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            String language = mediaFormat.getString(MediaFormat.KEY_LANGUAGE);
            int sampleRate = getInteger(mediaFormat, MediaFormat.KEY_SAMPLE_RATE);
            int channelCount = getInteger(mediaFormat, MediaFormat.KEY_CHANNEL_COUNT);
            int width = getInteger(mediaFormat, MediaFormat.KEY_WIDTH);
            int height = getInteger(mediaFormat, MediaFormat.KEY_HEIGHT);
            int maxWidth = getInteger(mediaFormat, MediaFormat.KEY_MAX_WIDTH);
            int maxHeight = getInteger(mediaFormat, MediaFormat.KEY_MAX_HEIGHT);
            int maxInputSize = getInteger(mediaFormat, MediaFormat.KEY_MAX_INPUT_SIZE);
            int bitRate = getInteger(mediaFormat, MediaFormat.KEY_BIT_RATE);
            int frameRate = getInteger(mediaFormat, MediaFormat.KEY_FRAME_RATE);
            int colorFormat = getInteger(mediaFormat, MediaFormat.KEY_COLOR_FORMAT);

            Log.v("bush", String.format("For track %d", i));
            Log.v("bush", String.format("mime = \"%s\"", mime));
            Log.v("bush", String.format("language = \"%s\"", language));
            Log.v("bush", String.format("sampleRate = %d", sampleRate));
            Log.v("bush", String.format("channelCount = %s", channelCount));
            Log.v("bush", String.format("width = %s", width));
            Log.v("bush", String.format("height = %s", height));
            Log.v("bush", String.format("maxWidth = %s", maxWidth));
            Log.v("bush", String.format("maxHeight = %s", maxHeight));
            Log.v("bush", String.format("maxInputSize = %s", maxInputSize));
            Log.v("bush", String.format("bitRate = %s", bitRate));
            Log.v("bush", String.format("frameRate = %s", frameRate));

            String format = colorFormat + "";

            switch (colorFormat) {
                case COLOR_Format16bitARGB4444:
                    format = "argb";
                    break;
            }
            Log.v("bush", String.format("colorFormat = \"%s\"", format));
        }
    }

    // 注意，这算是个bug，直接getInteger的话如果map里没有就会崩溃，看一下源码
    // 所以这里先判断一下
    private int getInteger(MediaFormat mediaFormat, String key) {
        if (mediaFormat.containsKey(key)) {
            Integer value = mediaFormat.getInteger(key);
            return value != null ? value : -1;
        }
        return -1;
    }

    @Override
    public String[] getRequestedPermissions() {
        return PERMISSIONS;
    }
}
