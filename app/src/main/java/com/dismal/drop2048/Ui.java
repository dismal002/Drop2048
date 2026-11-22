package com.dismal.drop2048;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dismal.drop2048.core.Themes;

/** Small view-building helpers (everything is built in code so it can follow the active theme). */
final class Ui {
    private Ui() {}

    static int dp(Context c, float v) { return Math.round(v * c.getResources().getDisplayMetrics().density); }

    static GradientDrawable rounded(Context c, int color, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(c, radiusDp));
        return d;
    }

    static StateListDrawable pressable(Context c, int normal, int pressed, float radiusDp) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_pressed}, rounded(c, pressed, radiusDp));
        s.addState(new int[]{}, rounded(c, normal, radiusDp));
        return s;
    }

    static TextView text(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        t.setIncludeFontPadding(false);
        return t;
    }

    /** Standard menu button ("soft" look) or primary look. */
    static TextView button(Context c, String label, Themes.Theme th, boolean primary) {
        TextView b = text(c, label, primary ? 17 : 14, primary ? th.buttonFg : th.textStrong, true);
        b.setGravity(Gravity.CENTER);
        int padH = dp(c, primary ? 24 : 16), padV = dp(c, primary ? 12 : 9);
        b.setPadding(padH, padV, padH, padV);
        b.setBackground(primary ? pressable(c, th.button, th.buttonHover, 6) : pressable(c, th.soft, th.softHover, 6));
        b.setMinWidth(dp(c, primary ? 160 : 110));
        b.setClickable(true);
        b.setFocusable(false);
        return b;
    }

    static TextView link(Context c, String label, Themes.Theme th) {
        TextView b = text(c, label, 14, th.text, true);
        b.setPaintFlags(b.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        b.setPadding(dp(c, 10), dp(c, 6), dp(c, 10), dp(c, 6));
        b.setClickable(true);
        b.setFocusable(false);
        b.setGravity(Gravity.CENTER);
        return b;
    }

    /** Square icon button with a tinted vector icon. */
    static FrameLayout iconButton(Context c, int iconRes, int tint, int bg, int bgPressed, float sizeDp, float iconDp, float radiusDp) {
        FrameLayout f = new FrameLayout(c);
        f.setBackground(pressable(c, bg, bgPressed, radiusDp));
        f.setClickable(true);
        f.setFocusable(false);
        ImageView i = icon(c, iconRes, tint, iconDp);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(c, iconDp), dp(c, iconDp), Gravity.CENTER);
        f.addView(i, lp);
        f.setMinimumWidth(dp(c, sizeDp));
        f.setMinimumHeight(dp(c, sizeDp));
        return f;
    }

    static ImageView icon(Context c, int res, int tint, float sizeDp) {
        ImageView i = new ImageView(c);
        i.setImageResource(res);
        i.setColorFilter(tint);
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return i;
    }

    static ImageView iconOf(View iconButton) { return (ImageView) ((FrameLayout) iconButton).getChildAt(0); }

    static LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }

    static LinearLayout.LayoutParams weight(Context c, float w, int heightPx, int marginDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, heightPx, w);
        p.setMargins(dp(c, marginDp), 0, dp(c, marginDp), 0);
        return p;
    }

    /** Small rounded tile (rules list, leaderboard badge). */
    static TextView miniTile(Context c, String label, int bg, int fg, float sizeDp, float sp) {
        TextView t = text(c, label, sp, fg, true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(rounded(c, bg, 5));
        t.setMinWidth(dp(c, sizeDp));
        t.setMinHeight(dp(c, sizeDp));
        return t;
    }

    static int alpha(int color, float a) { return Color.argb(Math.round(a * 255), Color.red(color), Color.green(color), Color.blue(color)); }
}
