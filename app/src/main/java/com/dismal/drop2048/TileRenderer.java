package com.dismal.drop2048;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.dismal.drop2048.core.GameEngine;
import com.dismal.drop2048.core.Themes;

import java.util.HashMap;
import java.util.Map;

final class TileRenderer {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path path = new Path();
    private final Context ctx;

    /** When true, tiles come from the neon vector art instead of the theme colours. */
    private static boolean neon;
    private static final Map<Long, Neon> neonCache = new HashMap<>();

    /** A rendered neon tile plus the bounds of its actual artwork inside the transparent canvas. */
    private static final class Neon {
        final Bitmap bmp;
        final int left, top, right, bottom;

        Neon(Bitmap b, int l, int t, int r, int bo) {
            bmp = b; left = l; top = t; right = r; bottom = bo;
        }
    }

    TileRenderer(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        stroke.setStyle(Paint.Style.STROKE);
        text.setTextAlign(Paint.Align.CENTER);
    }

    static void setNeon(boolean on) {
        neon = on;
        neonCache.clear();
    }

    private static int withAlpha(int color, float alpha) {
        int a = Math.round(((color >>> 24) & 255) * alpha);
        return (a << 24) | (color & 0xFFFFFF);
    }

    /** (px, py) top-left of the cell, size = tile edge, scale = pop animation, alpha = fade (ghosts). */
    void draw(Canvas c, Themes.Theme th, int v, float px, float py, float size, float scale, float alpha) {
        if (neon && drawNeon(c, v, px, py, size, scale, alpha)) return;

        float s = size * scale;
        float x = px + (size - s) / 2f;
        float y = py + (size - s) / 2f;
        float r = s * th.radius;
        boolean joker = v == GameEngine.JOKER;
        int bg = joker ? th.jokerStops[0] : th.tileBg(v);
        int fg = joker ? th.jokerStar : th.tileFg(v);

        if (th.modern) { // flat modern tiles float on a soft drop shadow
            fill.setShader(null);
            float off = s * 0.055f;
            for (int k = 3; k >= 1; k--) {
                float e = s * 0.04f * k;
                fill.setColor(Math.round(10 * alpha) << 24);
                rect.set(x - e, y + off - e, x + s + e, y + s + off + e);
                c.drawRoundRect(rect, r + e, r + e, fill);
            }
        }

        if (th.glow) {
            fill.setShader(null);
            for (int k = 4; k >= 1; k--) {
                float e = s * 0.22f * k / 4f;
                fill.setColor(withAlpha(bg, 0.10f * alpha));
                rect.set(x - e, y - e, x + s + e, y + s + e);
                c.drawRoundRect(rect, r + e, r + e, fill);
            }
        }

        rect.set(x, y, x + s, y + s);
        if (joker) {
            int n = th.jokerStops.length;
            float[] pos = new float[n];
            for (int i = 0; i < n; i++) pos[i] = n == 1 ? 0f : i / (float) (n - 1);
            fill.setShader(new LinearGradient(x, y, x + s, y + s, th.jokerStops, pos, Shader.TileMode.CLAMP));
            fill.setColor(0xFF000000);
        } else {
            fill.setShader(null);
            fill.setColor(bg);
        }
        fill.setAlpha(Math.round(255 * alpha));
        c.drawRoundRect(rect, r, r, fill);
        fill.setShader(null);

        if (th.bevel) { // light top/left edges, dark bottom/right — like classic Tetris blocks
            float b = s * 0.1f;
            fill.setColor(withAlpha(0x59FFFFFF, alpha));
            path.rewind();
            path.moveTo(x, y); path.lineTo(x + s, y); path.lineTo(x + s - b, y + b);
            path.lineTo(x + b, y + b); path.lineTo(x + b, y + s - b); path.lineTo(x, y + s);
            path.close();
            c.drawPath(path, fill);
            fill.setColor(withAlpha(0x47000000, alpha));
            path.rewind();
            path.moveTo(x + s, y + s); path.lineTo(x, y + s); path.lineTo(x + b, y + s - b);
            path.lineTo(x + s - b, y + s - b); path.lineTo(x + s - b, y + b); path.lineTo(x + s, y);
            path.close();
            c.drawPath(path, fill);
        }

        if (th.hasBorder) {
            float w = Math.max(2f, s * 0.045f);
            stroke.setStrokeWidth(w);
            stroke.setColor(withAlpha(th.border, alpha));
            rect.set(x + w / 2f, y + w / 2f, x + s - w / 2f, y + s - w / 2f);
            c.drawRoundRect(rect, r, r, stroke);
        }

        text.setTypeface(th.mono ? Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) : Typeface.DEFAULT_BOLD);
        text.setColor(withAlpha(fg, alpha));
        String label;
        float fs;
        if (joker) {
            label = "\u2605";
            fs = s * 0.6f;
        } else {
            label = String.valueOf(v);
            int digits = label.length();
            float k = th.modern ? 1.06f : 1f;
            fs = s * (digits <= 2 ? 0.48f : digits == 3 ? 0.4f : digits == 4 ? 0.32f : 0.26f) * k;
        }
        text.setTextSize(fs);
        Paint.FontMetrics fm = text.getFontMetrics();
        float cy = y + s / 2f + (joker ? s * 0.03f : fs * 0.04f) - (fm.ascent + fm.descent) / 2f;
        c.drawText(label, x + s / 2f, cy, text);
    }

    /**
     * Neon art: one bitmap per (value, size). The vectors are drawn in a 184-unit box with a wide
     * transparent margin, so the visible artwork bounds are used to scale the tile to the cell.
     * Returns false if it could not be drawn.
     */
    private boolean drawNeon(Canvas c, int v, float px, float py, float size, float scale, float alpha) {
        Matrix m = c.getMatrix();
        float k = Math.max(1f, m.mapRadius(1f));
        int n = Math.max(24, Math.min(512, Math.round(size * k)));
        Neon art = neonBitmap(neonRes(v), n);
        if (art == null) return false;

        float s = size * scale;
        float x = px + (size - s) / 2f;
        float y = py + (size - s) / 2f;
        int n2 = art.bmp.getWidth();
        float fw = (art.right - art.left) / (float) n2;
        float fh = (art.bottom - art.top) / (float) n2;
        float fcx = (art.left + art.right) / 2f / n2;
        float fcy = (art.top + art.bottom) / 2f / n2;
        float side = s / Math.max(0.01f, Math.max(fw, fh)); // artwork fills the cell, glow may spill
        float cx = x + s / 2f;
        float cy = y + s / 2f;
        float dl = cx - fcx * side;
        float dt = cy - fcy * side;

        fill.setShader(null);
        fill.setFilterBitmap(true);
        fill.setAlpha(Math.round(255 * Math.max(0f, Math.min(1f, alpha))));
        rect.set(dl, dt, dl + side, dt + side);
        c.drawBitmap(art.bmp, null, rect, fill);
        fill.setAlpha(255);
        return true;
    }

    private Neon neonBitmap(int res, int n) {
        long key = ((long) res << 10) | (n & 1023);
        Neon a = neonCache.get(key);
        if (a != null) return a;
        try {
            Drawable d = ctx.getDrawable(res);
            if (d == null) return null;
            Bitmap b = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888);
            Canvas bc = new Canvas(b);
            d.setBounds(0, 0, n, n);
            d.draw(bc);

            int l = n, t = n, r = -1, bo = -1;
            int[] px = new int[n * n];
            b.getPixels(px, 0, n, 0, 0, n, n);
            for (int y = 0; y < n; y++) {
                int row = y * n;
                for (int x = 0; x < n; x++) {
                    if ((px[row + x] >>> 24) > 170) { // ignore the faint glow, keep the solid artwork
                        if (x < l) l = x;
                        if (x > r) r = x;
                        if (y < t) t = y;
                        if (y > bo) bo = y;
                    }
                }
            }
            if (r < l || bo < t) { l = 0; t = 0; r = n - 1; bo = n - 1; }
            a = new Neon(b, l, t, r + 1, bo + 1);
            neonCache.put(key, a);
            return a;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static int neonRes(int v) {
        switch (v) {
            case 2: return R.drawable.neon_tile_2;
            case 4: return R.drawable.neon_tile_4;
            case 8: return R.drawable.neon_tile_8;
            case 16: return R.drawable.neon_tile_16;
            case 32: return R.drawable.neon_tile_32;
            case 64: return R.drawable.neon_tile_64;
            case 128: return R.drawable.neon_tile_128;
            case 256: return R.drawable.neon_tile_256;
            case 512: return R.drawable.neon_tile_512;
            case 1024: return R.drawable.neon_tile_1k;
            case 2048: return R.drawable.neon_tile_2k;
            case 4096: return R.drawable.neon_tile_4k;
            case 8192: return R.drawable.neon_tile_8k;
            case 16384: return R.drawable.neon_tile_16k;
            case 32768: return R.drawable.neon_tile_32k;
            case 65536: return R.drawable.neon_tile_64k;
            case 131072: return R.drawable.neon_tile_128k;
            default: return v > 262144 ? R.drawable.neon_tile_256k : R.drawable.neon_tile_unknown;
        }
    }
}
