package inuker.com.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaExtractor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {

    public static Intent getSelectVideoIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        return Intent.createChooser(intent, "Select Video");
    }

    public static String getVideoPathFromUri(Context context, Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return null;
    }

    public static File getVideoFileFromUri(Context context, Uri uri) {
        String path = getVideoPathFromUri(context, uri);
        return path != null ? new File(path) : null;
    }

    public static void copyFile(File srcFile, File targetDir) throws IOException {
        if (!srcFile.exists()) {
            throw new RuntimeException("src file not exist");
        }
        if (!srcFile.isFile()) {
            throw new RuntimeException("src file invalid");
        }
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new RuntimeException("target dir not exist");
        }
        byte[] data = new byte[1024];
        FileInputStream inputStream = new FileInputStream(srcFile);
        FileOutputStream outputStream = new FileOutputStream(new File(targetDir, srcFile.getName()));

        int size;
        while ((size = inputStream.read(data)) != -1) {
            outputStream.write(data, 0, size);
        }
        inputStream.close();
        outputStream.close();
    }

    public static MediaExtractor getMediaExtractor(File file) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            FileDescriptor fd = fis.getFD();
            extractor.setDataSource(fd);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {}
        }
        return extractor;
    }

    public static MediaExtractor getMediaExtractor(Context context, Uri uri) throws IOException {
        return getMediaExtractor(Utils.getVideoFileFromUri(context, uri));
    }

    public static MediaFormat getMediaFormat(MediaExtractor mediaExtractor, int trackIndex) {
        return MediaFormat.create(mediaExtractor.getTrackFormat(trackIndex));
    }
}
