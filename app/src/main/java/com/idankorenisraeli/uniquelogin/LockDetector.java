package com.idankorenisraeli.uniquelogin;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class LockDetector {
    private static LockDetector single_instance = null;
    private Context context;


    private LockDetector(Context context){
        this.context = context.getApplicationContext();
    }

    public static void initHelper(Context context){
        if(single_instance==null)
            single_instance = new LockDetector(context);
    }

    public static LockDetector getInstance(){
        return single_instance;
    }

    public boolean isDevicePatternLocked(){
        return isDeviceLocked() && isPatternSet();
    }

    public boolean isDeviceScreenLocked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return isDeviceLocked();
        } else {
            return isPatternSet() || isPassOrPinSet();
        }
    }

    /**
     * @return true if pattern set, false if not (or if an issue when checking)
     */
    private boolean isPatternSet() {
        ContentResolver cr = context.getContentResolver();
        try {
            int lockPatternEnable = Settings.Secure.getInt(cr, Settings.Secure.LOCK_PATTERN_ENABLED);
            return lockPatternEnable == 1;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    /**
     * @return true if pass or pin set
     */
    private boolean isPassOrPinSet() {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE); //api 16+
        return keyguardManager.isKeyguardSecure();
    }

    /**
     * @return true if pass or pin or pattern locks screen
     */
    @TargetApi(23)
    private boolean isDeviceLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE); //api 23+
        return keyguardManager.isDeviceSecure();
    }



}
