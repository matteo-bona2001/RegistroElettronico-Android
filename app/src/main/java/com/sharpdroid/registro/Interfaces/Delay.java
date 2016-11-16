package com.sharpdroid.registro.Interfaces;

import java.io.Serializable;

public class Delay implements Serializable {
    private int id;
    private String day;
    private String hours;
    private String justification;
    private boolean done;

    public Delay(int id, String day, String hours, String justification, boolean done) {
        this.id = id;
        this.day = day;
        this.hours = hours;
        this.justification = justification;
        this.done = done;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
