package com.papegames.compliance;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getDeviceId(View view) {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSubscriberId(View view) {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            tm.getSubscriberId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

