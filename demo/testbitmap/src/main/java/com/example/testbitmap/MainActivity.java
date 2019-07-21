package com.example.testbitmap;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import inuker.com.library.BaseActivity;

public class MainActivity extends BaseActivity {

    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurfaceView = new GLSurfaceView(this);
        setContentView(mGLSurfaceView);

        mGLSurfaceView.setEGLContextClientVersion(3);
        mGLSurfaceView.setRenderer(new BitmapSurfaceRender(this));
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected boolean isFullScreenActivity() {
        return true;
    }
}
