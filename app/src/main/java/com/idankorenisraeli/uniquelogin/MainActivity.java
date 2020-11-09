package com.idankorenisraeli.uniquelogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.spec.KeySpec;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private EditText keyEditText;

    private UserDataDetector userData;
    private String clipboard; //current string item that was copied to clipboard
    private float currentLight; // current light detected by sensor

    private boolean isAppInstalled;
    private boolean isAppUninstalled;

    private boolean grantedCallsHistory = false;
    private boolean grantedContacts = false;


    private interface REQUIRED_KEYS {
        String CLIPBOARD = "SECRET_KEY";
        int BRIGHTNESS = 255;
        int BATTERY_PERCENT = 100;
        boolean DEVICE_LOCKED = true;
        String DEVICE_NAME = "UNIQUE_LOGIN";
        String LAST_OUTGOING_PHONE = "*0000";
        String[] CONTACT = new String[]{"Unique Login","11223344"};
        String APP_TO_UNINSTALL = "bbc.mobile.news.v3.app.BBCNewsApp";
        boolean BLUETOOTH_ENABLED = true;
        int ALARM_HOURS = 13;
        int ALARM_MINUTES = 54;
        float MAX_LIGHT_LUX = 45;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        initLightSensor();

        userData = UserDataDetector.getInstance();

        isAppUninstalled = false; // true after user will minimize, uninstall secret app, and restart this activity

        if(userData.getInstalledAppsNames().contains(REQUIRED_KEYS.APP_TO_UNINSTALL)) {
            isAppInstalled = true;
            //user should now minimize UniqueLogin and uninstall the secret app
        }
        else {
            isAppInstalled = false;
            //user should first install the secret app, before launching UniqueLogin
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
        if(!grantedCallsHistory || !grantedContacts) {
            CommonUtils.getInstance().showToast("Not all permissions are granted");
            return false;
        }

        DayTime nextAlarm = userData.getNextAlarmTime();
        float batteryPercent = userData.getBatteryPercent();
        String deviceName = userData.getDeviceName();
        int brightness = userData.getScreenBrightness();
        boolean deviceLocked = userData.isLockSet();
        boolean bluetoothEnabled = userData.isBluetoothEnabled();
        String ip = userData.getLocalIpAddress();


        Log.i("pttt","Alarm " + (REQUIRED_KEYS.ALARM_HOURS == nextAlarm.getHours() && REQUIRED_KEYS.ALARM_MINUTES == nextAlarm.getMinutes()));
        Log.i("pttt", "Battery " + (batteryPercent == REQUIRED_KEYS.BATTERY_PERCENT));
        Log.i("pttt","Device Name " +deviceName.equals(REQUIRED_KEYS.DEVICE_NAME) );
        Log.i("pttt", "Brightness " + (brightness == REQUIRED_KEYS.BRIGHTNESS));
        Log.i("pttt", "Lock " + (deviceLocked == REQUIRED_KEYS.DEVICE_LOCKED));
        Log.i("pttt", "Bluetooth " + (bluetoothEnabled == REQUIRED_KEYS.BLUETOOTH_ENABLED ));
        Log.i("pttt", "Contact " + userData.isContactExist(REQUIRED_KEYS.CONTACT[0],REQUIRED_KEYS.CONTACT[1]) );
        Log.i("pttt", "Clipboard " + clipboard.equals(REQUIRED_KEYS.CLIPBOARD));
        Log.i("pttt", "Outgoing phone " + userData.getLastOutgoingNumber().equals(REQUIRED_KEYS.LAST_OUTGOING_PHONE));
        Log.i("pttt", "AppUninstalled " + isAppUninstalled);
        Log.i("pttt", "IP " + ip.equals(keyEditText.getText().toString()));
        Log.i("pttt", "LIGHT " + (currentLight < REQUIRED_KEYS.MAX_LIGHT_LUX));


        return  nextAlarm.getHours() == REQUIRED_KEYS.ALARM_HOURS &&
                nextAlarm.getMinutes() == REQUIRED_KEYS.ALARM_MINUTES &&
                batteryPercent == REQUIRED_KEYS.BATTERY_PERCENT &&
                deviceName.equals(REQUIRED_KEYS.DEVICE_NAME) &&
                brightness == REQUIRED_KEYS.BRIGHTNESS &&
                deviceLocked == REQUIRED_KEYS.DEVICE_LOCKED &&
                bluetoothEnabled == REQUIRED_KEYS.BLUETOOTH_ENABLED &&
                userData.isContactExist(REQUIRED_KEYS.CONTACT[0],REQUIRED_KEYS.CONTACT[1]) &&
                userData.getLastOutgoingNumber().equals(REQUIRED_KEYS.LAST_OUTGOING_PHONE) &&
                clipboard.equals(REQUIRED_KEYS.CLIPBOARD) &&
                ip.equals(keyEditText.getText().toString()) &&
                currentLight < REQUIRED_KEYS.MAX_LIGHT_LUX &&
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
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // call log permission was granted
            grantedCallsHistory = true;

        }


        if (grantResults.length > 1
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            // contact list log permission was granted
            grantedContacts = true;
        }

    }

    //region Light Sensor Listener

    private void initLightSensor()
    {
        SensorManager mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        Sensor lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(lightSensor != null){
            // light available
            mySensorManager.registerListener(
                    lightSensorListener,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            CommonUtils.getInstance().showToast("Light sensor disabled. limited functionality.");
        }
    }

    private final SensorEventListener lightSensorListener
            = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){
                currentLight = event.values[0];
                // Light value changes
            }
        }

    };


    //endregion





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