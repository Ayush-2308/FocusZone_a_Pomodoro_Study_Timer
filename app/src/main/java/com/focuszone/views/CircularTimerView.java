package com.focuszone.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.focuszone.data.model.SessionType;
import com.focuszone.utils.TimeUtils;

public class CircularTimerView extends View {

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint modePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sessionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private final Rect textBounds = new Rect();

    private long totalSeconds = 25L * 60L;
    private long remainingSeconds = 25L * 60L;
    private float animatedFraction = 1f;
    private int modeColor = Color.rgb(255, 107, 53);
    private String modeLabel = "Focus";
    private String sessionCounter = "Session 1 / 4";
    private boolean showCounter = true;
    private ValueAnimator progressAnimator;

    public CircularTimerView(Context context) {
        super(context);
        init();
    }

    public CircularTimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularTimerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setTimer(long totalSeconds, long remainingSeconds) {
        this.totalSeconds = Math.max(1L, totalSeconds);
        this.remainingSeconds = Math.max(0L, remainingSeconds);
        float targetFraction = Math.max(0f, Math.min(1f, this.remainingSeconds / (float) this.totalSeconds));
        animateFractionTo(targetFraction);
    }

    public void setMode(String label) {
        modeLabel = label == null ? "Focus" : label;
        SessionType type = SessionType.FOCUS;
        if ("Short Break".equals(modeLabel)) {
            type = SessionType.SHORT_BREAK;
        } else if ("Long Break".equals(modeLabel)) {
            type = SessionType.LONG_BREAK;
        }
        modeColor = type.getColor();
        progressPaint.setColor(modeColor);
        sessionPaint.setColor(modeColor);
        invalidate();
    }

    public void setSessionCounter(String sessionCounter) {
        this.sessionCounter = sessionCounter == null ? "" : sessionCounter;
        invalidate();
    }

    public void setShowCounter(boolean showCounter) {
        this.showCounter = showCounter;
        invalidate();
    }

    public void pulse() {
        animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(180L)
                .withEndAction(() -> animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(220L)
                        .start())
                .start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desired = Math.round(width * 0.86f);
        int height = resolveSize(desired, heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float stroke = dp(20);
        float padding = stroke / 2f + dp(8);
        arcBounds.set(padding, padding, getWidth() - padding, getHeight() - padding);

        canvas.drawArc(arcBounds, 0f, 360f, false, backgroundPaint);
        canvas.drawArc(arcBounds, 270f, animatedFraction * 360f, false, progressPaint);
        drawCenterText(canvas);
    }

    private void init() {
        backgroundPaint.setColor(Color.rgb(42, 42, 42));
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(dp(20));
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint.setColor(modeColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(dp(20));
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        timePaint.setColor(Color.WHITE);
        timePaint.setTextAlign(Paint.Align.CENTER);
        timePaint.setTextSize(sp(48));
        timePaint.setFakeBoldText(true);

        modePaint.setColor(Color.rgb(170, 170, 170));
        modePaint.setTextAlign(Paint.Align.CENTER);
        modePaint.setTextSize(sp(14));

        sessionPaint.setColor(modeColor);
        sessionPaint.setTextAlign(Paint.Align.CENTER);
        sessionPaint.setTextSize(sp(12));
    }

    private void drawCenterText(Canvas canvas) {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        String time = TimeUtils.formatSeconds(remainingSeconds);
        timePaint.getTextBounds(time, 0, time.length(), textBounds);
        canvas.drawText(time, centerX, centerY - dp(2), timePaint);
        canvas.drawText(modeLabel, centerX, centerY + dp(30), modePaint);
        if (showCounter) {
            canvas.drawText(sessionCounter, centerX, centerY + dp(54), sessionPaint);
        }
    }

    private void animateFractionTo(float targetFraction) {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
        progressAnimator = ValueAnimator.ofFloat(animatedFraction, targetFraction);
        progressAnimator.setDuration(260L);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            animatedFraction = (float) animation.getAnimatedValue();
            invalidate();
        });
        progressAnimator.start();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
