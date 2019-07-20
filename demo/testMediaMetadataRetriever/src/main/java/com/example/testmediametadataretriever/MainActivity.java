package com.example.testmediametadataretriever;

import android.Manifest;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;

import inuker.com.library.BaseActivity;
import inuker.com.library.Utils;

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

        findViewById(R.id.retriver).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSelectedVideoUri != null) {
                    testMediaMetadataRetriever(mSelectedVideoUri);
                } else {
                    File file = new File("/sdcard/audio_stream-1562643512647.mp4");
                    Log.v("bush", String.format("file size = %d", file.length()));
                    testMediaMetadataRetriever(file);
                }
            }
        });
    }

    @Override
    public String[] getRequestedPermissions() {
        return PERMISSIONS;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

    private MediaMetadataRetriever getMetadataRetriever(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        // setDataSource非常耗时，建议不要放在UI线程
        retriever.setDataSource(this, uri);
        return retriever;
    }

    private MediaMetadataRetriever getMetadataRetriever(File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        // setDataSource非常耗时，建议不要放在UI线程
        retriever.setDataSource(file.getAbsolutePath());
        return retriever;
    }

    private void testMediaMetadataRetriever(Uri uri) {
        MediaMetadataRetriever retriever = getMetadataRetriever(uri);
        showMetadata(retriever);
    }

    private void testMediaMetadataRetriever(File file) {
        MediaMetadataRetriever retriever = getMetadataRetriever(file);
        showMetadata(retriever);
    }

    private void showMetadata(MediaMetadataRetriever retriever) {
        long start = System.currentTimeMillis();
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        String location = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION);
        String hasVideoTrack = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
        String hasAudioTrack = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
        Log.v("bush", String.format("extractMetadata takes %dms", System.currentTimeMillis() - start));

        // 注意这里取出来的都是string类型的
        Log.v("bush", String.format("width = %s, height = %s", width, height));
        Log.v("bush", String.format("rotation = %s", rotation));
        Log.v("bush", String.format("duration = %sms", duration));
        Log.v("bush", String.format("bitrate = %s", bitrate));
        Log.v("bush", String.format("location = %s", location));
        Log.v("bush", String.format("hasVideoTrack= %s", hasVideoTrack));
        Log.v("bush", String.format("hasAudioTrack= %s", hasAudioTrack));
    }

}
