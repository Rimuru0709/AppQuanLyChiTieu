package com.quanlychitieu.doan.statistic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.quanlychitieu.doan.R;

public class BarChartView extends View {

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private int[] values = new int[31];

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context,
                        @Nullable AttributeSet attrs,
                        int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setAntiAlias(true);
    }

    public void setData(int[] values) {
        if (values != null && values.length == 31) {
            this.values = values;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = dp(40);
        int paddingBottom = dp(30);

        int baseY = getHeight() - paddingBottom;

        int chartHeight =
                getHeight() - paddingBottom - dp(20);

        int maxValue = 0;

        for (int value : values) {
            if (Math.abs(value) > maxValue) {
                maxValue = Math.abs(value);
            }
        }

        if (maxValue == 0) {
            maxValue = 1;
        }

        // Trục X
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(dp(1));

        paint.setColor(
                ContextCompat.getColor(
                        getContext(),
                        R.color.divider_color
                )
        );

        canvas.drawLine(
                paddingLeft,
                baseY,
                getWidth() - dp(10),
                baseY,
                paint
        );

        // Số 0
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(sp(12));

        paint.setColor(
                ContextCompat.getColor(
                        getContext(),
                        R.color.text_secondary
                )
        );

        canvas.drawText(
                "0",
                dp(18),
                baseY + dp(4),
                paint
        );

        // Màu cột
        paint.setColor(
                ContextCompat.getColor(
                        getContext(),
                        R.color.color_primary
                )
        );

        int barWidth = dp(8);

        int gap =
                Math.max(
                        1,
                        (getWidth() - paddingLeft - dp(30)) / 31
                );

        for (int i = 0; i < 31; i++) {

            int barHeight =
                    Math.abs(values[i])
                            * chartHeight
                            / maxValue;

            int x =
                    paddingLeft + i * gap;

            RectF rect =
                    new RectF(
                            x,
                            baseY - barHeight,
                            x + barWidth,
                            baseY
                    );

            canvas.drawRoundRect(
                    rect,
                    dp(4),
                    dp(4),
                    paint
            );
        }

        // Chữ ngày
        paint.setColor(
                ContextCompat.getColor(
                        getContext(),
                        R.color.text_primary
                )
        );

        paint.setTextSize(sp(12));

        canvas.drawText("1",
                paddingLeft,
                baseY + dp(22),
                paint);

        canvas.drawText("5",
                paddingLeft + gap * 4,
                baseY + dp(22),
                paint);

        canvas.drawText("10",
                paddingLeft + gap * 9,
                baseY + dp(22),
                paint);

        canvas.drawText("15",
                paddingLeft + gap * 14,
                baseY + dp(22),
                paint);

        canvas.drawText("20",
                paddingLeft + gap * 19,
                baseY + dp(22),
                paint);

        canvas.drawText("25",
                paddingLeft + gap * 24,
                baseY + dp(22),
                paint);

        canvas.drawText("30",
                paddingLeft + gap * 29,
                baseY + dp(22),
                paint);
    }

    private int dp(int value) {
        return Math.round(
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    private float sp(int value) {
        return value *
                getResources()
                        .getDisplayMetrics()
                        .scaledDensity;
    }
}