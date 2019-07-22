package com.example.testbitmap;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import inuker.com.library.BaseActivity;

public class MainActivity extends BaseActivity {

    private GLSurfaceView mGLSurfaceView;

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


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

    @Override
    public String[] getRequestedPermissions() {
        return PERMISSIONS;
    }
}
