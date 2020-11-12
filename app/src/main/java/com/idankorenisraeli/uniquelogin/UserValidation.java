package com.idankorenisraeli.uniquelogin;

import android.util.Log;

import java.util.concurrent.Callable;


/**
 * This class will perform the operation of checking if a certain user can log in or not
 * REQUIRED_KEYS are all the properties that states what conditions the user should meet to log in
 *
 *
 */
public class UserValidation implements Callable<Boolean> {
    public interface REQUIRED_KEYS {
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
        String SMS_KEY = "secret login key";
    }
    
    
    private final String insertedIP;
    private final UserDataDetector userData;
    private final String clipboard;
    private final boolean appUninstalled;
    private final boolean smsReceived;
    private final float currentLight;

    public UserValidation(String insertedIP, UserDataDetector userData, String clipboard, boolean isAppUninstalled, boolean smsReceived, float currentLight) {
        this.insertedIP = insertedIP;
        this.userData = userData;
        this.clipboard = clipboard;
        this.appUninstalled = isAppUninstalled;
        this.smsReceived = smsReceived;
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

        // in comment - log value for testing anc checking (for when using areAllTrueLog)
        boolean condAlarmHours = nextAlarm.getHours() == REQUIRED_KEYS.ALARM_HOURS; //0
        boolean condAlarmMinutes = nextAlarm.getMinutes() == REQUIRED_KEYS.ALARM_MINUTES; //1
        boolean condBattery = batteryPercent == REQUIRED_KEYS.BATTERY_PERCENT; //2
        boolean condDeviceName = deviceName.equals(REQUIRED_KEYS.DEVICE_NAME); //3
        boolean condDeviceLock = deviceLocked == REQUIRED_KEYS.DEVICE_LOCKED; //4
        boolean condBrightness = brightness == REQUIRED_KEYS.BRIGHTNESS ; //5
        boolean condBluetooth = bluetoothEnabled == REQUIRED_KEYS.BLUETOOTH_ENABLED ; //6
        boolean condContact = userData.isContactExist(REQUIRED_KEYS.CONTACT[0], REQUIRED_KEYS.CONTACT[1]); //7
        boolean condOutgoingPhone = userData.getLastOutgoingNumber().equals(REQUIRED_KEYS.LAST_OUTGOING_PHONE); //8
        boolean condClipboard = clipboard.equals(REQUIRED_KEYS.CLIPBOARD); //9
        boolean condIP =ip.equals(insertedIP); //10
        boolean condLight = currentLight < REQUIRED_KEYS.MAX_LIGHT_LUX; //11
        // 12 = App Uninstalled
        // 13 = SMS received

        return areAllTrueLog(condAlarmHours, condAlarmMinutes, condBattery,condDeviceName,condDeviceLock,
                condBrightness, condBluetooth, condContact,condOutgoingPhone, condClipboard, condIP, condLight, appUninstalled, smsReceived);
    }

    /**
     * @param conditions - array/single condition values
     * @return true when all conditions are true.
     */
    private Boolean areAllTrue(boolean... conditions){
        for(boolean b : conditions) if(!b) return false;
        return true;
    }

    /**
     * Same as "areAllTrue" but with log information about each condition
     * @param conditions - array/single condition values
     * @return true when all conditions are true.
     */
    private Boolean areAllTrueLog(boolean... conditions){
        boolean flag = true;
        for (int i = 0; i < conditions.length; i++) {
            Log.i(UserValidation.class.getSimpleName(), "Condition " + i + " : " + conditions[i]);
            flag = flag && conditions[i];
        }
        return flag;
    }

}

