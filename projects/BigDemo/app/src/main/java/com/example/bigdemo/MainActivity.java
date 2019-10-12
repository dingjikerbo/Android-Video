package com.example.bigdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.library.BaseActivity;
import com.example.library.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
    }

    void test() {
        File file = new File(getExternalFilesDir(null), "log.txt");
        try {
            FileUtils.writeFile(file, Arrays.asList("one", "two", "three", "four"), true);
        } catch (IOException e) {
            Log.e("bush", "", e);
        }
    }

}
