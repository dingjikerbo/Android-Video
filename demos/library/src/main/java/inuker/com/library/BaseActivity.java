package inuker.com.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Window;
import android.view.WindowManager;

public class BaseActivity extends Activity {

    private static final int REQUEST_PERMISSIONS = 0x9131;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFullScreenActivity()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        requestPermissionsIfNeeded();
    }

    protected boolean isFullScreenActivity() {
        return false;
    }

    private void requestPermissionsIfNeeded() {
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this, getRequestedPermissions(), REQUEST_PERMISSIONS);
        } else {
            onPermissionRequested();
        }
    }

    private boolean checkPermissions() {
        String[] requestedPermissions = getRequestedPermissions();
        if (requestedPermissions != null) {
            for (String permission : requestedPermissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (requestCode != RESULT_OK || !checkPermissions()) {
                    finish();
                } else {
                    onPermissionRequested();
                }
                break;
        }
    }

    public void onPermissionRequested() {

    }

    public String[] getRequestedPermissions() {
        return null;
    }
}
