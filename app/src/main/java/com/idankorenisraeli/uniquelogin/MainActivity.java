package com.idankorenisraeli.uniquelogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private EditText keyEditText;

    private UserDataDetector userData;
    private LockDetector lockData;
    private String clipboard;
    //current string item that was copied to clipboard

    private boolean isAppInstalled;
    private boolean isAppUninstalled;


    private interface REQUIRED_KEYS {
        String CLIPBOARD = "SECRET_KEY";
        int BRIGHTNESS = 255;
        int BATTERY_PERCENT = 100;
        boolean PATTERN_LOCK = true;
        String DEVICE_NAME = "UNIQUE_LOGIN";
        String LAST_OUTGOING_PHONE = "*0000";
        String[] CONTACT = new String[]{"Unique Login","11223344"};
        String APP_TO_UNINSTALL = "bbc.mobile.news.v3.app.BBCNewsApp";
        boolean BLUETOOTH_ENABLED = true;
        int ALARM_HOURS = 13;
        int ALARM_MINUTES = 54;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        lockData = LockDetector.getInstance();
        userData = UserDataDetector.getInstance();

        isAppUninstalled = false; // true after user will minimize, uninstall the app, and restart this activity

        if(userData.getInstalledAppsNames().contains(REQUIRED_KEYS.APP_TO_UNINSTALL)) {
            isAppInstalled = true;
            CommonUtils.getInstance().showToast("You can now uninstall the secret app");
        }
        else {
            isAppInstalled = false;
            CommonUtils.getInstance().showToast("Secret app to uninstall is not installed on device");
        }

        requestPermissions();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isUserDataValid())
                    CommonUtils.getInstance().showToast("Login Successfully!");
                else
                    CommonUtils.getInstance().showToast("Login Failed.");
            }
        });
    }

    private void findViews() {
        this.loginButton = findViewById(R.id.main_BTN_login);
        this.keyEditText = findViewById(R.id.main_EDT_text);
    }

    private boolean isUserDataValid()
    {
        // Non Rooted
        DayTime nextAlarm = userData.getNextAlarmTime();
        float batteryPercent = userData.getBatteryPercent();
        List<String> installedApps = userData.getInstalledAppsNames();
        String deviceName = userData.getDeviceName();
        int brightness = userData.getScreenBrightness();
        boolean patternLocked = lockData.isDevicePatternLocked();
        boolean bluetoothEnabled = userData.isBluetoothEnabled();
        String ip = userData.getLocalIpAddress();


        Log.i("pttt","Alarm " + (nextAlarm.getHours() == REQUIRED_KEYS.ALARM_HOURS && nextAlarm.getMinutes() == REQUIRED_KEYS.ALARM_MINUTES));
        Log.i("pttt", "Battery " + REQUIRED_KEYS.BATTERY_PERCENT);
        Log.i("pttt","Device Name " +deviceName.equals(REQUIRED_KEYS.DEVICE_NAME) );
        Log.i("pttt", "Brightness " + (brightness == REQUIRED_KEYS.BRIGHTNESS));
        Log.i("pttt", "Pattern " + (patternLocked == REQUIRED_KEYS.PATTERN_LOCK));
        Log.i("pttt", "Bluetooth " + (bluetoothEnabled == REQUIRED_KEYS.BLUETOOTH_ENABLED ));
        Log.i("pttt", "Contact " + userData.isContactExist(REQUIRED_KEYS.CONTACT[0],REQUIRED_KEYS.CONTACT[1]) );
        Log.i("pttt", " Clipboard " + clipboard.equals(REQUIRED_KEYS.CLIPBOARD));
        Log.i("pttt", "Outgoing phone " + userData.getLastOutgoingNumber().equals(REQUIRED_KEYS.LAST_OUTGOING_PHONE));
        Log.i("pttt", "AppUninstalled " + isAppUninstalled);
        Log.i("pttt", "IP " + ip.equals(keyEditText.getText().toString()));


        return  nextAlarm.getHours() == REQUIRED_KEYS.ALARM_HOURS &&
                nextAlarm.getMinutes() == REQUIRED_KEYS.ALARM_MINUTES &&
                batteryPercent == REQUIRED_KEYS.BATTERY_PERCENT &&
                deviceName.equals(REQUIRED_KEYS.DEVICE_NAME) &&
                brightness == REQUIRED_KEYS.BRIGHTNESS &&
                patternLocked == REQUIRED_KEYS.PATTERN_LOCK &&
                bluetoothEnabled == REQUIRED_KEYS.BLUETOOTH_ENABLED &&
                userData.isContactExist(REQUIRED_KEYS.CONTACT[0],REQUIRED_KEYS.CONTACT[1]) &&
                userData.getLastOutgoingNumber().equals(REQUIRED_KEYS.LAST_OUTGOING_PHONE) &&
                clipboard.equals(REQUIRED_KEYS.CLIPBOARD) &&
                ip.equals(keyEditText.getText().toString()) &&
                isAppUninstalled;
    }



    private void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS},
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
        Log.i("pttt", permissions.length + " | " + grantResults.length);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted
            Log.i("pttt", userData.getLastOutgoingNumber());
        }


        if (grantResults.length > 1
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            //userData.getContactList();
        }

    }


    @Override
    protected void onRestart() {
        super.onRestart();

        if(isAppInstalled && !userData.getInstalledAppsNames().contains(REQUIRED_KEYS.APP_TO_UNINSTALL)){
            // The app was installed, but after the restart it is not installed anymore.
            // It means that the user minimized the app and uninstalled the secret app
            isAppUninstalled = true;
        }
    }
}