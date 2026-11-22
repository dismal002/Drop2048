package com.dismal.drop2048;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.Choreographer;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.dismal.drop2048.core.GameEngine;
import com.dismal.drop2048.core.GameEngine.Falling;
import com.dismal.drop2048.core.GameEngine.Floater;
import com.dismal.drop2048.core.GameEngine.Ghost;
import com.dismal.drop2048.core.GameEngine.Particle;
import com.dismal.drop2048.core.GameEngine.Tile;
import com.dismal.drop2048.core.Themes;

import java.util.Random;

final class GameView extends View implements Choreographer.FrameCallback {
    static final int CELL = 84;
    static final int PAD = 3;
    static final int TILE = CELL - PAD * 2;
    static final int W = GameEngine.COLS * CELL;
    static final int H = GameEngine.ROWS * CELL;

    private final GameEngine engine;
    private final TileRenderer tiles;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path clip = new Path();
    private final Random rnd = new Random();

    private boolean running;
    private long lastNanos;
    private int touchCol = -1;

    GameView(Context ctx, GameEngine engine) {
        super(ctx);
        this.engine = engine;
        tiles = new TileRenderer(ctx);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        textPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    // ---- Frame loop ----
    void start() {
        if (running) return;
        running = true;
        lastNanos = 0;
        Choreographer.getInstance().postFrameCallback(this);
    }

    void stop() {
        running = false;
        Choreographer.getInstance().removeFrameCallback(this);
    }

    @Override public void doFrame(long frameTimeNanos) {
        if (!running) return;
        double dt = lastNanos == 0 ? 0 : Math.min(0.05, (frameTimeNanos - lastNanos) / 1e9);
        lastNanos = frameTimeNanos;
        engine.frame(dt);
        invalidate();
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    // ---- Input: no buttons, just the board ----
    /** Finger x → column index (may be outside 0..COLS-1, the engine clamps it). */
    private int columnAt(float x) {
        float k = getWidth() / (float) W;
        return (int) Math.floor(x / (k * CELL));
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (getWidth() == 0 || engine.falling == null) return false;
        if (engine.phase != GameEngine.Phase.PLAYING || engine.overlay != GameEngine.Overlay.NONE) return false;
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                touchCol = columnAt(e.getX());
                engine.moveTo(touchCol);
                performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                int c = columnAt(e.getX());
                if (c != touchCol) {
                    touchCol = c;
                    engine.moveTo(c);
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
                engine.drop(); // lift the finger → the block falls
                touchCol = -1;
                return true;
            case MotionEvent.ACTION_CANCEL:
                touchCol = -1;
                return true;
            default:
                return true;
        }
    }

    // ---- Drawing ----
    private void cellXY(double col, double row, float[] out) {
        out[0] = (float) (col * CELL + PAD);
        out[1] = (float) (row * CELL + PAD);
    }

    private final float[] xy = new float[2];

    private static int withAlpha(int color, float a) {
        int alpha = Math.round(((color >>> 24) & 255) * Math.max(0f, Math.min(1f, a)));
        return (alpha << 24) | (color & 0xFFFFFF);
    }

    @Override protected void onDraw(Canvas canvas) {
        if (getWidth() == 0) return;
        Themes.Theme th = Themes.current;
        float k = getWidth() / (float) W;
        canvas.save();
        canvas.scale(k, k);

        float boardRadius = th.modern ? 18 : 8;
        rect.set(0, 0, W, H);
        clip.rewind();
        clip.addRoundRect(rect, boardRadius, boardRadius, Path.Direction.CW);
        canvas.clipPath(clip);

        if (engine.shake > 0) {
            canvas.translate((float) ((rnd.nextDouble() - 0.5) * engine.shake), (float) ((rnd.nextDouble() - 0.5) * engine.shake));
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        paint.setShader(null);
        paint.setColor(th.board);
        canvas.drawRect(-20, -20, W + 20, H + 20, paint);

        Falling falling = engine.falling;
        boolean active = falling != null && engine.phase == GameEngine.Phase.PLAYING
                && engine.overlay == GameEngine.Overlay.NONE;

        if (active) { // the whole column glows in the block's colour, like the demo
            int tint = falling.v == GameEngine.JOKER ? th.jokerStops[0] : th.tileBg(falling.v);
            paint.setColor(withAlpha(tint, 0.16f));
            rect.set(falling.c * CELL + PAD, 0, falling.c * CELL + PAD + TILE, H);
            canvas.drawRoundRect(rect, th.modern ? 9 : 7, th.modern ? 9 : 7, paint);
        }

        for (Ghost g : engine.ghosts) { // tiles being pulled into a merge
            double x = g.x + (g.tx - g.x) * g.t;
            double y = g.y + (g.ty - g.y) * g.t;
            cellXY(x, y, xy);
            tiles.draw(canvas, th, g.v, xy[0], xy[1], TILE, (float) (1 - g.t * 0.3), (float) (1 - g.t));
        }

        for (Tile[] row : engine.grid) {
            for (Tile t : row) {
                if (t == null) continue;
                cellXY(t.x, t.y, xy);
                tiles.draw(canvas, th, t.v, xy[0], xy[1], TILE, (float) (1 + 0.18 * Math.sin(t.pop * Math.PI)), 1f);
            }
        }

        if (falling != null) {
            cellXY(falling.x, falling.vy, xy);
            if (falling.dropping) dropTrail(canvas, th, falling, xy[0]);
            tiles.draw(canvas, th, falling.v, xy[0], xy[1], TILE, (float) (1 + 0.15 * Math.sin(falling.pop * Math.PI)), 1f);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
        for (Particle p : engine.particles) {
            float a = (float) (1 - p.life / p.ttl);
            paint.setColor(p.color);
            paint.setAlpha(Math.round(255 * Math.max(0, a)));
            float s = (float) (p.size * (1 - (p.life / p.ttl) * 0.5));
            float px = (float) (p.x * CELL), py = (float) (p.y * CELL);
            canvas.drawRect(px - s / 2, py - s / 2, px + s / 2, py + s / 2, paint);
        }
        paint.setAlpha(255);

        for (Floater f : engine.floats) {
            float size = f.big ? 38 : 24;
            float x = (float) (f.x * CELL);
            float y = (float) ((f.y - f.t * 0.8) * CELL);
            drawOutlinedText(canvas, f.text, x, y, size, 5,
                    th.floatStroke, f.gold ? th.floatRecord : f.big ? th.floatCombo : th.floatText, (float) (1 - f.t));
        }

        if (engine.overlay == GameEngine.Overlay.COUNTDOWN) {
            int n = (int) Math.ceil(engine.countdownLeft);
            double frac = engine.countdownLeft - Math.floor(engine.countdownLeft); // 1 → 0 within a second
            drawOutlinedText(canvas, String.valueOf(n), W / 2f, H / 2f, (float) (150 + 50 * frac), 10,
                    th.floatStroke, th.floatCombo, (float) (0.35 + 0.6 * frac));
        }
        canvas.restore();
    }

    /** The light beam the block leaves behind while it falls (demo's drop effect). */
    private void dropTrail(Canvas canvas, Themes.Theme th, Falling f, float left) {
        float top = (float) (f.vy * CELL + PAD);
        if (top < 8) return;
        int tint = f.v == GameEngine.JOKER ? th.jokerStops[0] : th.tileBg(f.v);
        float cx = left + TILE / 2f;
        // Solid slices instead of a gradient: no banding/dithering on old GPUs.
        int slices = 10;
        float h = top / slices;
        for (int i = 0; i < slices; i++) {
            float t = (i + 1) / (float) slices; // 0 at the top → 1 at the block
            paint.setColor(withAlpha(tint, 0.55f * t * t));
            rect.set(cx - TILE * 0.40f, i * h, cx + TILE * 0.40f, (i + 1) * h + 1);
            canvas.drawRect(rect, paint);
            paint.setColor(withAlpha(0xFFFFFFFF, 0.30f * t * t * t));
            rect.set(cx - TILE * 0.09f, i * h, cx + TILE * 0.09f, (i + 1) * h + 1);
            canvas.drawRect(rect, paint);
        }
    }

    private void drawOutlinedText(Canvas canvas, String s, float x, float y, float size, float strokeW,
                                  int strokeColor, int fillColor, float alpha) {
        textPaint.setTextSize(size);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float cy = y - (fm.ascent + fm.descent) / 2f;
        int a = Math.round(255 * Math.max(0f, Math.min(1f, alpha)));
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(strokeW);
        textPaint.setColor(strokeColor);
        textPaint.setAlpha(Math.round(a * ((strokeColor >>> 24) / 255f)));
        canvas.drawText(s, x, cy, textPaint);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(fillColor);
        textPaint.setAlpha(a);
        canvas.drawText(s, x, cy, textPaint);
    }
}
