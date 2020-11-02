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
import android.os.Bundle;
import android.util.Log;
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
    private long nextAlarm;
    private float batteryPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        // Non Rooted
        nextAlarm = getNextAlarmTime();
        clipboard = getClipboard();
        batteryPercent = getBatteryPercent();

        Log.i("pttt", nextAlarm + " alarm");


        for(String name : getInstalledAppsNames())
        {
            Log.i("pttt", "name: " + name);
        }

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
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

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

}