package com.quanlychitieu.doan.statistic;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class BarChartView extends View {

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    int[] values = new int[31];

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(int[] values) {
        this.values = values;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = 45;
        int paddingBottom = 35;
        int baseY = getHeight() - paddingBottom;
        int chartHeight = getHeight() - paddingBottom - 20;

        int maxValue = 0;
        for (int value : values) {
            if (value > maxValue) maxValue = value;
        }

        if (maxValue == 0) maxValue = 1;

        paint.setColor(Color.parseColor("#E5E7EB"));
        paint.setStrokeWidth(2);
        canvas.drawLine(paddingLeft, baseY, getWidth() - 10, baseY, paint);

        paint.setColor(Color.parseColor("#64748B"));
        paint.setTextSize(20);
        canvas.drawText("0", 25, baseY + 5, paint);

        paint.setColor(Color.parseColor("#2563EB"));

        int barWidth = 10;
        int gap = (getWidth() - paddingLeft - 30) / 31;

        for (int i = 0; i < 31; i++) {
            int barHeight = values[i] * chartHeight / maxValue;
            int x = paddingLeft + i * gap;

            RectF rect = new RectF(x, baseY - barHeight, x + barWidth, baseY);
            canvas.drawRoundRect(rect, 8, 8, paint);
        }

        paint.setColor(Color.parseColor("#1E3A8A"));
        paint.setTextSize(20);

        canvas.drawText("1", paddingLeft, baseY + 28, paint);
        canvas.drawText("5", paddingLeft + gap * 4, baseY + 28, paint);
        canvas.drawText("10", paddingLeft + gap * 9, baseY + 28, paint);
        canvas.drawText("15", paddingLeft + gap * 14, baseY + 28, paint);
        canvas.drawText("20", paddingLeft + gap * 19, baseY + 28, paint);
        canvas.drawText("25", paddingLeft + gap * 24, baseY + 28, paint);
        canvas.drawText("30", paddingLeft + gap * 29, baseY + 28, paint);
    }
}