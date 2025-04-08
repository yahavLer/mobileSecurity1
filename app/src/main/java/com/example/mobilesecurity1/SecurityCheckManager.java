package com.example.mobilesecurity1;
import android.Manifest;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.BatteryManager;

import androidx.core.app.ActivityCompat;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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

    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null &&
                (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }
    public boolean areHeadphonesConnected() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo device : devices) {
                int type = device.getType();
                if (type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                    return true;
                }
            }
        } else {
            // למכשירים ישנים - פתרון פשוט
            return audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn();
        }

        return false;
    }
    public boolean isConnectedToSpecificWifi(String targetSsid) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) return false;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false; // אין הרשאה
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            String currentSsid = wifiInfo.getSSID();
            if (currentSsid != null) {
                // SSID מגיע לעיתים עם גרשיים – נסיר אותם
                currentSsid = currentSsid.replace("\"", "");
                return currentSsid.equals(targetSsid);
            }
        }
        return false;
    }

}