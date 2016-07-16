package roo.clockanimation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.view.animation.AccelerateDecelerateInterpolator;

import org.joda.time.LocalDateTime;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Cap.ROUND;
import static android.graphics.Paint.Style.FILL;
import static android.graphics.Paint.Style.STROKE;
import static android.util.Log.d;
import static org.joda.time.Minutes.minutesBetween;

/**
 * Created by evelina on 15/07/2016.
 */

public class ClockDrawable extends Drawable implements Animatable {

    private final static int ANIMATION_DURATION = 500;

    private static final @ColorRes int FACE_COLOR = android.R.color.white;
    private static final @ColorRes int RIM_COLOR = R.color.colorAccent;

    private final Paint facePaint;
    private final Paint rimPaint;
    private final ValueAnimator minAnimator;
    private final ValueAnimator hourAnimator;

    private float rimRadius;
    private float faceRadius;
    private float screwRadius;

    private final Path hourHandPath;
    private final Path minuteHandPath;

    private float remainingHourRotation = 0f;
    private float remainingMinRotation = 0f;

    private float targetHourRotation = 0f;
    private float targetMinRotation = 0f;

    private float currentHourRotation = 0f;
    private float currentMinRotation;

    private boolean hourAnimInterrupted;
    private boolean minAnimInterrupted;

    private LocalDateTime previousTime;

    private boolean animateDays = true;

    public ClockDrawable(Resources resources) {
        facePaint = new Paint(ANTI_ALIAS_FLAG);
        facePaint.setColor(resources.getColor(FACE_COLOR));
        facePaint.setStyle(FILL);

        rimPaint = new Paint(ANTI_ALIAS_FLAG);
        rimPaint.setColor(resources.getColor(RIM_COLOR));
        rimPaint.setStyle(STROKE);
        rimPaint.setStrokeCap(ROUND);
        rimPaint.setStrokeWidth(resources.getDimension(R.dimen.clock_stroke_width));

        hourHandPath = new Path();
        minuteHandPath = new Path();

        hourAnimator = ValueAnimator.ofFloat(0, 0);
        hourAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        hourAnimator.setDuration(ANIMATION_DURATION);
        hourAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = (float) valueAnimator.getAnimatedValue();
                //d("ANIM", "Hfraction = " + fraction + ", remaining hour rotation = " + remainingHourRotation);
                remainingHourRotation = targetHourRotation - fraction;
                currentHourRotation = fraction;
                invalidateSelf();
            }
        });
        hourAnimator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (!hourAnimInterrupted) {
                    remainingHourRotation = 0f;
                }
                //i("ANIM", "END! remaining hour rotation = " + remainingHourRotation);
            }
        });


        minAnimator = ValueAnimator.ofFloat(0, 0);
        minAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        minAnimator.setDuration(ANIMATION_DURATION);
        minAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = (float) valueAnimator.getAnimatedValue();
                //d("ANIM", "Mfraction = " + fraction + ", remaining minute rotation = " + remainingMinRotation);
                remainingMinRotation = targetMinRotation - fraction;
                currentMinRotation = fraction;
                invalidateSelf();
            }
        });
        minAnimator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (!minAnimInterrupted) {
                    remainingMinRotation = 0f;
                }
                //i("ANIM", "END! remaining minute rotation = " + remainingMinRotation);
            }
        });

        previousTime = LocalDateTime.now().withTime(0, 0, 0, 0);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        rimRadius = Math.min(bounds.width(), bounds.height()) / 2f - rimPaint.getStrokeWidth();
        faceRadius = rimRadius - rimPaint.getStrokeWidth();
        screwRadius = rimPaint.getStrokeWidth() * 2;
        float hourHandLength = (float) (0.5 * faceRadius);
        float minuteHandLength = (float) (0.7 * faceRadius);
        float top = bounds.centerY() - screwRadius;

        hourHandPath.reset();
        hourHandPath.moveTo(bounds.centerX(), bounds.centerY());
        hourHandPath.addRect(bounds.centerX(), top, bounds.centerX(), top - hourHandLength, Direction.CCW);
        hourHandPath.close();

        minuteHandPath.reset();
        minuteHandPath.moveTo(bounds.centerX(), bounds.centerY());
        minuteHandPath.addRect(bounds.centerX(), top, bounds.centerX(), top - minuteHandLength, Direction.CCW);
        minuteHandPath.close();
    }

    @Override public void draw(Canvas canvas) {
        Rect bounds = getBounds();

        // draw the outer rim of the clock
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), rimRadius, rimPaint);
        // draw the face of the clock
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), faceRadius, facePaint);
        // draw the little rim in the middle of the clock
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), screwRadius, rimPaint);

        int saveCount = canvas.save();
        canvas.rotate(currentHourRotation, bounds.centerX(), bounds.centerY());
        // draw hour hand
        canvas.drawPath(hourHandPath, rimPaint);
        canvas.restoreToCount(saveCount);

        saveCount = canvas.save();
        canvas.rotate(currentMinRotation, bounds.centerX(), bounds.centerY());
        // draw minute hand
        canvas.drawPath(minuteHandPath, rimPaint);
        canvas.restoreToCount(saveCount);
    }

    @Override public void setAlpha(int alpha) {
        rimPaint.setAlpha(alpha);
        facePaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override public void setColorFilter(ColorFilter colorFilter) {
        rimPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override public void start() {
        hourAnimInterrupted = false;
        minAnimInterrupted = false;
        hourAnimator.start();
        minAnimator.start();
    }

    public void setAnimateDays(boolean animateDays) {
        this.animateDays = animateDays;
    }

    public void start(LocalDateTime newTime) {
        int minDiff = getMinsBetween(previousTime, newTime);
        // 60min ... 360grade
        // minDif .. minDelta
        float minDeltaRotation = ((float) minDiff * 360f) / 60f;
        // 720min ... 360grade = 12h ... 360grade
        // minDif ... hourDelta
        float hourDeltaRotation = ((float) minDiff * 360f) / 720f;

        remainingMinRotation += minDeltaRotation;
        remainingHourRotation += hourDeltaRotation;

        d("ANIM", "current hour rotation = " + currentHourRotation + ", current min rotation = " + currentMinRotation);

        if (isRunning()) {
            stop();
        }

        targetHourRotation = currentHourRotation + remainingHourRotation;
        hourAnimator.setFloatValues(currentHourRotation, targetHourRotation);

        targetMinRotation = currentMinRotation + remainingMinRotation;
        minAnimator.setFloatValues(currentMinRotation, targetMinRotation);

        start();

        previousTime = newTime;
    }

    @Override public void stop() {
        hourAnimInterrupted = true;
        minAnimInterrupted = true;
        hourAnimator.cancel();
        minAnimator.cancel();
    }

    @Override public boolean isRunning() {
        return hourAnimator.isRunning() || minAnimator.isRunning();
    }

    private int getMinsBetween(LocalDateTime t1, LocalDateTime t2) {
        if(animateDays) {
            return minutesBetween(t1, t2).getMinutes();
        }
        return minutesBetween(t1, t2.withDate(t1.getYear(), t1.getMonthOfYear(), t1.getDayOfMonth())).getMinutes();
    }
}
