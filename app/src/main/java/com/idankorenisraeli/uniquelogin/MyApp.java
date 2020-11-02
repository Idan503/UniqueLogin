package com.idankorenisraeli.uniquelogin;

import android.app.Application;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CommonUtils.initHelper(this);
    }
}
