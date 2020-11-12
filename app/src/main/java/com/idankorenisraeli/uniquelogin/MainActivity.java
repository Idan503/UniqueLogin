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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.idankorenisraeli.uniquelogin.common.CommonUtils;
import com.idankorenisraeli.uniquelogin.common.TaskRunner;
import com.idankorenisraeli.uniquelogin.sms.OnSmsReceived;
import com.idankorenisraeli.uniquelogin.sms.SmsListener;

 public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private EditText keyEditText;
    private ProgressBar progressBar;

    private UserDataDetector userData;
    private String clipboard; //current string item that was copied to clipboard
    private float currentLight; // current light detected by sensor

    private boolean secretAppInstalled;
    private boolean secretAppUninstalled;
    private boolean secretSmsReceived = false;

    //Dangerous permissions flag
    private boolean grantedCallsHistory = false;
    private boolean grantedContacts = false;
    private boolean grantedSms = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        initLightSensor();


        SmsListener.setCallback(new OnSmsReceived() {
            @Override
            public void onSmsReceived(String sender, String msg) {
                if(msg.contains(UserValidation.REQUIRED_KEYS.SMS_KEY))
                    secretSmsReceived = true;
            }
        });

        userData = UserDataDetector.getInstance();

        secretAppUninstalled = false; // true after user will minimize, uninstall secret app, and restart this activity

        if(userData.getInstalledAppsNames().contains(UserValidation.REQUIRED_KEYS.APP_TO_UNINSTALL)) {
            secretAppInstalled = true;
            //user should now minimize UniqueLogin and uninstall the secret app
        }
        else {
            secretAppInstalled = false;
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
            // can't perform check
            return;
        }

        //code gets to here iff dangerous permissions are granted


        // Validation operation may take some time so we do it async and show progressbar
        TaskRunner taskRunner = new TaskRunner();
        UserValidation conditionCheck = new UserValidation(keyEditText.getText().toString(),
                userData,clipboard, secretAppUninstalled, secretSmsReceived, currentLight);

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
                new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.RECEIVE_SMS},
                1);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        clipboard = userData.getClipboard();
        // When window is being focused, detecting what is the current clipboard string
    }


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


        if (grantResults.length > 2
                && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            // sms log permission was granted
            grantedSms = true;
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
        if(secretAppInstalled && !userData.getInstalledAppsNames().contains(UserValidation.REQUIRED_KEYS.APP_TO_UNINSTALL)){
            // The app was installed, but after the restart it is not installed anymore.
            // It means that the user minimized the app and uninstalled the secret app
            secretAppUninstalled = true;
        }
    }
}