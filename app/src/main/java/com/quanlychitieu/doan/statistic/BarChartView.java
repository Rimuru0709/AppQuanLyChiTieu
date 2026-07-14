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

import java.text.DecimalFormat;

public class BarChartView extends View {

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private int[] values =
            new int[31];

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(
            Context context,
            @Nullable AttributeSet attrs
    ) {
        super(context, attrs);
        init();
    }

    public BarChartView(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setAntiAlias(true);

        setLayerType(
                View.LAYER_TYPE_SOFTWARE,
                null
        );
    }

    public void setData(
            int[] values
    ) {
        if (values != null
                && values.length == 31) {

            this.values =
                    values.clone();

        } else {
            this.values =
                    new int[31];
        }

        invalidate();
    }

    @Override
    protected void onDraw(
            Canvas canvas
    ) {
        super.onDraw(canvas);

        if (getWidth() <= 0
                || getHeight() <= 0) {

            return;
        }

        int paddingLeft =
                dp(38);

        int paddingRight =
                dp(12);

        int paddingTop =
                dp(28);

        int paddingBottom =
                dp(32);

        float chartLeft =
                paddingLeft;

        float chartRight =
                getWidth() - paddingRight;

        float chartTop =
                paddingTop;

        float baseY =
                getHeight() - paddingBottom;

        float chartHeight =
                baseY - chartTop;

        float chartWidth =
                chartRight - chartLeft;

        if (chartWidth <= 0
                || chartHeight <= 0) {

            return;
        }

        int maxValue =
                findMaxValue();

        drawHorizontalGrid(
                canvas,
                chartLeft,
                chartRight,
                chartTop,
                baseY,
                maxValue
        );

        drawBars(
                canvas,
                chartLeft,
                chartWidth,
                chartTop,
                baseY,
                chartHeight,
                maxValue
        );

        drawDayLabels(
                canvas,
                chartLeft,
                chartWidth,
                baseY
        );
    }

    private int findMaxValue() {
        int maxValue = 0;

        for (int value : values) {
            int absoluteValue =
                    Math.abs(value);

            if (absoluteValue > maxValue) {
                maxValue =
                        absoluteValue;
            }
        }

        return Math.max(
                maxValue,
                1
        );
    }

    private void drawHorizontalGrid(
            Canvas canvas,
            float chartLeft,
            float chartRight,
            float chartTop,
            float baseY,
            int maxValue
    ) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(
                Paint.Style.STROKE
        );

        paint.setStrokeWidth(
                dpFloat(1)
        );

        paint.setColor(
                ContextCompat.getColor(
                        getContext(),
                        R.color.divider_color
                )
        );

        int lineCount = 4;

        for (int index = 0;
             index <= lineCount;
             index++) {

            float ratio =
                    index / (float) lineCount;

            float y =
                    baseY
                            - ratio
                            * (baseY - chartTop);

            canvas.drawLine(
                    chartLeft,
                    y,
                    chartRight,
                    y,
                    paint
            );

            int labelValue =
                    Math.round(
                            maxValue * ratio
                    );

            drawValueLabel(
                    canvas,
                    labelValue,
                    y
            );
        }
    }

    private void drawValueLabel(
            Canvas canvas,
            int value,
            float y
    ) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(
                Paint.Style.FILL
        );

        paint.setTextAlign(
                Paint.Align.RIGHT
        );

        paint.setTextSize(
                sp(9)
        );

        paint.setColor(
                ContextCompat.getColor(
                        getContext(),
                        R.color.text_secondary
                )
        );

        canvas.drawText(
                formatCompactMoney(value),
                dp(33),
                y + dp(3),
                paint
        );
    }

    private void drawBars(
            Canvas canvas,
            float chartLeft,
            float chartWidth,
            float chartTop,
            float baseY,
            float chartHeight,
            int maxValue
    ) {
        float slotWidth =
                chartWidth / 31f;

        float barWidth =
                Math.max(
                        dp(3),
                        Math.min(
                                dp(8),
                                slotWidth * 0.65f
                        )
                );

        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(
                Paint.Style.FILL
        );

        paint.setColor(
                ContextCompat.getColor(
                        getContext(),
                        R.color.color_primary
                )
        );

        for (int index = 0;
             index < 31;
             index++) {

            int value =
                    Math.abs(
                            values[index]
                    );

            if (value <= 0) {
                continue;
            }

            float normalized =
                    value / (float) maxValue;

            /*
             * Dùng căn bậc hai để các khoản nhỏ
             * vẫn nhìn thấy khi có một khoản rất lớn.
             */
            float visualRatio =
                    (float) Math.sqrt(
                            normalized
                    );

            float barHeight =
                    visualRatio
                            * chartHeight;

            /*
             * Cột có dữ liệu luôn cao tối thiểu 4dp.
             */
            barHeight =
                    Math.max(
                            barHeight,
                            dp(4)
                    );

            float centerX =
                    chartLeft
                            + index * slotWidth
                            + slotWidth / 2f;

            float left =
                    centerX
                            - barWidth / 2f;

            float right =
                    centerX
                            + barWidth / 2f;

            float top =
                    Math.max(
                            chartTop,
                            baseY - barHeight
                    );

            RectF barRect =
                    new RectF(
                            left,
                            top,
                            right,
                            baseY
                    );

            canvas.drawRoundRect(
                    barRect,
                    dp(3),
                    dp(3),
                    paint
            );
        }
    }

    private void drawDayLabels(
            Canvas canvas,
            float chartLeft,
            float chartWidth,
            float baseY
    ) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(
                Paint.Style.FILL
        );

        paint.setTextAlign(
                Paint.Align.CENTER
        );

        paint.setTextSize(
                sp(10)
        );

        paint.setColor(
                ContextCompat.getColor(
                        getContext(),
                        R.color.text_primary
                )
        );

        int[] days = {
                1,
                5,
                10,
                15,
                20,
                25,
                30
        };

        float slotWidth =
                chartWidth / 31f;

        for (int day : days) {
            int index =
                    day - 1;

            float centerX =
                    chartLeft
                            + index * slotWidth
                            + slotWidth / 2f;

            canvas.drawText(
                    String.valueOf(day),
                    centerX,
                    baseY + dp(20),
                    paint
            );
        }
    }

    private String formatCompactMoney(
            int value
    ) {
        if (value >= 1_000_000) {
            float million =
                    value / 1_000_000f;

            DecimalFormat formatter =
                    new DecimalFormat(
                            million >= 10
                                    ? "#"
                                    : "#.#"
                    );

            return formatter.format(
                    million
            ) + "tr";
        }

        if (value >= 1_000) {
            int thousand =
                    Math.round(
                            value / 1_000f
                    );

            return thousand + "k";
        }

        return String.valueOf(value);
    }

    private int dp(
            int value
    ) {
        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private float dpFloat(
            int value
    ) {
        return value
                * getResources()
                .getDisplayMetrics()
                .density;
    }

    private float sp(
            int value
    ) {
        return value
                * getResources()
                .getDisplayMetrics()
                .scaledDensity;
    }
}