package com.quanlychitieu.doan.bottomnav;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.alert.AlertActivity;
import com.quanlychitieu.doan.choosetransaction.ChooseTransactionActivity;
import com.quanlychitieu.doan.home.HomeActivity;
import com.quanlychitieu.doan.setting.SettingActivity;
import com.quanlychitieu.doan.statistic.StatisticActivity;

public class BottomNavHelper {

    private static final int COLOR_SELECTED =
            Color.parseColor("#2563EB");

    private static final int COLOR_NORMAL =
            Color.parseColor("#64748B");

    public static void setup(Activity activity) {

        LinearLayout bottomNav =
                activity.findViewById(R.id.bottomNav);

        LinearLayout navHome =
                activity.findViewById(R.id.navHome);

        LinearLayout navStatistic =
                activity.findViewById(R.id.navStatistic);

        LinearLayout navAdd =
                activity.findViewById(R.id.navAdd);

        LinearLayout navAlert =
                activity.findViewById(R.id.navAlert);

        LinearLayout navSetting =
                activity.findViewById(R.id.navSetting);

        ImageView imgHome =
                activity.findViewById(R.id.imgHome);

        ImageView imgStatistic =
                activity.findViewById(R.id.imgStatistic);

        ImageView imgAdd =
                activity.findViewById(R.id.imgAdd);

        ImageView imgAlert =
                activity.findViewById(R.id.imgAlert);

        ImageView imgSetting =
                activity.findViewById(R.id.imgSetting);

        TextView tvHome =
                activity.findViewById(R.id.tvHome);

        TextView tvStatistic =
                activity.findViewById(R.id.tvStatistic);

        TextView tvAdd =
                activity.findViewById(R.id.tvAdd);

        TextView tvAlert =
                activity.findViewById(R.id.tvAlert);

        TextView tvSetting =
                activity.findViewById(R.id.tvSetting);

        setupBottomSafeArea(activity, bottomNav);

        setupResponsiveSize(
                activity,
                imgHome,
                imgStatistic,
                imgAdd,
                imgAlert,
                imgSetting,
                tvHome,
                tvStatistic,
                tvAdd,
                tvAlert,
                tvSetting
        );

        setCurrentTabColor(
                activity,
                imgHome,
                imgStatistic,
                imgAlert,
                imgSetting,
                tvHome,
                tvStatistic,
                tvAlert,
                tvSetting
        );

        // Trang chủ
        if (navHome != null) {
            navHome.setOnClickListener(v -> {

                if (!(activity instanceof HomeActivity)) {

                    Intent intent = new Intent(
                            activity,
                            HomeActivity.class
                    );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    );

                    activity.startActivity(intent);
                }
            });
        }

        // Thống kê
        if (navStatistic != null) {
            navStatistic.setOnClickListener(v -> {

                if (!(activity instanceof StatisticActivity)) {

                    Intent intent = new Intent(
                            activity,
                            StatisticActivity.class
                    );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    );

                    activity.startActivity(intent);
                }
            });
        }

        // Thêm giao dịch
        if (navAdd != null) {
            navAdd.setOnClickListener(v -> {

                Intent intent = new Intent(
                        activity,
                        ChooseTransactionActivity.class
                );

                activity.startActivity(intent);
            });
        }

        // Cảnh báo
        if (navAlert != null) {
            navAlert.setOnClickListener(v -> {

                if (!(activity instanceof AlertActivity)) {

                    Intent intent = new Intent(
                            activity,
                            AlertActivity.class
                    );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    );

                    activity.startActivity(intent);
                }
            });
        }

        // Cài đặt
        if (navSetting != null) {
            navSetting.setOnClickListener(v -> {

                if (!(activity instanceof SettingActivity)) {

                    Intent intent = new Intent(
                            activity,
                            SettingActivity.class
                    );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    );

                    activity.startActivity(intent);
                }
            });
        }
    }

    /**
     * Xác định Activity hiện tại và tô màu mục tương ứng.
     */
    private static void setCurrentTabColor(
            Activity activity,
            ImageView imgHome,
            ImageView imgStatistic,
            ImageView imgAlert,
            ImageView imgSetting,
            TextView tvHome,
            TextView tvStatistic,
            TextView tvAlert,
            TextView tvSetting
    ) {

        resetAllColors(
                imgHome,
                imgStatistic,
                imgAlert,
                imgSetting,
                tvHome,
                tvStatistic,
                tvAlert,
                tvSetting
        );

        if (activity instanceof HomeActivity) {

            setSelectedColor(imgHome, tvHome);

        } else if (activity instanceof StatisticActivity) {

            setSelectedColor(
                    imgStatistic,
                    tvStatistic
            );

        } else if (activity instanceof AlertActivity) {

            setSelectedColor(
                    imgAlert,
                    tvAlert
            );

        } else if (activity instanceof SettingActivity) {

            setSelectedColor(
                    imgSetting,
                    tvSetting
            );
        }
    }

    /**
     * Đưa toàn bộ icon và chữ về màu bình thường.
     */
    private static void resetAllColors(
            ImageView imgHome,
            ImageView imgStatistic,
            ImageView imgAlert,
            ImageView imgSetting,
            TextView tvHome,
            TextView tvStatistic,
            TextView tvAlert,
            TextView tvSetting
    ) {

        setNormalColor(imgHome, tvHome);
        setNormalColor(imgStatistic, tvStatistic);
        setNormalColor(imgAlert, tvAlert);
        setNormalColor(imgSetting, tvSetting);
    }

    /**
     * Tô màu xanh cho mục đang được chọn.
     */
    private static void setSelectedColor(
            ImageView imageView,
            TextView textView
    ) {

        if (imageView != null) {
            imageView.setColorFilter(COLOR_SELECTED);
        }

        if (textView != null) {
            textView.setTextColor(COLOR_SELECTED);
        }
    }

    /**
     * Tô màu xám cho mục chưa được chọn.
     */
    private static void setNormalColor(
            ImageView imageView,
            TextView textView
    ) {

        if (imageView != null) {
            imageView.setColorFilter(COLOR_NORMAL);
        }

        if (textView != null) {
            textView.setTextColor(COLOR_NORMAL);
        }
    }

    /**
     * Chừa khoảng trống cho thanh điều hướng hệ thống.
     */
    private static void setupBottomSafeArea(
            Activity activity,
            LinearLayout bottomNav
    ) {

        if (bottomNav == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                bottomNav,
                (view, insets) -> {

                    Insets navigationBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.navigationBars()
                            );

                    view.setPadding(
                            dp(activity, 6),
                            dp(activity, 5),
                            dp(activity, 6),
                            navigationBars.bottom + dp(activity, 4)
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(bottomNav);
    }

    /**
     * Tự điều chỉnh kích thước icon theo màn hình.
     */
    private static void setupResponsiveSize(
            Activity activity,
            ImageView imgHome,
            ImageView imgStatistic,
            ImageView imgAdd,
            ImageView imgAlert,
            ImageView imgSetting,
            TextView tvHome,
            TextView tvStatistic,
            TextView tvAdd,
            TextView tvAlert,
            TextView tvSetting
    ) {

        DisplayMetrics metrics =
                Resources.getSystem().getDisplayMetrics();

        float screenWidthDp =
                metrics.widthPixels / metrics.density;

        int iconSizeDp =
                Math.round(screenWidthDp / 17f);

        iconSizeDp =
                clamp(iconSizeDp, 20, 26);

        int addSizeDp =
                Math.round(screenWidthDp / 8f);

        addSizeDp =
                clamp(addSizeDp, 42, 50);

        float textSizeSp;

        if (screenWidthDp < 350) {
            textSizeSp = 9f;
        } else if (screenWidthDp < 430) {
            textSizeSp = 10f;
        } else {
            textSizeSp = 11f;
        }

        setViewSize(
                activity,
                imgHome,
                iconSizeDp,
                iconSizeDp
        );

        setViewSize(
                activity,
                imgStatistic,
                iconSizeDp,
                iconSizeDp
        );

        setViewSize(
                activity,
                imgAdd,
                addSizeDp,
                addSizeDp
        );

        setViewSize(
                activity,
                imgAlert,
                iconSizeDp,
                iconSizeDp
        );

        setViewSize(
                activity,
                imgSetting,
                iconSizeDp,
                iconSizeDp
        );

        setTextSize(tvHome, textSizeSp);
        setTextSize(tvStatistic, textSizeSp);
        setTextSize(tvAdd, textSizeSp);
        setTextSize(tvAlert, textSizeSp);
        setTextSize(tvSetting, textSizeSp);
    }

    private static void setViewSize(
            Activity activity,
            View view,
            int widthDp,
            int heightDp
    ) {

        if (view == null) {
            return;
        }

        ViewGroup.LayoutParams params =
                view.getLayoutParams();

        params.width =
                dp(activity, widthDp);

        params.height =
                dp(activity, heightDp);

        view.setLayoutParams(params);
    }

    private static void setTextSize(
            TextView textView,
            float sizeSp
    ) {

        if (textView != null) {
            textView.setTextSize(sizeSp);
        }
    }

    private static int clamp(
            int value,
            int min,
            int max
    ) {

        return Math.max(
                min,
                Math.min(value, max)
        );
    }

    private static int dp(
            Activity activity,
            int value
    ) {

        return Math.round(
                value
                        * activity
                        .getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}