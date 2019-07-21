package com.example.testbitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import inuker.com.library.Utils;
import inuker.com.library.gles.CopyRender;

public class BitmapSurfaceRender implements GLSurfaceView.Renderer {

    private CopyRender mCopyRender;
    private Bitmap mBitmap;
    private int mTexture;

    public BitmapSurfaceRender(Context context) {
        mBitmap = Utils.loadAssetsBitmap(context, "test2.jpg");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCopyRender = new CopyRender();
        mTexture = Utils.loadTexture(mBitmap);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCopyRender.draw(mTexture);
    }
}
