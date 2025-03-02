package com.kernelcrew.moodapp.ui;

import java.io.Serializable;

public class MoodEvent implements Serializable {
    private int year;
    private int month;
    private int day;
    private int moodEventNumber;

    public MoodEvent(int year, int month, int day, int moodEventNumber) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.moodEventNumber = moodEventNumber;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getMoodEventNumber() {
        return moodEventNumber;
    }
}
