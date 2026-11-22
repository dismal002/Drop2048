package com.dismal.drop2048;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dismal.drop2048.core.GameEngine;
import com.dismal.drop2048.core.GameEngine.Mode;
import com.dismal.drop2048.core.I18n;
import com.dismal.drop2048.core.Themes;

import java.util.ArrayList;
import java.util.Locale;

public final class MainActivity extends Activity implements GameEngine.Listener {
    static final String REPO_URL = "https://github.com/dismal002/Drop2048";
    static final String LICENSE_URL = "https://www.gnu.org/licenses/gpl-3.0.html";
    private Store store;
    private GameEngine engine;
    private SoundFx sound;
    private LofiMusic music;
    private Vibrator vibrator;
    private ScreenBuilder screens;
    private Themes.Theme th;
    private boolean neon;

    // views
    private FrameLayout root;
    private LinearLayout header, column;
    private FrameLayout boardFrame, bottom, overlay;
    private LinearLayout hintBar, pauseBar;
    private GameView gameView;
    private NextView nextView;
    private LinearLayout nextBox;
    private TextView scoreText, bestText;
    private View pauseBtn;
    private View soundBtn, musicBtn;

    // navigation
    private String currentScreen;
    private final ArrayList<String> screenStack = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int overToken;
    private int lastBoardW = -1, lastRootW = -1, lastRootH = -1;

    // ---- Lifecycle ----
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        store = new Store(this);
        I18n.set(I18n.detect(store.get("drop2048-lang"), Locale.getDefault()));
        if (store.get("drop2048-ui") == null) { // one-time switch to the modern look
            store.set("drop2048-theme", "aurora");
            store.set("drop2048-ui", "1");
        }
        Themes.set(store.get("drop2048-theme"));
        neon = "1".equals(store.get("drop2048-neon"));
        TileRenderer.setNeon(neon);
        sound = new SoundFx(this, store);
        music = new LofiMusic(store);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        engine = new GameEngine(store, this);
        screens = new ScreenBuilder(this, engine);
        buildUi();
        setScreen("start");
    }

    @Override protected void onResume() {
        super.onResume();
        gameView.start();
        music.start();
    }

    @Override protected void onPause() {
        isResumedFlag = false;
        pause();
        gameView.stop();
        music.stop();
        super.onPause();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        music.stop();
        sound.release();
        super.onDestroy();
    }

    // ---- UI construction ----
    private int dp(float v) { return Ui.dp(this, v); }

    Store store() { return store; }

    private void buildUi() {
        th = Themes.current;
        if (gameView != null) gameView.stop();

        Window w = getWindow();
        w.setStatusBarColor(th.bg);
        w.setNavigationBarColor(th.bg);
        int flags = w.getDecorView().getSystemUiVisibility();
        int light = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | (Build.VERSION.SDK_INT >= 26 ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : 0);
        w.getDecorView().setSystemUiVisibility(th.isLight() ? (flags | light) : (flags & ~light));

        root = new FrameLayout(this);
        root.setBackgroundColor(th.bg);
        column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        column.setPadding(dp(16), dp(12), dp(16), dp(12));

        header = buildHeader();
        column.addView(header, new LinearLayout.LayoutParams(-1, -2));

        boardFrame = new FrameLayout(this);
        gameView = new GameView(this, engine);
        boardFrame.addView(gameView, new FrameLayout.LayoutParams(-1, -1));
        overlay = new FrameLayout(this);
        overlay.setBackground(Ui.rounded(this, th.screen, 8));
        overlay.setPadding(dp(12), dp(12), dp(12), dp(12));
        overlay.setVisibility(View.GONE);
        overlay.setClickable(true); // swallow touches while a panel is open
        boardFrame.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(-1, 0, 1f);
        bl.topMargin = dp(10);
        bl.bottomMargin = dp(10);
        bl.gravity = Gravity.CENTER_HORIZONTAL;
        column.addView(boardFrame, bl);

        bottom = new FrameLayout(this);
        hintBar = buildHint();
        pauseBar = buildPauseBar();
        bottom.addView(hintBar, new FrameLayout.LayoutParams(-1, -2));
        bottom.addView(pauseBar, new FrameLayout.LayoutParams(-1, -2));
        column.addView(bottom, new LinearLayout.LayoutParams(-1, -2));

        root.addView(column, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        nextView.bind(engine);
        lastBoardW = -1;
        root.addOnLayoutChangeListener((v, l, t, r, bt, ol, ot, or, ob) -> relayout(r - l, bt - t));
        updateHud();
        updateToggleIcons();
        if (isResumed()) gameView.start();
    }

    private boolean isResumedFlag;

    private boolean isResumed() { return isResumedFlag; }

    @Override protected void onPostResume() {
        super.onPostResume();
        isResumedFlag = true;
    }

    private void relayout(int rootW, int rootH) {
        if (rootW == lastRootW && rootH == lastRootH && lastBoardW > 0) return;
        lastRootW = rootW;
        lastRootH = rootH;
        int availW = rootW - dp(32);
        int byHeight = (int) ((rootH - dp(150)) * 0.625f);
        int boardW = Math.max(dp(200), Math.min(Math.min(availW, dp(560)), byHeight));
        int boardH = boardW * 8 / 5;
        int barW = Math.max(boardW, Math.min(availW, dp(340)));
        if (boardW == lastBoardW) return;
        lastBoardW = boardW;
        LinearLayout.LayoutParams bp = (LinearLayout.LayoutParams) boardFrame.getLayoutParams();
        bp.width = boardW;
        bp.height = boardH;
        bp.weight = 0;
        LinearLayout.LayoutParams hp = (LinearLayout.LayoutParams) header.getLayoutParams();
        hp.width = barW;
        LinearLayout.LayoutParams fp = (LinearLayout.LayoutParams) bottom.getLayoutParams();
        fp.width = barW;
        column.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        column.requestLayout();
    }

    private LinearLayout buildHeader() {
        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL);

        pauseBtn = Ui.iconButton(this, R.drawable.ic_pause, th.textStrong, th.soft, th.softHover, 40, 22, 12);
        pauseBtn.setOnClickListener(v -> action("pause"));
        h.addView(pauseBtn, new LinearLayout.LayoutParams(dp(40), dp(40)));

        View spacer = new View(this);
        h.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));

        scoreText = new TextView(this);
        bestText = new TextView(this);
        h.addView(statBox(I18n.t("score"), scoreText), statLp(0));
        h.addView(statBox(I18n.t("best"), bestText), statLp(6));

        nextBox = new LinearLayout(this);
        nextBox.setOrientation(LinearLayout.VERTICAL);
        nextBox.setGravity(Gravity.CENTER);
        nextBox.setPadding(dp(8), dp(5), dp(8), dp(4));
        nextBox.setBackground(Ui.rounded(this, th.panel, 12));
        TextView nl = Ui.text(this, I18n.t("next").toUpperCase(), 10.5f, Ui.alpha(th.panelText, 0.85f), true);
        nextView = new NextView(this);
        TextView nh = Ui.text(this, I18n.t("swapHintTouch"), 9.5f, Ui.alpha(th.panelText, 0.8f), true);
        nextBox.addView(nl);
        LinearLayout.LayoutParams nv = new LinearLayout.LayoutParams(dp(44), dp(44));
        nv.topMargin = dp(3);
        nv.bottomMargin = dp(3);
        nextBox.addView(nextView, nv);
        nextBox.addView(nh);
        nextBox.setMinimumWidth(dp(76));
        nextBox.setClickable(true);
        nextBox.setFocusable(false);
        nextBox.setOnTouchListener((v, e) -> {
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                vibrate(new int[]{20});
                swap();
            }
            return true;
        });
        h.addView(nextBox, statLp(6));
        return h;
    }

    private LinearLayout.LayoutParams statLp(int leftDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2);
        p.leftMargin = dp(leftDp);
        return p;
    }

    private View statBox(String label, TextView value) {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER);
        b.setMinimumWidth(dp(58));
        b.setMinimumHeight(dp(56));
        b.setPadding(dp(8), dp(4), dp(8), dp(4));
        b.setBackground(Ui.rounded(this, th.panel, 12));
        b.addView(Ui.text(this, label.toUpperCase(), 10.5f, Ui.alpha(th.panelText, 0.85f), true));
        value.setTextSize(18);
        value.setTextColor(th.panelText);
        value.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        value.setIncludeFontPadding(false);
        b.addView(value);
        return b;
    }

    private LinearLayout buildHint() {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.HORIZONTAL);
        p.setGravity(Gravity.CENTER);
        p.setPadding(0, dp(6), 0, dp(4));
        TextView t = Ui.text(this, I18n.t("hint"), 12, Ui.alpha(th.text, 0.9f), true);
        t.setGravity(Gravity.CENTER);
        p.addView(t);
        return p;
    }

    private LinearLayout buildPauseBar() {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.HORIZONTAL);
        p.setVisibility(View.GONE);

        LinearLayout resume = new LinearLayout(this);
        resume.setOrientation(LinearLayout.HORIZONTAL);
        resume.setGravity(Gravity.CENTER);
        resume.setBackground(Ui.pressable(this, th.button, th.buttonHover, 12));
        resume.setClickable(true);
        resume.setFocusable(false);
        resume.addView(Ui.icon(this, R.drawable.ic_play, th.buttonFg, 22), new LinearLayout.LayoutParams(dp(22), dp(22)));
        TextView rl = Ui.text(this, I18n.t("resume"), 16, th.buttonFg, true);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-2, -2);
        rlp.leftMargin = dp(6);
        resume.addView(rl, rlp);
        resume.setOnClickListener(v -> action("resume"));
        p.addView(resume, pbLp(3f, 0));

        View restart = pbButton(R.drawable.ic_restart, "restart", "restart");
        restart.setTag("restart");
        p.addView(restart, pbLp(1f, 6));
        soundBtn = pbButton(sound.isMuted() ? R.drawable.ic_sound_off : R.drawable.ic_sound_on, "sound", "sound");
        p.addView(soundBtn, pbLp(1f, 6));
        musicBtn = pbButton(music.isEnabled() ? R.drawable.ic_music_on : R.drawable.ic_music_off, "music", "music");
        p.addView(musicBtn, pbLp(1f, 6));
        p.addView(pbButton(R.drawable.ic_settings, "settings", "settings"), pbLp(1f, 6));
        p.addView(pbButton(R.drawable.ic_help, "howTo", "help"), pbLp(1f, 6));
        return p;
    }

    private LinearLayout.LayoutParams pbLp(float weight, int leftDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(56), weight);
        lp.leftMargin = dp(leftDp);
        return lp;
    }

    private View pbButton(int icon, String descKey, final String action) {
        View b = Ui.iconButton(this, icon, th.textStrong, th.soft, th.softHover, 52, 22, 12);
        b.setContentDescription(I18n.t(descKey));
        b.setOnClickListener(v -> action(action));
        return b;
    }

    private void updateToggleIcons() {
        if (soundBtn != null) Ui.iconOf(soundBtn).setImageResource(sound.isMuted() ? R.drawable.ic_sound_off : R.drawable.ic_sound_on);
        if (musicBtn != null) Ui.iconOf(musicBtn).setImageResource(music.isEnabled() ? R.drawable.ic_music_on : R.drawable.ic_music_off);
    }

    private void setScreen(String name) {
        currentScreen = name;
        boolean panel = name != null && !name.equals("pause");
        boolean paused = "pause".equals(name);
        if (name != null && panel) {
            View content = screens.build(name);
            overlay.removeAllViews();
            overlay.addView(content, new FrameLayout.LayoutParams(-1, -1));
            if (overlay.getVisibility() != View.VISIBLE) {
                overlay.setAlpha(0f);
                overlay.setVisibility(View.VISIBLE);
                overlay.animate().alpha(1f).setDuration(180).start();
            }
        } else {
            overlay.setVisibility(View.GONE);
            overlay.removeAllViews();
        }
        hintBar.setVisibility(paused ? View.GONE : View.VISIBLE);
        pauseBar.setVisibility(paused ? View.VISIBLE : View.GONE);
        View restart = pauseBar.findViewWithTag("restart");
        if (restart != null) restart.setVisibility(engine.mode == Mode.DAILY ? View.GONE : View.VISIBLE);
        Ui.iconOf(pauseBtn).setImageResource(paused ? R.drawable.ic_play : R.drawable.ic_pause);
    }

    private void openScreen(String name) {
        screenStack.clear();
        setScreen(name);
    }

    private void pushScreen(String name) {
        if (name.equals(currentScreen)) return;
        if (currentScreen != null) screenStack.add(currentScreen);
        setScreen(name);
    }

    private void back() {
        setScreen(screenStack.isEmpty() ? null : screenStack.remove(screenStack.size() - 1));
        if (currentScreen == null && engine.isPaused()) resume();
    }

    private void pause() {
        if (engine.pause()) openScreen("pause");
    }

    private void resume() {
        if (!engine.isPaused()) return;
        engine.resume();
        openScreen(null);
    }

    private void startGame(Mode m) {
        if (!engine.newGame(m)) { // today's daily challenge was already played
            pushScreen("dailyDone");
            return;
        }
        overToken++;
        openScreen(null);
        updateHud();
    }

    void action(String name) {
        if (!name.equals("sound") && !name.equals("neon")) sound.click();
        switch (name) {
            case "play": case "restart": startGame(engine.lastMode); break;
            case "play-classic": startGame(Mode.CLASSIC); break;
            case "play-zen": startGame(Mode.ZEN); break;
            case "play-daily": startGame(Mode.DAILY); break;
            case "share": share(); break;
            case "resume": resume(); break;
            case "back": back(); break;
            case "help": pause(); pushScreen("help"); break;
            case "theme": pause(); pushScreen("theme"); break;
            case "settings": pause(); pushScreen("settings"); break;
            case "about": pushScreen("about"); break;
            case "pause":
                if (engine.isPaused() && "pause".equals(currentScreen)) resume(); else pause();
                break;
            case "sound": toggleSound(); break;
            case "music": toggleMusic(); break;
            case "neon": setNeonTiles(!neon); break;
            default: break;
        }
    }

    private void swap() {
        int before = engine.nextVal;
        engine.swapWithNext();
        if (engine.nextVal != before) {
            nextBox.animate().cancel();
            nextBox.setScaleX(1f);
            nextBox.setScaleY(1f);
            nextBox.animate().scaleX(1.12f).scaleY(1.12f).setDuration(100)
                    .withEndAction(() -> nextBox.animate().scaleX(1f).scaleY(1f).setDuration(150).start()).start();
        }
    }

    void setThemeId(String id) {
        sound.click();
        Themes.set(id);
        store.set("drop2048-theme", id);
        rebuild();
    }

    boolean neonTiles() { return neon; }

    boolean soundOn() { return !sound.isMuted(); }

    boolean musicOn() { return music.isEnabled(); }

    void toggleSound() { sound.toggle(); updateToggleIcons(); rebuild(); }

    void toggleMusic() { music.toggle(); updateToggleIcons(); rebuild(); }

    String versionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (RuntimeException ignored) { }
    }

    void setNeonTiles(boolean on) {
        sound.click();
        neon = on;
        store.set("drop2048-neon", on ? "1" : "0");
        TileRenderer.setNeon(on);
        rebuild();
    }

    void setLang(String lang) {
        sound.click();
        I18n.set(lang);
        store.set("drop2048-lang", lang);
        rebuild();
    }

    /** Rebuilds all views for a new theme/language; keeps the open panel and its scroll position. */
    private void rebuild() {
        int scrollY = screens.scroll != null ? screens.scroll.getScrollY() : 0;
        String screen = currentScreen;
        buildUi();
        setScreen(screen);
        if (screens.scroll != null) {
            final android.widget.ScrollView s = screens.scroll;
            s.post(() -> s.scrollTo(0, scrollY));
        }
    }

    private void share() {
        Mode m = engine.mode;
        boolean daily = m == Mode.DAILY;
        String what = daily ? I18n.shareDaily(GameEngine.ddmm(engine.dailyDay)) : I18n.t(m.key);
        String text = I18n.shareText(what, I18n.points(engine.score), engine.maxTile, engine.bestCombo >= 2 ? engine.bestCombo : 0, daily);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Drop 2048");
        i.putExtra(Intent.EXTRA_TEXT, text);
        try { startActivity(Intent.createChooser(i, null)); } catch (RuntimeException ignored) { }
    }

    // ---- HUD ----
    private void updateHud() {
        scoreText.setText(String.valueOf(engine.score));
        bestText.setText(String.valueOf(engine.bestDisplay()));
    }

    private void vibrate(int[] pattern) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        long[] t = new long[pattern.length + 1];
        for (int i = 0; i < pattern.length; i++) t[i + 1] = pattern[i];
        try {
            if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createWaveform(t, -1));
            else vibrator.vibrate(t, -1);
        } catch (RuntimeException ignored) { }
    }

    // ---- GameEngine.Listener ----
    @Override public void onHud() { updateHud(); }

    @Override public void onNextChanged() { if (nextView != null) nextView.invalidate(); }

    @Override public void onSound(GameEngine.Sfx sfx, int a, int b) { sound.play(sfx, a, b); }

    @Override public void onHaptic(int[] patternMs) { vibrate(patternMs); }

    @Override public void onGameOver(boolean isRecord) {
        final int token = ++overToken;
        handler.postDelayed(() -> {
            if (token == overToken && engine.phase == GameEngine.Phase.OVER) openScreen("over");
        }, 700);
    }

    // ---- Keyboard (Chromebooks, Android TV, external keyboards) ----
    @Override public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (currentScreen != null) {
            if (getCurrentFocus() instanceof android.widget.EditText) return super.onKeyDown(keyCode, e); // typing a name
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SPACE) && screens.primary != null) {
                screens.primary.performClick();
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_P) {
                onBackPressed();
                return true;
            }
            return super.onKeyDown(keyCode, e);
        }
        boolean first = e.getRepeatCount() == 0;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT: case KeyEvent.KEYCODE_A:
                if (first) engine.holdDir(-1);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT: case KeyEvent.KEYCODE_D:
                if (first) engine.holdDir(1);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN: case KeyEvent.KEYCODE_S:
                engine.softDrop = true;
                return true;
            case KeyEvent.KEYCODE_DPAD_UP: case KeyEvent.KEYCODE_W:
                if (first) swap();
                return true;
            case KeyEvent.KEYCODE_SPACE:
                if (first) engine.hardDrop();
                return true;
            case KeyEvent.KEYCODE_P: case KeyEvent.KEYCODE_ESCAPE:
                pause();
                return true;
            default:
                return super.onKeyDown(keyCode, e);
        }
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent e) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT: case KeyEvent.KEYCODE_A: engine.releaseDir(-1); return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT: case KeyEvent.KEYCODE_D: engine.releaseDir(1); return true;
            case KeyEvent.KEYCODE_DPAD_DOWN: case KeyEvent.KEYCODE_S: engine.softDrop = false; return true;
            default: return super.onKeyUp(keyCode, e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override public void onBackPressed() {
        if (currentScreen != null) {
            if (!screenStack.isEmpty()) back();
            else if ("pause".equals(currentScreen)) resume();
            else super.onBackPressed(); // start / game-over panel: leave the app
        } else {
            pause();
        }
    }
}
