package com.example.testbitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import inuker.com.library.Utils;
import inuker.com.library.gles.CopyRender;
import inuker.com.library.gles.GlUtil;

public class BitmapSurfaceRender implements GLSurfaceView.Renderer {

    private CopyRender mCopyRender;
    private Bitmap mBitmap;
    private int mTexture;

    private int mFramebuffer;
    private int mWidth, mHeight;

    private ByteBuffer mPixelBuf;

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

        mWidth = width;
        mHeight = height;

        mPixelBuf = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
        mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);

        prepareFramebuffer(width, height);
    }

    private int mCount;

    private String getFileName() {
        return String.format("/sdcard/test/output%d.jpg", mCount++);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        Log.v("bush", String.format("onDrawFrame"));

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);

        try {
            saveFrame(getFileName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        mCopyRender.draw(mTexture);

        try {
            saveFrame(getFileName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareFramebuffer(int width, int height) {
        GlUtil.checkGlError("prepareFramebuffer start");

        int[] values = new int[1];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture);
        GlUtil.checkGlError("glBindTexture " + mTexture);

        // Set parameters.  We're probably using non-power-of-two dimensions, so
        // some values may not be available for use.
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("glTexParameter");

        // Create framebuffer object and bind it.
        GLES20.glGenFramebuffers(1, values, 0);
        GlUtil.checkGlError("glGenFramebuffers");
        mFramebuffer = values[0];    // expected > 0
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
        GlUtil.checkGlError("glBindFramebuffer " + mFramebuffer);

        // Attach the texture (color buffer) to the framebuffer object.
        GlUtil.checkGlError("glFramebufferRenderbuffer");
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mTexture, 0);
        GlUtil.checkGlError("glFramebufferTexture2D");

        // See if GLES is happy with all this.
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }

        // Switch back to the default framebuffer.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GlUtil.checkGlError("prepareFramebuffer done");
    }

    public void saveFrame(String filename) throws IOException {
        Log.v("bush", String.format("saveFrame %s", filename));

        mPixelBuf.rewind();
        GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                mPixelBuf);

        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filename));
            Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mPixelBuf.rewind();
            bmp.copyPixelsFromBuffer(mPixelBuf);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bmp.recycle();
        } finally {
            if (bos != null) bos.close();
        }
    }
}
