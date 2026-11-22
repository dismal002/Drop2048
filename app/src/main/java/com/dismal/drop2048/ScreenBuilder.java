package com.dismal.drop2048;

import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dismal.drop2048.core.GameEngine;
import com.dismal.drop2048.core.GameEngine.Mode;
import com.dismal.drop2048.core.I18n;
import com.dismal.drop2048.core.Themes;


/** Builds the panels shown over the board: start, help, theme, game over and the daily result. */
final class ScreenBuilder {
    private final MainActivity a;
    private final GameEngine engine;
    private Themes.Theme th;

    /** Button that Enter/Space activates on the current panel. */
    View primary;
    ScrollView scroll;


    ScreenBuilder(MainActivity activity, GameEngine engine) {
        this.a = activity;
        this.engine = engine;
    }

    private int dp(float v) { return Ui.dp(a, v); }

    private static String modeName(Mode m) { return I18n.t(m.key); }

    View build(String name) {
        th = Themes.current;
        primary = null;
        LinearLayout col = new LinearLayout(a);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(dp(4), dp(4), dp(4), dp(4));
        switch (name) {
            case "start": start(col); break;
            case "help": help(col); break;
            case "theme": theme(col); break;
            case "settings": settings(col); break;
            case "about": about(col); break;
            case "over": over(col); break;
            case "dailyDone": dailyDone(col); break;
            default: break;
        }
        scroll = new ScrollView(a);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        // Center short content, scroll long content.
        FrameWrap wrap = new FrameWrap(a, col);
        scroll.addView(wrap, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    /** Vertically centers its child when there is room (ScrollView + fillViewport). */
    private static final class FrameWrap extends LinearLayout {
        FrameWrap(android.content.Context c, View child) {
            super(c);
            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER);
            addView(child, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    // ---- small builders ----
    private TextView title(String s) {
        TextView t = Ui.text(a, s, 26, th.textStrong, true);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private LinearLayout.LayoutParams gap(int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(top);
        return p;
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(a);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER);
        return r;
    }

    private View logoBig() {
        LinearLayout l = row();
        TextView drop = Ui.text(a, "Drop", 40, th.textStrong, true);
        TextView tile = Ui.text(a, "2048", 34, th.logoFg, true);
        tile.setBackground(Ui.rounded(a, th.logo, 8));
        tile.setPadding(dp(9), dp(3), dp(9), dp(4));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2);
        p.leftMargin = dp(8);
        l.addView(drop);
        l.addView(tile, p);
        return l;
    }

    private void onClick(View v, final String action) { v.setOnClickListener(x -> a.action(action)); }

    // ---- Start ----
    private void start(LinearLayout col) {
        col.addView(logoBig());
        TextView tag = Ui.text(a, I18n.t("tagline"), 15, th.text, false);
        tag.setGravity(Gravity.CENTER);
        tag.setLineSpacing(0, 1.15f);
        tag.setMaxWidth(dp(280));
        col.addView(tag, gap(10));

        LinearLayout modes = row();
        modes.addView(modeButton("classic", "classicSub", engine.lastMode == Mode.CLASSIC, "play-classic"), Ui.weight(a, 1, -2, 4));
        modes.addView(modeButton("zen", "zenSub", engine.lastMode == Mode.ZEN, "play-zen"), Ui.weight(a, 1, -2, 4));
        col.addView(modes, new LinearLayout.LayoutParams(-1, -2));
        ((LinearLayout.LayoutParams) modes.getLayoutParams()).topMargin = dp(14);
        primary = modes.getChildAt(engine.lastMode == Mode.ZEN ? 1 : 0);

        // daily challenge: dashed outline
        GameEngine.DailyState ds = engine.dailyState();
        LinearLayout daily = new LinearLayout(a);
        daily.setOrientation(LinearLayout.VERTICAL);
        daily.setGravity(Gravity.CENTER);
        daily.setPadding(dp(12), dp(8), dp(12), dp(8));
        GradientDrawable dd = Ui.rounded(a, th.soft, 6);
        dd.setStroke(dp(2), th.logo, dp(6), dp(4));
        daily.setBackground(dd);
        daily.setClickable(true);
        daily.setFocusable(false);
        daily.setAlpha(ds != null ? 0.8f : 1f);
        String sub = ds == null ? I18n.dailySub(GameEngine.ddmm(GameEngine.todayKey()))
                : ds.done ? I18n.dailyDone(I18n.points(ds.score)) : I18n.t("dailyUsed");
        TextView dn = Ui.text(a, I18n.t("dailyBtn"), 15, th.textStrong, true);
        TextView ds2 = Ui.text(a, sub, 11, th.text, false);
        ds2.setGravity(Gravity.CENTER);
        daily.addView(dn);
        daily.addView(ds2, gap(2));
        onClick(daily, "play-daily");
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(-1, -2);
        dlp.topMargin = dp(10);
        dlp.leftMargin = dp(4);
        dlp.rightMargin = dp(4);
        col.addView(daily, dlp);

        TextView how = Ui.button(a, I18n.t("howTo"), th, false);
        how.setMinWidth(0);
        onClick(how, "help");
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(-1, -2);
        hlp.topMargin = dp(10);
        col.addView(how, hlp);

        TextView look = Ui.link(a, I18n.t("settings"), th);
        onClick(look, "settings");
        col.addView(look, gap(6));
    }

    private View modeButton(String nameKey, String subKey, boolean isPrimary, String action) {
        LinearLayout b = new LinearLayout(a);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(8), dp(11), dp(8), dp(11));
        b.setBackground(isPrimary ? Ui.pressable(a, th.button, th.buttonHover, 6) : Ui.pressable(a, th.soft, th.softHover, 6));
        b.setClickable(true);
        b.setFocusable(false);
        b.addView(Ui.text(a, I18n.t(nameKey), 17, isPrimary ? th.buttonFg : th.textStrong, true));
        TextView sub = Ui.text(a, I18n.t(subKey), 11, isPrimary ? Ui.alpha(th.buttonFg, 0.85f) : th.text, false);
        sub.setGravity(Gravity.CENTER);
        b.addView(sub, gap(2));
        onClick(b, action);
        return b;
    }

    // ---- Help ----
    private void help(LinearLayout col) {
        col.addView(title(I18n.t("howTo")));
        LinearLayout rules = new LinearLayout(a);
        rules.setOrientation(LinearLayout.VERTICAL);
        rules.addView(ruleRow(new View[]{mini("4", 4), mini("4", 4)}, "rule1"));
        rules.addView(ruleRow(new View[]{mini("×2", 16)}, "rule2"));
        rules.addView(ruleRow(new View[]{keyMini("↑")}, "rule3"));
        rules.addView(ruleRow(new View[]{jokerMini()}, "rule4"));
        rules.addView(ruleRow(new View[]{mini("!", 2)}, "rule5"));
        col.addView(rules, gap(12));

        TextView phone = Ui.text(a, I18n.t("phone") + ": " + I18n.t("phoneText"), 12.5f, th.text, false);
        phone.setLineSpacing(0, 1.1f);
        col.addView(phone, gap(12));
        if (a.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS) {
            TextView kb = Ui.text(a, I18n.t("keyboard") + ": " + I18n.t("keyboardText"), 12.5f, th.text, false);
            kb.setLineSpacing(0, 1.1f);
            col.addView(kb, gap(6));
        }


        TextView ok = Ui.button(a, I18n.t("gotIt"), th, true);
        onClick(ok, "back");
        primary = ok;
        col.addView(ok, gap(12));
    }

    private View ruleRow(View[] tiles, String key) {
        LinearLayout r = new LinearLayout(a);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout t = new LinearLayout(a);
        t.setOrientation(LinearLayout.HORIZONTAL);
        for (View v : tiles) {
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(28), dp(28));
            p.rightMargin = dp(3);
            t.addView(v, p);
        }
        r.addView(t, new LinearLayout.LayoutParams(dp(62), -2));
        TextView s = Ui.text(a, I18n.t(key), 13, th.text, false);
        s.setLineSpacing(0, 1.1f);
        r.addView(s, new LinearLayout.LayoutParams(0, -2, 1f));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(8);
        r.setLayoutParams(lp);
        return r;
    }

    private TextView mini(String label, int tileValue) {
        return Ui.miniTile(a, label, th.tileBg(tileValue), th.tileFg(tileValue), 28, 13);
    }

    private TextView keyMini(String label) {
        return Ui.miniTile(a, label, th.soft, th.textStrong, 28, 14);
    }

    private TextView jokerMini() {
        TextView t = Ui.miniTile(a, "\u2605", th.jokerStops[0], th.jokerStar, 28, 14);
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, th.jokerStops.length >= 2 ? th.jokerStops : new int[]{th.jokerStops[0], th.jokerStops[0]});
        g.setCornerRadius(dp(5));
        t.setBackground(g);
        return t;
    }

    // ---- Appearance (colour themes) ----
    private void theme(LinearLayout col) {
        col.addView(title(I18n.t("appearance")));
        // Two cards per row with LinearLayout weights: GridLayout collapses weighted cells to
        // zero width on older Android versions (API 21), which hid the whole list.
        LinearLayout grid = new LinearLayout(a);
        grid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout gridRow = null;
        for (Themes.Theme t : Themes.LIST) {
            if (gridRow == null || gridRow.getChildCount() == 2) {
                gridRow = row();
                gridRow.setBaselineAligned(false);
                grid.addView(gridRow, new LinearLayout.LayoutParams(-1, -2));
            }
            boolean active = t.id.equals(th.id);
            LinearLayout card = new LinearLayout(a);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER);
            card.setPadding(dp(6), dp(8), dp(6), dp(8));
            GradientDrawable bg = Ui.rounded(a, th.soft, 8);
            if (active) bg.setStroke(dp(2), th.logo);
            card.setBackground(bg);
            card.setClickable(true);
            card.setFocusable(false);

            LinearLayout preview = new LinearLayout(a);
            preview.setOrientation(LinearLayout.HORIZONTAL);
            preview.setGravity(Gravity.CENTER);
            preview.setPadding(dp(5), dp(5), dp(5), dp(5));
            preview.setBackground(Ui.rounded(a, t.board, 6));
            int[] vals = {2, 8, 64, GameEngine.JOKER};
            for (int v : vals) {
                View sw = new View(a);
                GradientDrawable g;
                if (v == GameEngine.JOKER) {
                    g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, t.jokerStops.length >= 2 ? t.jokerStops : new int[]{t.jokerStops[0], t.jokerStops[0]});
                } else {
                    g = new GradientDrawable();
                    g.setColor(t.tileBg(v));
                }
                g.setCornerRadius(dp(3));
                if (t.hasBorder) g.setStroke(dp(1.5f), t.border);
                sw.setBackground(g);
                LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(20), dp(20));
                sp.setMargins(dp(2), 0, dp(2), 0);
                preview.addView(sw, sp);
            }
            card.addView(preview);
            TextView name = Ui.text(a, I18n.t("theme." + t.id), 12, th.textStrong, true);
            card.addView(name, gap(5));
            final String id = t.id;
            card.setOnClickListener(x -> a.setThemeId(id));

            LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(0, -2, 1f);
            gp.setMargins(dp(3), dp(3), dp(3), dp(3));
            gridRow.addView(card, gp);
        }
        if (gridRow != null && gridRow.getChildCount() == 1) { // keep the last card in its column
            gridRow.addView(new View(a), new LinearLayout.LayoutParams(0, -2, 1f));
        }
        col.addView(grid, new LinearLayout.LayoutParams(-1, -2));
        ((LinearLayout.LayoutParams) grid.getLayoutParams()).topMargin = dp(10);

        TextView done = Ui.button(a, I18n.t("done"), th, true);
        onClick(done, "back");
        primary = done;
        col.addView(done, gap(10));
    }

    // ---- Settings ----
    private void settings(LinearLayout col) {
        col.addView(title(I18n.t("settings")));

        TextView langLabel = Ui.text(a, I18n.t("language").toUpperCase(), 11, Ui.alpha(th.text, 0.8f), true);
        col.addView(langLabel, gap(12));
        // Three per row so six languages stay readable on a phone.
        LinearLayout tabs = null;
        for (int i = 0; i < I18n.LANGS.length; i++) {
            if (i % 3 == 0) {
                tabs = row();
                col.addView(tabs, new LinearLayout.LayoutParams(-1, -2));
                ((LinearLayout.LayoutParams) tabs.getLayoutParams()).topMargin = dp(6);
            }
            final String lang = I18n.LANGS[i];
            boolean active = lang.equals(I18n.lang());
            TextView t = Ui.text(a, I18n.langName(lang), 14, active ? th.buttonFg : th.textStrong, true);
            t.setGravity(Gravity.CENTER);
            t.setPadding(dp(10), dp(8), dp(10), dp(8));
            t.setSingleLine(true);
            t.setBackground(Ui.rounded(a, active ? th.button : th.soft, 6));
            t.setClickable(true);
            t.setFocusable(false);
            t.setOnClickListener(x -> a.setLang(lang));
            tabs.addView(t, Ui.weight(a, 1, -2, 3));
        }

        toggleRow(col, I18n.t("sound"), a.soundOn(), "sound");
        toggleRow(col, I18n.t("music"), a.musicOn(), "music");
        toggleRow(col, I18n.t("neonTiles"), a.neonTiles(), "neon");

        TextView look = Ui.link(a, I18n.t("appearance"), th);
        onClick(look, "theme");
        col.addView(look, gap(6));

        TextView about = Ui.link(a, I18n.t("about"), th);
        onClick(about, "about");
        col.addView(about, gap(2));

        TextView done = Ui.button(a, I18n.t("done"), th, true);
        onClick(done, "back");
        primary = done;
        col.addView(done, gap(10));
    }

    /** Label + On/Off pill; the whole row toggles the setting. */
    private void toggleRow(LinearLayout col, String label, boolean on, final String action) {
        LinearLayout r = new LinearLayout(a);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable bg = Ui.rounded(a, th.soft, 8);
        if (on) bg.setStroke(dp(2), th.logo);
        r.setBackground(bg);
        r.setClickable(true);
        r.setFocusable(false);
        r.setOnClickListener(x -> a.action(action));
        r.addView(Ui.text(a, label, 15, th.textStrong, true), new LinearLayout.LayoutParams(0, -2, 1f));
        TextView state = Ui.text(a, I18n.t(on ? "on" : "off"), 12, on ? th.buttonFg : th.text, true);
        state.setGravity(Gravity.CENTER);
        state.setPadding(dp(12), dp(5), dp(12), dp(5));
        state.setBackground(Ui.rounded(a, on ? th.button : th.panel, 20));
        r.addView(state);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(8);
        col.addView(r, lp);
    }

    // ---- About ----
    private void about(LinearLayout col) {
        col.addView(logoBig());
        TextView ver = Ui.text(a, I18n.t("version") + " " + a.versionName(), 12, th.text, false);
        ver.setGravity(Gravity.CENTER);
        col.addView(ver, gap(6));

        TextView notice = Ui.text(a, I18n.t("licenseNotice"), 13, th.text, false);
        notice.setGravity(Gravity.CENTER);
        notice.setLineSpacing(0, 1.15f);
        notice.setMaxWidth(dp(280));
        col.addView(notice, gap(16));

        TextView licLabel = Ui.text(a, I18n.t("license").toUpperCase(), 11, Ui.alpha(th.text, 0.8f), true);
        licLabel.setGravity(Gravity.CENTER);
        col.addView(licLabel, gap(4));
        TextView lic = Ui.link(a, "GNU GPL v3.0", th);
        lic.setOnClickListener(x -> a.openUrl(MainActivity.LICENSE_URL));
        col.addView(lic);

        TextView srcLabel = Ui.text(a, I18n.t("sourceCode").toUpperCase(), 11, Ui.alpha(th.text, 0.8f), true);
        srcLabel.setGravity(Gravity.CENTER);
        col.addView(srcLabel, gap(10));
        TextView repo = Ui.link(a, "github.com/dismal002/Drop2048", th);
        repo.setOnClickListener(x -> a.openUrl(MainActivity.REPO_URL));
        col.addView(repo);

        TextView back = Ui.button(a, I18n.t("back"), th, true);
        onClick(back, "back");
        primary = back;
        col.addView(back, gap(12));
    }

    // ---- Game over ----
    private void over(LinearLayout col) {
        Mode m = engine.mode;
        boolean daily = m == Mode.DAILY;
        col.addView(title(I18n.t(daily ? "dailyComplete" : engine.overIsRecord ? "newRecord" : "gameOver")));
        TextView ml = Ui.text(a, daily ? I18n.overDaily(GameEngine.ddmm(engine.dailyDay)) : I18n.overMode(modeName(m)), 13, th.text, true);
        col.addView(ml, gap(2));

        int sec = (int) Math.round(engine.playTime);
        GridLayout sum = new GridLayout(a);
        sum.setColumnCount(2);
        String[][] cells = {
                {I18n.t("score"), String.valueOf(engine.score)},
                {I18n.t("maxTile"), String.valueOf(engine.maxTile)},
                {I18n.t("bestCombo"), engine.bestCombo >= 2 ? "×" + engine.bestCombo : "—"},
                {I18n.t("dropsTime"), engine.drops + " · " + (sec / 60) + ":" + String.format(java.util.Locale.US, "%02d", sec % 60)},
        };
        for (String[] c : cells) {
            LinearLayout cell = new LinearLayout(a);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(dp(6), dp(8), dp(6), dp(8));
            cell.setBackground(Ui.rounded(a, th.panel, 6));
            TextView lab = Ui.text(a, c[0].toUpperCase(), 10, Ui.alpha(th.panelText, 0.85f), true);
            lab.setGravity(Gravity.CENTER);
            cell.addView(lab);
            cell.addView(Ui.text(a, c[1], 18, th.panelText, true), gap(2));
            GridLayout.LayoutParams gp = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f));
            gp.width = 0;
            gp.setMargins(dp(3), dp(3), dp(3), dp(3));
            sum.addView(cell, gp);
        }
        col.addView(sum, new LinearLayout.LayoutParams(-1, -2));
        ((LinearLayout.LayoutParams) sum.getLayoutParams()).topMargin = dp(10);

        LinearLayout r = row();
        TextView again = Ui.button(a, daily ? I18n.playMode(modeName(engine.lastMode)) : I18n.t("again"), th, true);
        again.setMinWidth(0);
        onClick(again, "play");
        primary = again;
        TextView share = Ui.button(a, I18n.t("share"), th, false);
        share.setMinWidth(0);
        onClick(share, "share");
        r.addView(again, Ui.weight(a, 1.4f, -2, 4));
        r.addView(share, Ui.weight(a, 1, -2, 4));
        col.addView(r, new LinearLayout.LayoutParams(-1, -2));
        ((LinearLayout.LayoutParams) r.getLayoutParams()).topMargin = dp(6);
    }

    // ---- Daily result (stored on the device) ----
    private void dailyDone(LinearLayout col) {
        col.addView(title(I18n.t("dailyComplete")));
        GameEngine.DailyState ds = engine.dailyState();
        String line = ds == null ? I18n.t("dailyUsed") : I18n.dailyDone(I18n.points(ds.score));
        TextView p = Ui.text(a, line, 14, th.text, false);
        p.setGravity(Gravity.CENTER);
        col.addView(p, gap(8));
        if (ds != null) {
            TextView tile = Ui.text(a, I18n.t("maxTile") + ": " + ds.maxTile, 13, th.text, true);
            tile.setGravity(Gravity.CENTER);
            col.addView(tile, gap(4));
        }
        TextView back = Ui.button(a, I18n.t("back"), th, true);
        onClick(back, "back");
        primary = back;
        col.addView(back, gap(12));
    }
}
