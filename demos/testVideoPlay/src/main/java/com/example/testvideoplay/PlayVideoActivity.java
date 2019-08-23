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

public class PlayVideoActivity extends BaseActivity implements SurfaceHolder.Callback {

    private MoviePlayer mMoviePlayer;
    private SurfaceView mSurfaceView;

    private Uri mVideoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_movie);

        mVideoUri = getIntent().getParcelableExtra("uri");

        mSurfaceView = findViewById(R.id.surface);
        mSurfaceView.getHolder().addCallback(this);
    }

    @Override
    protected boolean isFullScreenActivity() {
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v("bush", String.format("surfaceCreated"));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v("bush", String.format("surfaceChanged: width = %d, height = %d at %s", width, height, Thread.currentThread().getName()));

        String path = Utils.getVideoPathFromUri(this, mVideoUri);

        // 这个surface要有效之后才能设置给MoviePlayer，否则会抛异常
        // 这个surfaceChanged是跑在主线程的，所以play要放在一个子线程和做
        mMoviePlayer = new MoviePlayer(new File(path), holder.getSurface());
        mMoviePlayer.play();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v("bush", String.format("surfaceDestroyed"));
    }
}
