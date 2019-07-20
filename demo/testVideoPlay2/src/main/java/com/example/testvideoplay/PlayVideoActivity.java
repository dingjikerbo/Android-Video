package com.example.testvideoplay;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

import inuker.com.library.BaseActivity;
import inuker.com.library.Utils;

public class PlayVideoActivity extends BaseActivity {

    private VideoSurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_movie);

        Uri uri = getIntent().getParcelableExtra("uri");

        mSurfaceView = findViewById(R.id.surface);
        mSurfaceView.setVideoFile(uri);
    }

    @Override
    protected boolean isFullScreenActivity() {
        return true;
    }
}
