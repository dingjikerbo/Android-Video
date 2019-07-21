package inuker.com.library.gles;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

public class CopyRender {

    private static final String VERTEX_SHADER =
            "attribute vec4 a_Position;\n" +
                    "attribute vec2 a_TextureCoordinates;\n" +
                    "varying vec2 v_TextureCoordinates;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_Position =  a_Position;\n" +
                    "    v_TextureCoordinates = a_TextureCoordinates;\n" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "\n" +
                    "varying vec2 v_TextureCoordinates;\n" +
                    "\n" +
                    "uniform sampler2D s_texture;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(s_texture, v_TextureCoordinates);\n" +
                    "\n" +
                    "//    gl_FragColor = vec4(1.0, g, b, 1.0);\n" +
                    "}";

    private static final float FULL_RECTANGLE_COORDS[] = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f, 1.0f,   // 2 top left
            1.0f, 1.0f,   // 3 top right
    };

    private static final float FULL_RECTANGLE_TEX_COORDS[] = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };

    private static final FloatBuffer FULL_RECTANGLE_BUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);

    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;

    private int mProgram;

    public CopyRender() {
        mProgram = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgram == 0) {
            throw new RuntimeException("failed creating program");
        }

        aPositionLocation = GLES20.glGetAttribLocation(mProgram, "a_Position");
        GlUtil.checkLocation(aPositionLocation, "aPosition");

        aTextureCoordinatesLocation = GLES20.glGetAttribLocation(mProgram, "a_TextureCoordinates");
        GlUtil.checkLocation(aTextureCoordinatesLocation, "a_TextureCoordinates");
    }

    public void draw(int texture) {
        GlUtil.checkGlError("onDrawFrame start");

        // (optional) clear to green so we can see if we're failing to set pixels
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        GlUtil.checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GlUtil.checkGlError("glBindTexture " + texture);

        glEnableVertexAttribArray(aPositionLocation);
        glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 8, FULL_RECTANGLE_BUF);
        GlUtil.checkGlError("aPositionLocation");

        glEnableVertexAttribArray(aTextureCoordinatesLocation);
        glVertexAttribPointer(aTextureCoordinatesLocation, 2, GL_FLOAT, false, 8, FULL_RECTANGLE_TEX_BUF);
        GlUtil.checkGlError("aTextureCoordinatesLocation");

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        GlUtil.checkGlError("glDrawArrays");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }
}

