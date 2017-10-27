package com.tsubasa.example;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tsubasa.bankcardscanner.ScanCamera;
import com.tsubasa.bankcardscanner.ScanCameraKt;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tv_result);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanCamera.class);
                startActivityForResult(intent, 110);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 110 && resultCode == RESULT_OK) {
            String result = data.getStringExtra(ScanCameraKt.EXTRA_SCAN_CARD_RESULT_STR);
            if (TextUtils.isEmpty(result)) {
                result = "结果为空";
            }
            textView.setText(result);
        }
    }
}
