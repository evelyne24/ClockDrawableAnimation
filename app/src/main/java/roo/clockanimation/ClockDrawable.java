package roo.clockanimation;

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
import android.view.animation.LinearInterpolator;

import org.joda.time.Hours;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Cap.ROUND;
import static android.graphics.Paint.Style.FILL;
import static android.graphics.Paint.Style.STROKE;
import static android.util.Log.i;

/**
 * Created by evelina on 15/07/2016.
 */

public class ClockDrawable extends Drawable implements Animatable, AnimatorUpdateListener {

    private final static float FULL_ROTATION_DEGREES = 360f;
    private final static float HOUR_ROTATION_DEGREES = 30f; // 30 = 360 / 12
    private final static float MINUTE_ROTATION_DEGREES = 0.5f; // 0.5 = 30 / 60
    private final static int ANIMATION_DURATION = 500;

    private static final @ColorRes int FACE_COLOR = android.R.color.white;
    private static final @ColorRes int RIM_COLOR = R.color.colorAccent;

    private final Paint facePaint;
    private final Paint rimPaint;
    private final ValueAnimator handAnimator;

    private float rimRadius;
    private float faceRadius;
    private float screwRadius;

    private final Path hourHandPath;
    private final Path minuteHandPath;

    private float hourHandStartRotation;
    private float hourHandCurrRotation;
    private float hourHandDeltaRotation;

    private float minuteHandStartRotation;
    private float minuteHandCurrRotation;
    private float minHandDeltaRotation;

    private LocalDateTime previousTime;

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

        handAnimator = ValueAnimator.ofFloat(0, FULL_ROTATION_DEGREES);
        handAnimator.setInterpolator(new LinearInterpolator());
        handAnimator.setDuration(ANIMATION_DURATION);
        handAnimator.addUpdateListener(this);

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
        canvas.rotate(hourHandCurrRotation, bounds.centerX(), bounds.centerY());
        // draw hour hand
        canvas.drawPath(hourHandPath, rimPaint);
        canvas.restoreToCount(saveCount);

        saveCount = canvas.save();
        canvas.rotate(minuteHandCurrRotation, bounds.centerX(), bounds.centerY());
        // draw minute hand
        canvas.drawPath(minuteHandPath, rimPaint);
        canvas.restoreToCount(saveCount);
    }

//    private PointF computePointAlongArc(PointF center, PointF start, float alpha) {
//        double cosA = cos(alpha);
//        double sinA = sin(alpha);
//        double rx = center.x + (start.x - center.x) * cosA + (center.y - start.y) * sinA;
//        double ry = center.y + (start.y - center.y) * cosA + (start.x - center.x) * sinA;
//        return new PointF((float) rx, (float) ry);
//    }

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
        handAnimator.start();
    }

    public void start(LocalDateTime currentTime) {
        int hoursBetween = Hours.hoursBetween(previousTime, currentTime).getHours();
        int minutesBetween = Minutes.minutesBetween(previousTime, currentTime).getMinutes();

        minuteHandStartRotation = minuteHandCurrRotation;
        minHandDeltaRotation = minutesBetween * 12 * MINUTE_ROTATION_DEGREES;

        hourHandStartRotation = hourHandCurrRotation;
        hourHandDeltaRotation = hoursBetween * HOUR_ROTATION_DEGREES;
        i("ANIM", "rotate MINUTES by " + minHandDeltaRotation + ", HOUR by " + hourHandDeltaRotation);

        handAnimator.setFloatValues(0, minHandDeltaRotation);
        handAnimator.start();

        previousTime = currentTime;
    }

    @Override public void stop() {
        handAnimator.cancel();
        handAnimator.cancel();
    }

    @Override public boolean isRunning() {
        return handAnimator.isRunning() || handAnimator.isRunning();
    }

    @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float fraction = valueAnimator.getAnimatedFraction();
        hourHandCurrRotation = (hourHandStartRotation + fraction * hourHandDeltaRotation) % FULL_ROTATION_DEGREES;
        minuteHandCurrRotation = (minuteHandStartRotation + fraction * minHandDeltaRotation) % FULL_ROTATION_DEGREES;
        invalidateSelf();
    }
}
