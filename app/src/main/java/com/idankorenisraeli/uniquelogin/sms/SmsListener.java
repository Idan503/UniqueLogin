package com.idankorenisraeli.uniquelogin.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsListener extends BroadcastReceiver {
    private static OnSmsReceived callback = null;
    // Callback should be static because app creates an object via empty costructor (receiver declared in manifest.xml).
    // Therefore, in order to be able to communicate with the main activity, a static callback shoud be attached.


    public static void setCallback(OnSmsReceived onReceived){
        callback = onReceived;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdu_Objects = (Object[]) bundle.get("pdus");
                if (pdu_Objects != null) {

                    for (Object aObject : pdu_Objects) {

                        SmsMessage currentSMS = getIncomingMessage(aObject, bundle);

                        String senderNo = currentSMS.getDisplayOriginatingAddress();
                        String message = currentSMS.getDisplayMessageBody();
                        if(callback!=null)
                            callback.onSmsReceived(senderNo, message);
                    }
                    this.abortBroadcast();
                    // End of loop
                }
            }
        } // bundle null
    }

    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        String format = bundle.getString("format");
        currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);
        return currentSMS;
    }
}

