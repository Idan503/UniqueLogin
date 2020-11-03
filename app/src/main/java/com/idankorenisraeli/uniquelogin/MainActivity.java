package com.idankorenisraeli.uniquelogin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.scottyab.rootbeer.RootBeer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.provider.ContactsContract.Directory.PACKAGE_NAME;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private EditText keyEditText;
    
    private String clipboard;
    private String deviceName;
    private long nextAlarm;
    private float batteryPercent;
    private List<String> installedApps;
    private int brightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        // Non Rooted
        nextAlarm = getNextAlarmTime();
        clipboard = getClipboard();
        batteryPercent = getBatteryPercent();
        installedApps = getInstalledAppsNames();
        deviceName = getDeviceName();
        brightness = getScreenBrightness();


        RootBeer rootBeer = new RootBeer(this);
        if(rootBeer.isRooted()){
            //Phone is rooted
            Log.i("pttt", "Device is rooted");
        }
        else
            CommonUtils.getInstance().showToast("non-root - limited app functionality");

    }

    private void findViews(){
        this.loginButton = findViewById(R.id.main_BTN_login);
        this.keyEditText = findViewById(R.id.main_EDT_text);
    }

    private String getClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip() != null) {
            CharSequence clip = clipboard.getPrimaryClip().getItemAt(0).coerceToText(MainActivity.this).toString();
            return clip.toString();
        }
        return "";
    }

    private float getBatteryPercent(){
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, filter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (level * 100) / (float)scale;
    }

    private long getNextAlarmTime()
    {
        AlarmManager.AlarmClockInfo nextClock = getSystemService(AlarmManager.class).getNextAlarmClock();
        if(nextClock==null)
            return -1; //no next clock
        return nextClock.getTriggerTime();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        clipboard = getClipboard();
    }

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


    private List<String> getInstalledAppsNames()
    {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        List<String> installedAppsNames = new ArrayList<String>();

        for(ApplicationInfo app : apps)
            installedAppsNames.add(app.name);

        return installedAppsNames;
    }

    private String getDeviceName()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return Settings.Global.getString(this.getContentResolver(), Settings.Global.DEVICE_NAME);
        }
        else
            return "Android Device";
    }

    private int getScreenBrightness() {
        int brightness = -1;
        try{
            brightness = Settings.System.getInt(this.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        }catch (Settings.SettingNotFoundException exception){
            exception.printStackTrace();
        }
        return brightness;
    }

}