package com.focuszone.ui.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HeatmapView extends View {

    private final Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cellRect = new RectF();
    private final Calendar calendar = Calendar.getInstance();
    private final Map<Integer, Integer> sessionsByDay = new HashMap<>();

    private int daysInMonth;
    private int firstDayOffset;
    private float cellSize;
    private float gap;
    private float topOffset;

    public HeatmapView(Context context) {
        super(context);
        init();
    }

    public HeatmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeatmapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setData(Map<Integer, Integer> data) {
        sessionsByDay.clear();
        if (data != null) {
            sessionsByDay.putAll(data);
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = Math.round(width * 0.78f);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calculateMonth();

        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        gap = dp(6);
        topOffset = dp(42);
        cellSize = (width - gap * 6f) / 7f;

        drawTitle(canvas);
        drawWeekLabels(canvas);
        drawCells(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }

        int day = dayForPoint(event.getX(), event.getY());
        if (day > 0) {
            int count = sessionsByDay.getOrDefault(day, 0);
            Toast.makeText(
                    getContext(),
                    String.format(Locale.US, "%d sessions on this day.", count),
                    Toast.LENGTH_SHORT
            ).show();
        }
        return true;
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        textPaint.setColor(Color.rgb(190, 190, 190));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(sp(11));

        titlePaint.setColor(Color.WHITE);
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextSize(sp(16));
        titlePaint.setTextAlign(Paint.Align.LEFT);
    }

    private void calculateMonth() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        firstDayOffset = dayOfWeek - Calendar.MONDAY;
        if (firstDayOffset < 0) {
            firstDayOffset += 7;
        }
    }

    private void drawTitle(Canvas canvas) {
        String title = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                + " "
                + calendar.get(Calendar.YEAR);
        canvas.drawText(title, getPaddingLeft(), dp(20), titlePaint);
    }

    private void drawWeekLabels(Canvas canvas) {
        String[] labels = {"M", "T", "W", "T", "F", "S", "S"};
        float y = dp(38);
        for (int i = 0; i < labels.length; i++) {
            float x = getPaddingLeft() + i * (cellSize + gap) + cellSize / 2f;
            canvas.drawText(labels[i], x, y, textPaint);
        }
    }

    private void drawCells(Canvas canvas) {
        for (int day = 1; day <= daysInMonth; day++) {
            int index = firstDayOffset + day - 1;
            int row = index / 7;
            int column = index % 7;
            float left = getPaddingLeft() + column * (cellSize + gap);
            float top = topOffset + row * (cellSize + gap);

            cellRect.set(left, top, left + cellSize, top + cellSize);
            cellPaint.setColor(colorForCount(sessionsByDay.getOrDefault(day, 0)));
            canvas.drawRoundRect(cellRect, dp(6), dp(6), cellPaint);

            textPaint.setColor(Color.WHITE);
            canvas.drawText(
                    String.valueOf(day),
                    cellRect.centerX(),
                    cellRect.centerY() + textPaint.getTextSize() / 3f,
                    textPaint
            );
            textPaint.setColor(Color.rgb(190, 190, 190));
        }
    }

    private int dayForPoint(float x, float y) {
        if (y < topOffset) {
            return -1;
        }

        int column = (int) ((x - getPaddingLeft()) / (cellSize + gap));
        int row = (int) ((y - topOffset) / (cellSize + gap));
        if (column < 0 || column > 6 || row < 0) {
            return -1;
        }

        float cellLeft = getPaddingLeft() + column * (cellSize + gap);
        float cellTop = topOffset + row * (cellSize + gap);
        if (x > cellLeft + cellSize || y > cellTop + cellSize) {
            return -1;
        }

        int day = row * 7 + column - firstDayOffset + 1;
        return day >= 1 && day <= daysInMonth ? day : -1;
    }

    private int colorForCount(int count) {
        if (count <= 0) {
            return Color.rgb(42, 42, 42);
        }
        if (count <= 2) {
            return Color.rgb(255, 173, 133);
        }
        if (count <= 4) {
            return Color.rgb(255, 126, 74);
        }
        return Color.rgb(255, 107, 53);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
