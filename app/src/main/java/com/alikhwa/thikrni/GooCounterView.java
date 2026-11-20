package com.alikhwa.thikrni;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

/**
 * زر الصلاة على النبي بتأثير Venom: قرص ذهبي سائل تتحرك حوله قطرات تلتحم به،
 * ويمدّ لسانًا نحو إصبعك ثم يتناثر ويلتئم عند الرفع.
 */
public class GooCounterView extends FrameLayout {
    private static final int N = 7;

    private final Theme th;
    private final float d;
    private final Paint disc = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Goo.Blob[] drops = new Goo.Blob[N];
    private final float[] ang = new float[N], rho = new float[N], spd = new float[N],
            ph = new float[N], rad = new float[N];
    private final Goo.Blob touch = new Goo.Blob(0, 0, 0);
    private boolean touching;
    private float touchX, touchY;

    private float cx, cy, r0, press = 1f, pulse = 0f, burst = 1f, time = 0f;
    private ValueAnimator loop, burstAnim, pressAnim;
    private long last;

    public GooCounterView(Context c, Theme theme) {
        super(c);
        th = theme;
        d = c.getResources().getDisplayMetrics().density;
        setClipChildren(false);

        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(2f * d);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setColor(th.onAccent);

        float[] rhos = {1.00f, 1.08f, 0.96f, 1.18f, 1.02f, 1.28f, 1.12f};
        float[] spds = {0.55f, -0.40f, 0.70f, -0.62f, 0.33f, 0.48f, -0.52f};
        float[] rads = {0.24f, 0.19f, 0.27f, 0.15f, 0.22f, 0.13f, 0.17f};
        for (int i = 0; i < N; i++) {
            ang[i] = (float) (i * 2 * Math.PI / N);
            rho[i] = rhos[i];
            spd[i] = spds[i];
            rad[i] = rads[i];
            ph[i] = i * 1.3f;
            drops[i] = new Goo.Blob(0, 0, 0);
        }

        View back = new View(c) {
            @Override
            protected void onDraw(Canvas cv) {
                drawBack(cv);
            }
        };
        View gooLayer = new View(c) {
            @Override
            protected void onDraw(Canvas cv) {
                drawGoo(cv);
            }
        };
        View front = new View(c) {
            @Override
            protected void onDraw(Canvas cv) {
                drawFront(cv);
            }
        };
        addView(back, new LayoutParams(-1, -1));
        addView(gooLayer, new LayoutParams(-1, -1));
        addView(front, new LayoutParams(-1, -1));
        Goo.apply(gooLayer, 8f * d);
        setClickable(true);

        loop = ValueAnimator.ofFloat(0f, 1f);
        loop.setDuration(1000);
        loop.setRepeatCount(ValueAnimator.INFINITE);
        loop.setInterpolator(new LinearInterpolator());
        loop.addUpdateListener(a -> frame());
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        cx = w / 2f;
        cy = h / 2f;
        r0 = Math.min(w, h) / 2f * 0.56f;
        disc.setShader(new RadialGradient(cx - r0 * 0.3f, cy - r0 * 0.35f, r0 * 1.3f,
                new int[]{th.accentLight, th.accent, th.accentDark},
                new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        glow.setShader(new RadialGradient(cx, cy, r0 * 1.7f,
                Theme.a(th.accent, 0x66), Theme.a(th.accent, 0x00), Shader.TileMode.CLAMP));
        for (int i = 0; i < N; i++) {
            Goo.Blob b = drops[i];
            b.x = b.tx = cx + (float) Math.cos(ang[i]) * rho[i] * r0;
            b.y = b.ty = cy + (float) Math.sin(ang[i]) * rho[i] * r0;
            b.r = b.tr = rad[i] * r0;
        }
        touch.x = touch.tx = cx;
        touch.y = touch.ty = cy;
        touch.r = touch.tr = 0f;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        last = System.nanoTime();
        if (!loop.isStarted()) loop.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        loop.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    public void onVisibilityAggregated(boolean visible) {
        super.onVisibilityAggregated(visible);
        if (visible) {
            last = System.nanoTime();
            if (isAttachedToWindow() && !loop.isStarted()) loop.start();
        } else {
            loop.cancel();
        }
    }

    // ------------------------------------------------------------ الفيزياء

    private void frame() {
        long now = System.nanoTime();
        float dt = Math.min(0.033f, (now - last) / 1e9f);
        last = now;
        time += dt;
        pulse = (pulse + dt / 2.8f) % 1f;
        if (r0 <= 0) return;

        for (int i = 0; i < N; i++) {
            Goo.Blob b = drops[i];
            float a = ang[i] + time * spd[i];
            float rr = r0 * (rho[i] + 0.10f * (float) Math.sin(time * 1.7f + ph[i]));
            b.tx = cx + (float) Math.cos(a) * rr;
            b.ty = cy + (float) Math.sin(a) * rr;
            b.tr = r0 * rad[i] * (1f + 0.15f * (float) Math.sin(time * 2.3f + ph[i]));
            b.step(dt, 90f, 8f);
        }

        if (touching) {
            float dx = touchX - cx, dy = touchY - cy;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float reach = r0 * (0.75f + 0.55f * Math.min(1f, dist / r0));
            if (dist > 1f) {
                touch.tx = cx + dx / dist * reach;
                touch.ty = cy + dy / dist * reach;
            } else {
                touch.tx = cx;
                touch.ty = cy - reach;
            }
            touch.tr = r0 * 0.34f;
        } else {
            touch.tx = cx;
            touch.ty = cy;
            touch.tr = 0f;
        }
        touch.step(dt, 220f, 16f);

        for (int i = 0; i < getChildCount(); i++) getChildAt(i).invalidate();
    }

    private void splash() {
        for (int i = 0; i < N; i++) {
            Goo.Blob b = drops[i];
            float dx = b.x - cx, dy = b.y - cy;
            float len = Math.max(1f, (float) Math.sqrt(dx * dx + dy * dy));
            float sp = r0 * 7f;
            b.vx += dx / len * sp;
            b.vy += dy / len * sp;
        }
    }

    // --------------------------------------------------------------- الرسم

    private void drawBack(Canvas cv) {
        float R = r0 * press;
        for (int k = 0; k < 3; k++) {
            float f = (pulse + k / 3f) % 1f;
            ring.setColor(Theme.a(th.accent, (int) ((1f - f) * 110)));
            ring.setStrokeWidth(2f * d);
            cv.drawCircle(cx, cy, R * (1.1f + 0.55f * f), ring);
        }
        if (burst < 1f) {
            ring.setStrokeWidth(4f * d * (1f - burst) + 1f);
            ring.setColor(((int) ((1f - burst) * 230) << 24) | 0xFFFFFF);
            cv.drawCircle(cx, cy, R * (1f + 0.9f * burst), ring);
        }
        cv.drawCircle(cx, cy, r0 * 1.7f, glow);
    }

    private void drawGoo(Canvas cv) {
        cv.save();
        cv.scale(press, press, cx, cy);
        cv.drawCircle(cx, cy, r0, disc);
        for (Goo.Blob b : drops) if (b.r > 0.5f) cv.drawCircle(b.x, b.y, b.r, disc);
        if (touch.r > 0.5f) cv.drawCircle(touch.x, touch.y, touch.r, disc);
        cv.restore();
    }

    private void drawFront(Canvas cv) {
        float R = r0 * press;
        ring.setStrokeWidth(2f * d);
        ring.setColor(0x66FFF6D0);
        cv.drawCircle(cx, cy, R * 0.93f, ring);
        text.setTextSize(R * 0.34f);
        cv.drawText("صلِّ على", cx, cy - R * 0.05f, text);
        text.setTextSize(R * 0.40f);
        cv.drawText("النبي", cx, cy + R * 0.38f, text);
        text.setTextSize(R * 0.16f);
        cv.drawText("اضغط للعدّ", cx, cy - R * 0.45f, text);
    }

    // ----------------------------------------------------------------- لمس

    private void animatePress(float to, boolean overshoot) {
        if (pressAnim != null) pressAnim.cancel();
        pressAnim = ValueAnimator.ofFloat(press, to);
        pressAnim.setDuration(overshoot ? 420 : 120);
        if (overshoot) pressAnim.setInterpolator(new OvershootInterpolator(3f));
        pressAnim.addUpdateListener(a -> press = (float) a.getAnimatedValue());
        pressAnim.start();
    }

    private void burst() {
        if (burstAnim != null) burstAnim.cancel();
        burstAnim = ValueAnimator.ofFloat(0f, 1f);
        burstAnim.setDuration(750);
        burstAnim.addUpdateListener(a -> burst = (float) a.getAnimatedValue());
        burstAnim.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touching = true;
                touchX = e.getX();
                touchY = e.getY();
                animatePress(0.94f, false);
                break;
            case MotionEvent.ACTION_MOVE:
                touchX = e.getX();
                touchY = e.getY();
                break;
            case MotionEvent.ACTION_UP:
                touching = false;
                splash();
                animatePress(1f, true);
                burst();
                break;
            case MotionEvent.ACTION_CANCEL:
                touching = false;
                animatePress(1f, true);
                break;
            default:
                break;
        }
        return super.onTouchEvent(e);
    }
}
