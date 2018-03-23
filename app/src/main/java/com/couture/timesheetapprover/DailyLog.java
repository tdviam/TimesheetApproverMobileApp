package com.couture.timesheetapprover;

/**
 * Created by iet on 21-Mar-18.
 */

public class DailyLog {
    private String ID;
    private String date;
    private String hours;
    private String description;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDate() {
        return (date == null) ? "" : date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHours() {
        return hours == null ? "" : hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
