package com.idankorenisraeli.uniquelogin.common;

/**
 * A simple time of day class, in 00:00 to 23:59 format (non am/pm)
 */
public class DayTime {
    private int hours;
    private int minutes;

    public DayTime() {
    }


    public DayTime(int hours, int minutes) {
        this.hours = hours;
        this.minutes = minutes;
    }


    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
}
