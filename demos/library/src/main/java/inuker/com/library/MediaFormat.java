package inuker.com.library;

public class MediaFormat {

    private android.media.MediaFormat mMediaFormat;

    private MediaFormat(android.media.MediaFormat mediaFormat) {
        mMediaFormat = mediaFormat;
    }

    public static MediaFormat create(android.media.MediaFormat mediaFormat) {
        return new MediaFormat(mediaFormat);
    }

    public String getMime() {
        return getString(android.media.MediaFormat.KEY_MIME, "");
    }

    public int getWidth() {
        return getInteger(android.media.MediaFormat.KEY_WIDTH, 0);
    }

    public int getHeight() {
        return getInteger(android.media.MediaFormat.KEY_HEIGHT, 0);
    }

    public int getRotation() {
        return getInteger(android.media.MediaFormat.KEY_ROTATION, 0);
    }

    private String getString(String key, String defaultValue) {
        return mMediaFormat.containsKey(key) ? mMediaFormat.getString(key) : defaultValue;
    }

    private int getInteger(String key, int defaultValue) {
        return mMediaFormat.containsKey(key) ? mMediaFormat.getInteger(key) : defaultValue;
    }

    public android.media.MediaFormat get() {
        return mMediaFormat;
    }
}
