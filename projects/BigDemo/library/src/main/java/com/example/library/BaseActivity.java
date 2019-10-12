package com.example.library;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class BaseActivity extends Activity {

    private static final int REQUEST_PERMISSIONS = 0x9971;

    private List<String> mPermissions = new ArrayList<>();

    private static String[] DEFAULT_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyPermissions();
    }

    private boolean isAllPermissionGranted() {
        for (String permission : mPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void verifyPermissions() {
        mPermissions.clear();
        HashSet<String> permissions = new HashSet<>();
        permissions.addAll(Arrays.asList(DEFAULT_PERMISSIONS));
        permissions.addAll(getExtraPermissions());
        mPermissions.addAll(permissions);

        if (!isAllPermissionGranted()) {
            ActivityCompat.requestPermissions(
                    this,
                    mPermissions.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (resultCode != RESULT_OK || !isAllPermissionGranted()) {
                finish();
                return;
            }
        }
    }

    protected List<String> getExtraPermissions() {
        return Collections.EMPTY_LIST;
    }
}
