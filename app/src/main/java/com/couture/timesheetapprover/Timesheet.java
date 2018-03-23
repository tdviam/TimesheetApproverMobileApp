package com.couture.timesheetapprover;

/**
 * Created by iet on 20-Mar-18.
 */

public class Timesheet {
    private String taskId;
    private String username;
    private String startDate;
    private String endDate;
    private String totalHours;
    private DailyLog sun;
    private DailyLog mon;
    private DailyLog tue;
    private DailyLog wed;
    private DailyLog thu;
    private DailyLog fri;
    private DailyLog sat;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }


    public DailyLog getSun() {
        return sun;
    }

    public void setSun(DailyLog sun) {
        this.sun = sun;
    }

    public DailyLog getMon() {
        return mon;
    }

    public void setMon(DailyLog mon) {
        this.mon = mon;
    }

    public DailyLog getTue() {
        return tue;
    }

    public void setTue(DailyLog tue) {
        this.tue = tue;
    }

    public DailyLog getWed() {
        return wed;
    }

    public void setWed(DailyLog wed) {
        this.wed = wed;
    }

    public DailyLog getThu() {
        return thu;
    }

    public void setThu(DailyLog thu) {
        this.thu = thu;
    }

    public DailyLog getFri() {
        return fri;
    }

    public void setFri(DailyLog fri) {
        this.fri = fri;
    }

    public DailyLog getSat() {
        return sat;
    }

    public void setSat(DailyLog sat) {
        this.sat = sat;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(String totalHours) {
        this.totalHours = totalHours;
    }
}
