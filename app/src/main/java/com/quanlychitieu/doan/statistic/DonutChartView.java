package com.quanlychitieu.doan.statistic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.quanlychitieu.doan.R;

import java.text.DecimalFormat;

public class DonutChartView extends View {

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private float[] values = {100f};

    private int[] colors = {
            Color.parseColor("#D1D5DB")
    };

    private int totalExpense = 0;

    public DonutChartView(Context context) {
        super(context);
        init();
    }

    public DonutChartView(
            Context context,
            @Nullable AttributeSet attrs
    ) {
        super(context, attrs);
        init();
    }

    public DonutChartView(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setAntiAlias(true);
    }

    public void setData(
            float[] values,
            int[] colors,
            int totalExpense
    ) {
        if (values == null ||
                colors == null ||
                values.length == 0 ||
                values.length != colors.length) {

            this.values = new float[]{100f};

            this.colors = new int[]{
                    ContextCompat.getColor(
                            getContext(),
                            R.color.divider_color
                    )
            };

            this.totalExpense = 0;

        } else {
            this.values = values;
            this.colors = colors;
            this.totalExpense = totalExpense;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int chartPadding = dp(20);

        int size =
                Math.min(
                        getWidth(),
                        getHeight()
                ) - chartPadding * 2;

        if (size <= 0) {
            return;
        }

        float left =
                (getWidth() - size) / 2f;

        float top =
                (getHeight() - size) / 2f;

        RectF chartRect =
                new RectF(
                        left,
                        top,
                        left + size,
                        top + size
                );

        drawDonut(canvas, chartRect);
        drawCenterText(canvas);
    }

    private void drawDonut(
            Canvas canvas,
            RectF chartRect
    ) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(18));
        paint.setStrokeCap(Paint.Cap.BUTT);

        float startAngle = -90f;

        for (int index = 0;
             index < values.length;
             index++) {

            paint.setColor(colors[index]);

            float sweepAngle =
                    values[index] * 360f / 100f;

            canvas.drawArc(
                    chartRect,
                    startAngle,
                    sweepAngle,
                    false,
                    paint
            );

            startAngle += sweepAngle;
        }
    }

    private void drawCenterText(Canvas canvas) {
        int textColor =
                ContextCompat.getColor(
                        getContext(),
                        R.color.text_primary
                );

        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.CENTER);

        float centerX =
                getWidth() / 2f;

        float centerY =
                getHeight() / 2f;

        paint.setTextSize(sp(17));
        paint.setFakeBoldText(true);

        canvas.drawText(
                formatMoney(totalExpense),
                centerX,
                centerY - dp(2),
                paint
        );

        paint.setTextSize(sp(13));
        paint.setFakeBoldText(false);

        canvas.drawText(
                "Tổng chi",
                centerX,
                centerY + dp(25),
                paint
        );
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

    private String formatMoney(int money) {
        DecimalFormat formatter =
                new DecimalFormat("#,###");

        return formatter
                .format(money)
                .replace(",", ".") +
                " đ";
    }
}