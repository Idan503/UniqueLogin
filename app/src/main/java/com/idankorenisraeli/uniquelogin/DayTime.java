package com.idankorenisraeli.uniquelogin;

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
