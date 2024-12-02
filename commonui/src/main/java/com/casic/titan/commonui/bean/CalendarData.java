package com.casic.titan.commonui.bean;

import android.graphics.drawable.Drawable;

import androidx.databinding.BaseObservable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

/**
 * created by fz on 2024/12/2 10:25
 * describe:
 */
public class CalendarData extends BaseObservable {
    private int year;
    private int month;
    private int day = -1;
    private int week;
    private boolean enable;
    private Drawable drawable;
    private Fragment fragment;
    public CalendarData() {
    }

    public CalendarData(int year, int month) {
        this.year = year;
        this.month = month;
    }

    public CalendarData(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public CalendarData(int year, int month, List<CalendarData> calendarDataList) {
        this.year = year;
        this.month = month;
        this.calendarDataList = calendarDataList;
    }

    private List<CalendarData> calendarDataList;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public List<CalendarData> getCalendarDataList() {
        return calendarDataList;
    }

    public void setCalendarDataList(List<CalendarData> calendarDataList) {
        this.calendarDataList = calendarDataList;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }
}

