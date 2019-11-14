package com.example.customdividingviewdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView mTextView;
    DividingView mDividingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.tv_result);
        mDividingView = findViewById(R.id.dv_write_plan);
        mDividingView.setOnResultListener(new DividingView.DividingResultListener() {
            @Override
            public void onResultChange(float result) {
                mTextView.setText(String.valueOf((int)result));
            }
        });
    }
}
