package com.quanlychitieu.doan.alert;

public class AlertModel {

    private int icon;
    private String category;
    private String budget;
    private String used;
    private String percent;
    private boolean enable;

    public AlertModel(int icon, String category, String budget,
                      String used, String percent, boolean enable) {
        this.icon = icon;
        this.category = category;
        this.budget = budget;
        this.used = used;
        this.percent = percent;
        this.enable = enable;
    }

    public int getIcon() {
        return icon;
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

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}