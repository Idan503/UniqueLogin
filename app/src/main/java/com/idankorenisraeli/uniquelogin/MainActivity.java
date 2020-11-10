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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private EditText keyEditText;
    private ProgressBar progressBar;

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
                validateUser();
            }
        });
    }

    private void findViews() {
        this.loginButton = findViewById(R.id.main_BTN_login);
        this.keyEditText = findViewById(R.id.main_EDT_text);
        this.progressBar = findViewById(R.id.main_PROG_progress_bar);
    }

    private void validateUser()
    {
        if(!grantedCallsHistory || !grantedContacts) {
            CommonUtils.getInstance().showToast("Not all permissions are granted");
            return;
        }

        //code gets to here iff dangerous permissions are granted

        TaskRunner taskRunner = new TaskRunner();
        UserConditionCheck conditionCheck = new UserConditionCheck(keyEditText.getText().toString(),userData,clipboard,isAppUninstalled, currentLight);

        progressBar.setVisibility(View.VISIBLE);

        taskRunner.executeAsync(conditionCheck, (result)-> {
            progressBar.setVisibility(View.INVISIBLE);
            if(result)
                CommonUtils.getInstance().showToast("Logged in successfully");
            else
                CommonUtils.getInstance().showToast("Failed to log in");
        });
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

    //region Check User Conditions



    private static class UserConditionCheck implements Callable<Boolean> {
        private final String insertedIP;
        private final UserDataDetector userData;
        private final String clipboard;
        private final boolean isAppUninstalled;
        private final float currentLight;

        public UserConditionCheck(String insertedIP, UserDataDetector userData, String clipboard, boolean isAppUninstalled, float currentLight) {
            this.insertedIP = insertedIP;
            this.userData = userData;
            this.clipboard = clipboard;
            this.isAppUninstalled = isAppUninstalled;
            this.currentLight = currentLight;
        }

        @Override
        public Boolean call() {
            DayTime nextAlarm = userData.getNextAlarmTime();
            float batteryPercent = userData.getBatteryPercent();
            String deviceName = userData.getDeviceName();
            int brightness = userData.getScreenBrightness();
            boolean deviceLocked = userData.isLockSet();
            boolean bluetoothEnabled = userData.isBluetoothEnabled();
            String ip = userData.getLocalIpAddress();

            boolean condAlarmHours = nextAlarm.getHours() == REQUIRED_KEYS.ALARM_HOURS;
            boolean condAlarmMinutes = nextAlarm.getMinutes() == REQUIRED_KEYS.ALARM_MINUTES;
            boolean condBattery = batteryPercent == REQUIRED_KEYS.BATTERY_PERCENT;
            boolean condDeviceName = deviceName.equals(REQUIRED_KEYS.DEVICE_NAME);
            boolean condDeviceLock = deviceLocked == REQUIRED_KEYS.DEVICE_LOCKED;
            boolean condBrightness = brightness == REQUIRED_KEYS.BRIGHTNESS ;
            boolean condBluetooth = bluetoothEnabled == REQUIRED_KEYS.BLUETOOTH_ENABLED ;
            boolean condContact = userData.isContactExist(REQUIRED_KEYS.CONTACT[0],REQUIRED_KEYS.CONTACT[1]);
            boolean condOutgoingPhone = userData.getLastOutgoingNumber().equals(REQUIRED_KEYS.LAST_OUTGOING_PHONE);
            boolean condClipboard = clipboard.equals(REQUIRED_KEYS.CLIPBOARD);
            boolean condIP =ip.equals(insertedIP);
            boolean condLight = currentLight < REQUIRED_KEYS.MAX_LIGHT_LUX;

            return areAllTrueLog(condAlarmHours, condAlarmMinutes, condBattery,condDeviceName,condDeviceLock,
                    condBrightness, condBluetooth, condContact,condOutgoingPhone, condClipboard, condIP, condLight, isAppUninstalled);
        }

        /**
         * @param conditions - array/single condition values
         * @return true when all conditions are true.
         */
        private Boolean areAllTrue(boolean... conditions){
            for(boolean b : conditions) if(!b) return false;
            return true;
        }

        private Boolean areAllTrueLog(boolean... conditions){
            boolean flag = true;
            for (int i = 0; i < conditions.length; i++) {
                Log.i(UserConditionCheck.class.getSimpleName(), "Condition " + i + " : " + conditions[i]);
                flag = flag && conditions[i];
            }
            return flag;
        }

    }


    private static class TaskRunner {
        private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
        private final Handler handler = new Handler(Looper.getMainLooper());

        public interface Callback<R> {
            void onComplete(R result);
        }

        public <R> void executeAsync(Callable<R> callable, Callback<R> callback) {
            executor.execute(() -> {
                final R result;
                try {
                    result = callable.call();
                handler.post(() -> {
                    callback.onComplete(result);
                });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

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