package com.example.testvideoplay;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
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

        String path = Utils.getVideoPathFromUri(getContext(), mVideoUri);
        mMoviePlayer = new MoviePlayer(new File(path), mEglCore.getEGLContext());
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
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

        private int mTextureId;

        @Override
        public void onOutputSurfaceCreated(final int texture) {
            mTextureId = texture;
        }

        @Override
        public void onDrawAvailable() {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    mCopyRender.draw(mTextureId);
                }
            });
        }
    };
}
