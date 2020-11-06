package com.idankorenisraeli.uniquelogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.TextureView;
import android.widget.Button;
import android.widget.EditText;

import com.scottyab.rootbeer.RootBeer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private EditText keyEditText;
    private TextureView textureView;

    private UserDataDetector userData;
    private String clipboard;
    //current string item that was copied to clipboard


    private interface KEYS {
        int CALL_LOG_PERMISSION = 501;
        int CONTACTS_PERMISSION = 502;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        LockDetector lock = LockDetector.getInstance();
        userData = UserDataDetector.getInstance();

        // Non Rooted
        long nextAlarm = userData.getNextAlarmTime();
        float batteryPercent = userData.getBatteryPercent();
        List<String> installedApps = userData.getInstalledAppsNames();
        String deviceName = userData.getDeviceName();
        int brightness = userData.getScreenBrightness();
        boolean patternLocked = lock.isDevicePatternLocked();

        boolean isBluetoothEnabled = userData.isBluetoothEnabled();

        Log.i("pttt", isBluetoothEnabled + " ");

        requestPermissions();

        RootBeer rootBeer = new RootBeer(this);
        if (rootBeer.isRooted()) {
            //Phone is rooted
        } else
            CommonUtils.getInstance().showToast("non-root - limited app functionality");

    }

    private void findViews() {
        this.loginButton = findViewById(R.id.main_BTN_login);
        this.keyEditText = findViewById(R.id.main_EDT_text);
        this.textureView = findViewById(R.id.main_TXT_camera_preview);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_CALL_LOG,Manifest.permission.READ_CONTACTS},
                1);

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        clipboard = userData.getClipboard();
        // When window is being focused, detecting what is the current clipboard string
    }


    // For root device only
    //region Run Command
    private String runShellCommand(String[] command) throws Exception {
        // Run the command
        Process process = Runtime.getRuntime().exec(command);

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        // Grab the results
        StringBuilder log = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            log.append(line).append("\n"); //reading the output of the command line by line
        }

        return log.toString();
    }
    //endregion


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted
            Log.i("pttt", userData.getLastOutgoingNumber());
        }


        if (grantResults.length > 1
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            userData.getContactList();
        }

    }
}