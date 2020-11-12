package com.idankorenisraeli.uniquelogin.common;

import android.app.Application;

import com.idankorenisraeli.uniquelogin.UserDataDetector;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CommonUtils.initHelper(this);
        UserDataDetector.initHelper(this);
    }
}
