package com.lenovo.carcamerashow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class TelePActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telepl);

        initView();

    }

    private void initView() {
        findViewById(R.id.call).setOnClickListener(v -> {
            startActivity(new Intent(this,MainActivity.class));
            finish();
        });
    }
}
