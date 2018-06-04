package com.alarm.dio.kyoudai.dioalarmclock;


import java.io.Serializable;

public class Alarm {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    private String name;
    private String time;
    private String days;
    private String sound;

    public Alarm(String name, String time, String days, String sound) {
        this.name = name;
        this.time = time;
        this.days = days;
        this.sound = sound;
    }
    public Alarm() {

    }


}
