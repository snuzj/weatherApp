package com.snuzj.weatherapp;

public class FutureRVModel {
    private String date;
    private String condition;
    private String icon;

    private String temperature;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public FutureRVModel(String date, String condition, String icon, String temperature) {
        this.date = date;
        this.condition = condition;
        this.icon = icon;
        this.temperature = temperature;
    }




}
