package com.example.testvideoplay;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

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

        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startPlayVideo(mSelectedVideoUri);
            }
        });
    }

    private void startPlayVideo(Uri uri) {
        if (uri != null) {
            Intent intent = new Intent(this, PlayVideoActivity.class);
            Bundle extra = new Bundle();
            extra.putParcelable("uri", uri);
            intent.putExtras(extra);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_video, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
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

    @Override
    public String[] getRequestedPermissions() {
        return PERMISSIONS;
    }
}
