package com.example.mobilesecurity1;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class SecurityCheckManager {

    private final Context context;

    public SecurityCheckManager(Context context) {
        this.context = context;
    }

    public boolean isBatteryAboveThreshold(int thresholdPercent) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (int) ((level / (float) scale) * 100);
            return batteryPct >= thresholdPercent;
        }
        return false;
    }

    // בהמשך: methods like isLocationEnabled(), isWifiConnected(), etc.
}