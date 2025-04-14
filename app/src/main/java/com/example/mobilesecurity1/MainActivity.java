package com.example.mobilesecurity1;

import android.Manifest;
import android.graphics.drawable.ColorDrawable;
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
import android.media.MediaPlayer;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 101;
    private int pendingStepRequiringPermission = -1;
    private SensorManager sensorManager;
    private SensorEventListener tiltListener;
    private SensorEventListener lightListener;
    private boolean[] stepsCompleted = new boolean[6];
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

        new AlertDialog.Builder(this)
                .setTitle("שלב " + stepNumber)
                .setMessage("האם לבצע את: " + dummyTasks[stepNumber - 1] + "?")
                .setPositiveButton("בצע", (dialog, which) -> handleStepExecution(stepNumber))
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void handleStepExecution(int stepNumber) {
        switch (stepNumber) {
            case 1:
                executeBatteryCheck(stepNumber);
                break;
            case 2:
                executeLocationCheck(stepNumber);
                break;
            case 3:
                executeHeadphonesCheck(stepNumber);
                break;
            case 4:
                executeTiltCheck(stepNumber);
                break;
            case 5:
                executeWifiCheck(stepNumber);
                break;
            case 6:
                executeLightCheck(stepNumber);
                break;
            default:
                markStepAsCompleted(stepNumber);
        }
    }

    private void executeBatteryCheck(int stepNumber) {
        showInstructionsAndRun(stepNumber, () -> {
            if (checkManager.isBatteryAboveThreshold(50)) {
                markStepAsCompleted(stepNumber);
            } else {
                showFailureMessage("יש להטעין את הסוללה ליותר מ-50%");
            }
        });
    }

    private void executeLocationCheck(int stepNumber) {
        showInstructionsAndRun(stepNumber, () -> {
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
        });
    }

    private void executeHeadphonesCheck(int stepNumber) {
        showInstructionsAndRun(stepNumber, () -> {
            if (checkManager.areHeadphonesConnected()) {
                markStepAsCompleted(stepNumber);
            } else {
                showFailureMessage("יש לחבר אוזניות (חוטיות או בלוטות')");
            }
        });
    }

    private void executeTiltCheck(int stepNumber) {
        showInstructionsAndRun(stepNumber, () -> startTiltDetection(stepNumber));
    }

    private void executeWifiCheck(int stepNumber) {
        showInstructionsAndRun(stepNumber, () -> {
            if (!checkManager.hasLocationPermission()) {
                pendingStepRequiringPermission = stepNumber;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
                return;
            }

            if (checkManager.isConnectedToSpecificWifi("Motti")) {
                markStepAsCompleted(stepNumber);
            } else {
                showFailureMessage("יש להתחבר לרשת ה-WiFi בשם MyHomeWiFi");
            }
        });
    }

    private void executeLightCheck(int stepNumber) {
        showInstructionsAndRun(stepNumber, () -> startLightDetection(stepNumber));
    }


    private void markStepAsCompleted(int stepNumber) {
        Button button = stepButtons[stepNumber - 1];
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
        button.setTextColor(Color.WHITE);
        showSuccessAnimation(button);
        playSuccessSound();

        stepsCompleted[stepNumber - 1] = true; // מסמן שהשלב הושלם
        checkIfAllStepsCompleted();
    }

    private void showFailureMessage(String message) {
        new AlertDialog.Builder(this)
                .setTitle("בדיקה נכשלה")
                .setMessage(message)
                .setPositiveButton("אישור", null)
                .show();
    }

    private void showInstructionsAndRun(int stepNumber, Runnable onAcknowledge) {
        String message = "";
        switch (stepNumber) {
            case 1:
                message = "הבדיקה תצליח אם הסוללה שלך טעונה בלפחות 50%."; break;
            case 2:
                message = "הבדיקה תצליח אם הפעלת את שירותי המיקום והענקת הרשאה לאפליקציה."; break;
            case 3:
                message = "חבר אוזניות (חוטיות או בבלוטות'). הבדיקה תזהה אם יש אוזניות מחוברות."; break;
            case 4:
                message = "הטלטל את הטלפון ימינה כך שיהיה מוטה בזווית חדה. המערכת תזהה את ההטיה."; break;
            case 5:
                message = "יש להיות מחובר לרשת Wi-Fi בשם: MyHomeWiFi כדי לעבור את הבדיקה."; break;
            case 6:
                message = "הבדיקה תצליח אם הסביבה חשוכה – למשל אם תכסה את הטלפון עם היד."; break;
        }

        new AlertDialog.Builder(this)
                .setTitle("הוראות לשלב " + stepNumber)
                .setMessage(message)
                .setPositiveButton("הבנתי, המשך", (dialog, which) -> onAcknowledge.run())
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

                // אם הטלפון מוטה ימינה (x < -5)
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

    private void startLightDetection(int stepNumber) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            showFailureMessage("לא נמצא חיישן אור במכשיר");
            return;
        }

        lightListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float lux = event.values[0]; // ערך התאורה

                //  הצלחה זה כשהאור נמוך מ-30
                if (lux < 30.0f) {
                    sensorManager.unregisterListener(this);
                    markStepAsCompleted(stepNumber);
                    showSuccessMessage("זוהתה סביבה חשוכה - הצלחה!");
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void playSuccessSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.success_sound);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mediaPlayer.start();
    }

    private void checkIfAllStepsCompleted() {
        for (boolean stepDone : stepsCompleted) {
            if (!stepDone) {
                return; // יש שלב שלא הושלם
            }
        }

        // כל השלבים הושלמו
        showSuccessEndScreen();
    }

    private void showSuccessEndScreen() {
        new AlertDialog.Builder(this)
                .setTitle("כל הבדיקות הושלמו בהצלחה 🎉")
                .setMessage("ברכות! סיימת את כל שלבי האימות.")
                .setPositiveButton("סיום", (dialog, which) -> finish())
                .show();
    }

    private void showSuccessAnimation(Button targetButton) {
        targetButton.setScaleX(0.8f);
        targetButton.setScaleY(0.8f);
        targetButton.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();
    }
}
