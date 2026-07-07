package com.quanlychitieu.doan.bottomnav;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.alert.AlertActivity;
import com.quanlychitieu.doan.choosetransaction.ChooseTransactionActivity;
import com.quanlychitieu.doan.home.HomeActivity;
import com.quanlychitieu.doan.setting.SettingActivity;
import com.quanlychitieu.doan.statistic.StatisticActivity;

public class BottomNavHelper {

    public static void setup(Activity activity) {

        TextView navHome = activity.findViewById(R.id.navHome);
        TextView navStatistic = activity.findViewById(R.id.navStatistic);
        TextView navAdd = activity.findViewById(R.id.navAdd);
        TextView navAlert = activity.findViewById(R.id.navAlert);
        TextView navSetting = activity.findViewById(R.id.navSetting);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (!(activity instanceof HomeActivity)) {
                    activity.startActivity(new Intent(activity, HomeActivity.class));
                }
            });
        }

        if (navStatistic != null) {
            navStatistic.setOnClickListener(v -> {
                if (!(activity instanceof StatisticActivity)) {
                    activity.startActivity(new Intent(activity, StatisticActivity.class));
                }
            });
        }

        if (navAdd != null) {
            navAdd.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, ChooseTransactionActivity.class)));
        }

        if (navAlert != null) {
            navAlert.setOnClickListener(v -> {
                if (!(activity instanceof AlertActivity)) {
                    activity.startActivity(new Intent(activity, AlertActivity.class));
                }
            });
        }

        if (navSetting != null) {
            navSetting.setOnClickListener(v -> {
                if (!(activity instanceof SettingActivity)) {
                    activity.startActivity(new Intent(activity, SettingActivity.class));
                }
            });
        }
    }
}