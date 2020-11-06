package com.idankorenisraeli.uniquelogin;

import android.Manifest;
import android.app.AlarmManager;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserDataDetector {
    private static UserDataDetector single_instance = null;
    Context context;

    private UserDataDetector(Context context){
        this.context = context.getApplicationContext();
    }

    public static void initHelper(Context context){
        if(single_instance==null)
            single_instance = new UserDataDetector(context);
    }

    public static UserDataDetector getInstance(){
        return single_instance;
    }


    //region Device Name
    public String getDeviceName()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return Settings.Global.getString(context.getContentResolver(), Settings.Global.DEVICE_NAME);
        }
        else
            return "Android Device";
    }
    //endregion

    //region Screen Brightness
    public int getScreenBrightness() {
        int brightness = -1;
        try{
            brightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        }catch (Settings.SettingNotFoundException exception){
            exception.printStackTrace();
        }
        return brightness;
    }
    //endregion


    //region Battery Percent
    public float getBatteryPercent(){
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, filter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (level * 100) / (float)scale;
    }
    //endregion

    // region Next Alarm
    public long getNextAlarmTime()
    {
        AlarmManager.AlarmClockInfo nextClock = context.getSystemService(AlarmManager.class).getNextAlarmClock();
        if(nextClock==null)
            return -1; //no next clock
        return nextClock.getTriggerTime();
    }

    //endregion

    //region Installed Apps List
    public List<String> getInstalledAppsNames()
    {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        List<String> installedAppsNames = new ArrayList<String>();

        for(ApplicationInfo app : apps)
            installedAppsNames.add(app.name);

        return installedAppsNames;
    }

    //endregion



    //region Clipboard
    public String getClipboard() {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip() != null) {
            CharSequence clip = clipboard.getPrimaryClip().getItemAt(0).coerceToText(context).toString();
            return clip.toString();
        }
        return "";
    }

    //endregion


    //region Contacts List
    public void getContactList() {
    }

    //endregion

    //region Audio Balance
    public void getAudioBalance()
    {
        // Opened a StackOverflow question, hoping for future updates
        // https://stackoverflow.com/questions/64703435/how-to-detect-android-device-left-right-audio-balance
    }

    //endregion


    //region Last Outgoing Phone Call Number

    // checks the history log of device phone calls and returns the number of the last outgoing call
    public String getLastOutgoingNumber() {
        Cursor managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int numberColumn = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int typeColumn = managedCursor.getColumnIndex(CallLog.Calls.TYPE);

        managedCursor.moveToLast(); //Starting with last phone call (latest)


        // Iterating the phone calls history log while looking for the outgoing
        do {
            String callType = managedCursor.getString(typeColumn); // call type
            int directionCode = Integer.parseInt(callType); //incoming/outgoing

            if (directionCode == CallLog.Calls.OUTGOING_TYPE) {
                // outgoing call was found, retuns the phone number
                return managedCursor.getString(numberColumn);
            }
        }while(managedCursor.moveToPrevious());

        return "NA"; //there is no last outgoing call
    }
    //endregion





}