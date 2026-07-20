package com.quanlychitieu.doan.alert;

public class AlertModel {

    private int icon;
    private String color;
    private String category;
    private String budget;
    private String used;
    private String percent;
    private boolean enable;
    private int warningPercent;

    public AlertModel(int icon,
                      String color,
                      String category,
                      String budget,
                      String used,
                      String percent,
                      boolean enable,
                      int warningPercent) {
        this.icon = icon;
        this.color = color;
        this.category = category;
        this.budget = budget;
        this.used = used;
        this.percent = percent;
        this.enable = enable;
        this.warningPercent = warningPercent;
    }

    public int getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public String getCategory() {
        return category;
    }

    public String getBudget() {
        return budget;
    }

    public String getUsed() {
        return used;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getWarningPercent() {
        return warningPercent;
    }

    public void setWarningPercent(int warningPercent) {
        this.warningPercent = warningPercent;
    }
}