package com.idankorenisraeli.uniquelogin.sms;

public interface OnSmsReceived {
    void onSmsReceived(String sender, String msg);
}
