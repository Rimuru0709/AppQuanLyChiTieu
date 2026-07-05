package com.quanlychitieu.doan.statistic;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;

public class DonutChartView extends View {

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    float[] values = {100};
    int[] colors = {Color.parseColor("#D1D5DB")};
    int totalExpense = 0;

    public DonutChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(float[] values, int[] colors, int totalExpense) {
        this.values = values;
        this.colors = colors;
        this.totalExpense = totalExpense;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = Math.min(getWidth(), getHeight()) - 40;
        int left = (getWidth() - size) / 2;
        int top = (getHeight() - size) / 2;

        RectF rect = new RectF(left, top, left + size, top + size);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(35);
        paint.setStrokeCap(Paint.Cap.BUTT);

        float startAngle = -90;

        for (int i = 0; i < values.length; i++) {
            paint.setColor(colors[i]);
            float sweepAngle = values[i] * 360f / 100f;
            canvas.drawArc(rect, startAngle, sweepAngle, false, paint);
            startAngle += sweepAngle;
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#111111"));
        paint.setTextAlign(Paint.Align.CENTER);

        paint.setTextSize(25);
        paint.setFakeBoldText(true);
        canvas.drawText(formatMoney(totalExpense), getWidth() / 2, getHeight() / 2, paint);

        paint.setTextSize(21);
        paint.setFakeBoldText(false);
        canvas.drawText("Tổng chi", getWidth() / 2, getHeight() / 2 + 35, paint);
    }

    private String formatMoney(int money) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(money).replace(",", ".") + " đ";
    }
}