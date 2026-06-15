package com.quanlychitieu.doan.bottomnav;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.category.CategoryActivity;
import com.quanlychitieu.doan.choosetransaction.ChooseTransactionActivity;
import com.quanlychitieu.doan.home.HomeActivity;
import com.quanlychitieu.doan.setting.SettingActivity;
import com.quanlychitieu.doan.statistic.StatisticActivity;

public class BottomNavHelper {

    public static void setup(Activity activity) {
        TextView navHome = activity.findViewById(R.id.navHome);
        TextView navStatistic = activity.findViewById(R.id.navStatistic);
        TextView navAdd = activity.findViewById(R.id.navAdd);
        TextView navCategory = activity.findViewById(R.id.navCategory);
        TextView navSetting = activity.findViewById(R.id.navSetting);

        navHome.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, HomeActivity.class)));

        navStatistic.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, StatisticActivity.class)));

        navAdd.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, ChooseTransactionActivity.class)));

        navCategory.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, CategoryActivity.class)));

        navSetting.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, SettingActivity.class)));
    }
}