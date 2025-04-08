package com.example.mobilesecurity1;
import android.Manifest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 101;
    private int pendingStepRequiringPermission = -1;
    private SensorManager sensorManager;
    private SensorEventListener tiltListener;

    private Button[] stepButtons;
    private SecurityCheckManager checkManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkManager = new SecurityCheckManager(this);
        stepButtons = new Button[] {
                findViewById(R.id.step1),
                findViewById(R.id.step2),
                findViewById(R.id.step3),
                findViewById(R.id.step4),
                findViewById(R.id.step5),
                findViewById(R.id.step6)
        };

        for (int i = 0; i < stepButtons.length; i++) {
            int step = i + 1;
            stepButtons[i].setOnClickListener(v -> showStepDialog(step));
        }
    }

    private void showStepDialog(int stepNumber) {
        String[] dummyTasks = {
                "בדיקת אחוז סוללה",
                "בדיקת מיקום",
                "בדיקת חיבור לאוזניות",
                "בדיקת כיוון טלפון",
                "בדיקת חיבור ל-WiFi",
                "בדיקת תאורה / בהירות"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("שלב " + stepNumber)
                .setMessage("האם לבצע את: " + dummyTasks[stepNumber - 1] + "?")
                .setPositiveButton("בצע", (dialog, which) -> {
                    if (stepNumber == 1) {
                        if (checkManager.isBatteryAboveThreshold(50)) {
                            markStepAsCompleted(stepNumber);
                        } else {
                            showFailureMessage("יש להטעין את הסוללה ליותר מ-50%");
                        }
                        return; // ← מונע המשך לוגיקה
                    }

                    if (stepNumber == 2) {
                        if (!checkManager.hasLocationPermission()) {
                            pendingStepRequiringPermission = stepNumber;
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION_PERMISSION);
                        } else if (!checkManager.isLocationEnabled()) {
                            showFailureMessage("שירותי המיקום אינם פעילים");
                        } else {
                            markStepAsCompleted(stepNumber);
                        }
                        return; // ← מונע המשך לוגיקה
                    }
                    if (stepNumber == 3) {
                        if (checkManager.areHeadphonesConnected()) {
                            markStepAsCompleted(stepNumber);
                        } else {
                            showFailureMessage("יש לחבר אוזניות (חוטיות או בלוטות')");
                        }
                        return;
                    }
                    if (stepNumber == 4) {
                        startTiltDetection(stepNumber); // נתחיל להאזין להטייה
                        return;
                    }

                    // כל שאר השלבים (3–6) – פעולה מדומה
                    markStepAsCompleted(stepNumber);
                })

                .setNegativeButton("ביטול", null)
                .show();
    }


    private void markStepAsCompleted(int stepNumber) {
        Button button = stepButtons[stepNumber - 1];
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
        button.setTextColor(Color.WHITE);
    }
    private void showFailureMessage(String message) {
        new AlertDialog.Builder(this)
                .setTitle("בדיקה נכשלה")
                .setMessage(message)
                .setPositiveButton("אישור", null)
                .show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingStepRequiringPermission == 2 && checkManager.isLocationEnabled()) {
                    markStepAsCompleted(2);
                } else {
                    showFailureMessage("יש להפעיל את שירותי המיקום");
                }
            } else {
                showFailureMessage("לא התקבלה הרשאת מיקום");
            }
            pendingStepRequiringPermission = -1;
        }
    }
    private void startTiltDetection(int stepNumber) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            showFailureMessage("לא נמצא חיישן תאוצה במכשיר");
            return;
        }

        tiltListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0]; // תזוזה בציר צדדים
                float y = event.values[1]; // תזוזה בציר אנכי
                float z = event.values[2]; // תזוזה בציר עומק

                // דוגמה: אם הטלפון מוטה ימינה (x < -5)
                if (x < -5.0f) {
                    sensorManager.unregisterListener(this);
                    markStepAsCompleted(stepNumber);
                    showSuccessMessage("הטלפון מוטה ימינה - הצלחה!");
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        sensorManager.registerListener(tiltListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private void showSuccessMessage(String message) {
        new AlertDialog.Builder(this)
                .setTitle("בדיקה הצליחה")
                .setMessage(message)
                .setPositiveButton("אישור", null)
                .show();
    }


}
