package com.example.testvideoplay;

import android.content.Context;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;

import inuker.com.library.BaseSurfaceView;
import inuker.com.library.Utils;
import inuker.com.library.gles.CopyRender;
import inuker.com.library.gles.EglCore;
import inuker.com.library.gles.WindowSurface;

public class VideoSurfaceView extends BaseSurfaceView {

    private EglCore mEglCore;
    private WindowSurface mWindowSurface;

    private MoviePlayer mMoviePlayer;
    private Uri mVideoUri;

    private CopyRender mCopyRender;
    private int mTexture;

    private int mSurfaceWidth;
    private int mSurfaceHeight;

    public VideoSurfaceView(Context context) {
        super(context);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setVideoFile(Uri uri) {
        mVideoUri = uri;
    }

    @Override
    public void onSurfaceCreated(SurfaceHolder holder) {
        mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);
        mWindowSurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mWindowSurface.makeCurrent();

        mCopyRender = new CopyRender();

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mTexture = textures[0];

        String path = Utils.getVideoPathFromUri(getContext(), mVideoUri);
        mMoviePlayer = new MoviePlayer(new File(path), mEglCore.getEGLContext(), mTexture);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;

        GLES20.glClearColor(1.0f, 0.2f, 0.2f, 1.0f);
        GLES20.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);

        // 这个surface要有效之后才能设置给MoviePlayer，否则会抛异常
        // 这个surfaceChanged是跑在主线程的，所以play要放在一个子线程和做
        mMoviePlayer.play(mMoviePlayListener);
    }

    @Override
    public void onSurfaceDestroyed() {
        mWindowSurface.release();
        mEglCore.makeNothingCurrent();
        mEglCore.release();
    }

    private final MoviePlayer.MoviePlayListener mMoviePlayListener = new MoviePlayer.MoviePlayListener() {

        @Override
        public void onDrawAvailable() {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    Log.v("bush", String.format("onFrameAvailable at %s: %d", Thread.currentThread().getName(), mTexture));
                    mWindowSurface.makeCurrent();

                    mCopyRender.draw(mTexture);
                    mWindowSurface.swapBuffers();
                }
            });
        }
    };
}
