package com.lenovo.carcamerashow;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class SharedHelper {

    private Context mContext;

    public SharedHelper() {
    }

    public SharedHelper(Context mContext) {
        this.mContext = mContext;
    }


    //定义一个保存数据的方法
    public void saveIp(String ip) {
        SharedPreferences sp = mContext.getSharedPreferences("carCameraIp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("ip", ip);
        editor.commit();
    }

    //定义一个读取SP文件的方法
    public String readIp() {
        Map<String, String> data = new HashMap<String, String>();
        SharedPreferences sp = mContext.getSharedPreferences("carCameraIp", Context.MODE_PRIVATE);
        String ip = sp.getString("ip", "");
        return ip;
    }
}