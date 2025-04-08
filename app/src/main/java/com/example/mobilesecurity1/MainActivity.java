package com.example.mobilesecurity1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

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
                        // כאן נקרא למחלקת הבדיקות
                        if (checkManager.isBatteryAboveThreshold(50)) {
                            markStepAsCompleted(stepNumber);
                        } else {
                            showFailureMessage("יש להטעין את הסוללה ליותר מ-50%");
                        }
                    } else {
                        // כל שאר השלבים בינתיים - דמה
                        markStepAsCompleted(stepNumber);
                    }

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
}
