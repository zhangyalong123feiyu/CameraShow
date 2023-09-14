package com.lenovo.carcamerashow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

public class SplahActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        EditText edit = findViewById(R.id.edit);
        findViewById(R.id.go).setOnClickListener(v -> {
            String editInfo = edit.getText().toString().trim();
            if (!TextUtils.isEmpty(editInfo)){
                SocketLive.webIp=editInfo;
            }

            startActivity(new Intent(this,TelePActivity.class));
        });
    }

    public void call(View view){
        startActivity(new Intent(this,TelePActivity.class));
    }

}
