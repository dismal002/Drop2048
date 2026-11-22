package com.dismal.drop2048.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
public final class GameEngine {
    public static final int COLS = 5;
    public static final int ROWS = 8;
    public static final double BASE_SPEED = 1.1;        // cells per second
    public static final double SPEED_PER_DROP = 0.008;
    public static final double MAX_SPEED = 4;
    public static final double SOFT_DROP_SPEED = 14;
    public static final double DROP_SPEED = 40;         // animated fall after the player lets go
    public static final double LOCK_DELAY = 0.5;        // Tetris Guideline "Extended Placement"
    public static final int LOCK_MAX_RESETS = 15;
    public static final double REPEAT_DELAY = 0.18;
    public static final double REPEAT_RATE = 0.05;
    public static final double STEP_DELAY = 0.3;
    public static final int JOKER = 0;
    public static final double JOKER_CHANCE = 0.04;
    public static final int JOKER_MIN_DROPS = 6;
    public static final double COUNTDOWN = 3;

    public enum Mode {
        CLASSIC("classic"), ZEN("zen"), DAILY("daily");
        public final String key;
        Mode(String key) { this.key = key; }
        public static Mode of(String key) {
            for (Mode m : values()) if (m.key.equals(key)) return m;
            return CLASSIC;
        }
    }

    public enum Phase { MENU, PLAYING, RESOLVING, OVER }

    public enum Overlay { NONE, PAUSED, COUNTDOWN }

    public enum Sfx { MOVE, SWAP, LAND, MERGE, COMBO, JOKER, RECORD, GAME_OVER, CLICK }

    // ---- Entities (read by the renderer) ----
    public static final class Tile {
        public int id, v, r, c;
        public double x, y, pop;
    }

    public static final class Falling {
        public int v, c, resets;
        public double x, y, vy, pop, lock;
        public boolean dropping;
    }

    public static final class Ghost {
        public int v;
        public double x, y, tx, ty, t;
    }

    public static final class Floater {
        public String text;
        public double x, y, t;
        public boolean big, gold;
    }

    public static final class Particle {
        public double x, y, vx, vy, size, life, ttl;
        public int color;
    }

    /** Result of today's daily challenge, as saved on the device. */
    public static final class DailyState {
        public String day;
        public boolean done;
        public int score, maxTile;
    }

    public interface Listener {
        void onHud();
        void onNextChanged();
        /** a/b: MERGE → (topValue, combo); COMBO → (comboCount, 0). */
        void onSound(Sfx sfx, int a, int b);
        /** Vibrate/pause alternating durations in ms, starting with a vibration. */
        void onHaptic(int[] patternMs);
        void onGameOver(boolean isRecord);
    }

    public interface KeyValue {
        String get(String key);
        void set(String key, String value);
    }

    // ---- State ----
    private final KeyValue store;
    private final Listener listener;
    private final Random rng;

    public Tile[][] grid = emptyGrid();
    public Falling falling;
    public int nextVal = 2;
    public int score, combo, maxTile = 2, drops, bestCombo;
    public double playTime;
    public Phase phase = Phase.MENU;
    public Overlay overlay = Overlay.NONE;
    public double countdownLeft;
    public Mode mode = Mode.CLASSIC;
    public Mode lastMode = Mode.CLASSIC;
    public int best, bestAtStart;
    public boolean overIsRecord;
    public String dailyDay;
    public final List<Ghost> ghosts = new ArrayList<>();
    public final List<Floater> floats = new ArrayList<>();
    public final List<Particle> particles = new ArrayList<>();
    public double shake;
    public boolean softDrop;

    private boolean recordAnnounced;
    private List<Tile> candidates = new ArrayList<>();
    private double resolveTimer;
    private final List<Integer> heldDirs = new ArrayList<>();
    private double repeatTimer;
    private int nextId = 1;
    private int tileIndex;

    /** Test hook: when ≥ 0, used instead of the system clock. */
    public static long fakeNowMillis = -1;

    public GameEngine(KeyValue store, Listener listener) { this(store, listener, new Random()); }

    public GameEngine(KeyValue store, Listener listener, Random rng) {
        this.store = store;
        this.listener = listener;
        this.rng = rng;
        mode = Mode.of(store.get("drop2048-mode"));
        if (mode == Mode.DAILY) mode = Mode.CLASSIC;
        lastMode = mode;
        dailyDay = todayKey();
        loadBest();
        demoGrid();
    }

    // ---- Helpers ----
    private static Tile[][] emptyGrid() { return new Tile[ROWS][COLS]; }

    private Tile makeTile(int v, int r, int c) {
        Tile t = new Tile();
        t.id = nextId++;
        t.v = v; t.r = r; t.c = c; t.x = c; t.y = r;
        return t;
    }

    private static long now() { return fakeNowMillis >= 0 ? fakeNowMillis : System.currentTimeMillis(); }

    /** Day key for the daily challenge (UTC+3). */
    public static String todayKey() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new Date(now() + 3L * 3600 * 1000));
    }

    public static String ddmm(String day) { return day.substring(8, 10) + "." + day.substring(5, 7); }

    private void loadBest() {
        if (mode == Mode.ZEN) best = parseInt(store.get("drop2048-best-zen"));
        else if (mode == Mode.DAILY) best = 0; // one attempt: the day's record is the current score
        else best = parseInt(store.get("drop2048-best"));
    }

    private static int parseInt(String s) {
        try { return s == null ? 0 : Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }

    static int hashString(String str) {
        int h = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++) h = (h ^ str.charAt(i)) * 16777619;
        return h;
    }

    /** Two uniform numbers in [0,1) for the n-th block of the given day. */
    public static double[] dailyRandoms(String day, int index) {
        int a = hashString("drop2048:" + day) ^ (index * 0x9e3779b9);
        double[] out = new double[2];
        for (int k = 0; k < 2; k++) {
            a = a + 0x6d2b79f5;
            int t = (a ^ (a >>> 15)) * (1 | a);
            t = (t + ((t ^ (t >>> 7)) * (61 | t))) ^ t;
            out[k] = ((t ^ (t >>> 14)) & 0xFFFFFFFFL) / 4294967296.0;
        }
        return out;
    }

    private double[] nextRandoms() {
        if (mode != Mode.DAILY) return new double[]{rng.nextDouble(), rng.nextDouble()};
        return dailyRandoms(dailyDay, ++tileIndex);
    }

    // ---- Daily challenge bookkeeping ----
    public DailyState dailyState() {
        String raw = store.get("drop2048-daily");
        if (raw == null) return null;
        String[] p = raw.split("\\|");
        if (p.length < 4 || !p[0].equals(todayKey())) return null;
        DailyState d = new DailyState();
        d.day = p[0];
        d.done = "1".equals(p[1]);
        d.score = parseInt(p[2]);
        d.maxTile = parseInt(p[3]);
        return d;
    }

    private void saveDaily(String day, boolean done, int score, int maxTile) {
        store.set("drop2048-daily", day + "|" + (done ? "1" : "0") + "|" + score + "|" + maxTile);
    }

    // ---- Start screen backdrop ----
    private void demoGrid() {
        grid = emptyGrid();
        Integer[][] rows = {{2, null, 8, 4, 16}, {4, 32, 64, 8, 2}, {null, null, 4, null, JOKER}};
        for (int i = 0; i < rows.length; i++) {
            for (int c = 0; c < COLS; c++) {
                Integer v = rows[i][c];
                if (v != null) grid[ROWS - 1 - i][c] = makeTile(v, ROWS - 1 - i, c);
            }
        }
        for (Tile t : applyGravity()) t.y = t.r;
        falling = new Falling();
        falling.v = 8; falling.c = 3; falling.x = 3; falling.y = 2.3; falling.vy = 2.3;
        nextVal = 4;
    }

    // ---- Game flow ----
    /** Starts a game. Returns false if the daily challenge was already played today (caller shows its board). */
    public boolean newGame(Mode newMode) {
        if (newMode == Mode.DAILY) {
            if (dailyState() != null) return false;
            dailyDay = todayKey();
            saveDaily(dailyDay, false, 0, 0);
        } else {
            lastMode = newMode;
            store.set("drop2048-mode", newMode.key);
        }
        mode = newMode;
        tileIndex = 0;
        loadBest();
        grid = emptyGrid();
        score = 0; combo = 0; bestCombo = 0; maxTile = 2; drops = 0; playTime = 0;
        bestAtStart = best;
        recordAnnounced = false;
        ghosts.clear(); floats.clear(); particles.clear(); candidates = new ArrayList<>();
        overlay = Overlay.NONE;
        releaseKeys();
        nextVal = randomValue();
        phase = Phase.PLAYING;
        spawn();
        listener.onHud();
        return true;
    }

    /** The bigger the biggest tile, the bigger the dropped numbers. */
    private int randomValue() {
        double[] u = nextRandoms();
        if (drops >= JOKER_MIN_DROPS && u[0] < JOKER_CHANCE) return JOKER;
        int maxExp = (int) Math.max(2, Math.min(7, log2(maxTile) - 2));
        int total = 0;
        for (int e = 1; e <= maxExp; e++) total += maxExp - e + 1;
        double roll = u[1] * total;
        for (int e = 1; e <= maxExp; e++) {
            roll -= maxExp - e + 1;
            if (roll < 0) return 1 << e;
        }
        return 2;
    }

    private static double log2(int v) { return Math.log(v) / Math.log(2); }

    private int stackHeight(int c) {
        int h = 0;
        for (int r = ROWS - 1; r >= 0 && grid[r][c] != null; r--) h++;
        return h;
    }

    public int landingRow(int c) { return ROWS - 1 - stackHeight(c); }

    public double speed() {
        return mode == Mode.ZEN ? BASE_SPEED : Math.min(MAX_SPEED, BASE_SPEED + drops * SPEED_PER_DROP);
    }

    private void spawn() {
        int c = COLS / 2;
        Falling f = new Falling();
        f.v = nextVal; f.c = c; f.x = c; f.y = 0; f.vy = 0;
        falling = f;
        nextVal = randomValue();
        listener.onNextChanged();
        if (landingRow(c) < 0) gameOver();
    }

    /** Like Tetris: a block is "in a row" until it has fully dropped into the next one. */
    private boolean canEnter(int c) {
        return c >= 0 && c < COLS && (int) Math.floor(falling.y + 1e-6) <= landingRow(c);
    }

    public boolean canControl() {
        return phase == Phase.PLAYING && falling != null && !falling.dropping && overlay == Overlay.NONE;
    }

    public void move(int dir) {
        if (!canControl()) return;
        if (canEnter(falling.c + dir)) {
            falling.c += dir;
            falling.y = Math.min(falling.y, landingRow(falling.c));
            if (falling.lock > 0 && falling.resets < LOCK_MAX_RESETS) {
                falling.lock = 0;
                falling.resets++;
            }
            listener.onSound(Sfx.MOVE, 0, 0);
        }
    }

    /** Drag: moves the block towards the column under the finger, stopping at the first blocked one. */
    public void moveTo(int target) {
        if (!canControl()) return;
        target = Math.max(0, Math.min(COLS - 1, target));
        int dir = Integer.compare(target, falling.c);
        boolean moved = false;
        while (falling.c != target && canEnter(falling.c + dir)) {
            falling.c += dir;
            falling.y = Math.min(falling.y, landingRow(falling.c));
            if (falling.lock > 0 && falling.resets < LOCK_MAX_RESETS) {
                falling.lock = 0;
                falling.resets++;
            }
            moved = true;
        }
        if (moved) listener.onSound(Sfx.MOVE, 0, 0);
    }

    /** "Up": swaps the current block with the next one. */
    public void swapWithNext() {
        if (!canControl() || falling.v == nextVal) return;
        int tmp = falling.v;
        falling.v = nextVal;
        nextVal = tmp;
        falling.pop = 1;
        listener.onNextChanged();
        listener.onSound(Sfx.SWAP, 0, 0);
    }

    /** Drops the block down with a short animated fall (the demo's "lift to drop"). */
    public void hardDrop() { drop(); }

    /** Starts the fast fall; the block lands when it reaches the stack. */
    public void drop() {
        if (!canControl()) return;
        falling.dropping = true;
        falling.lock = 0;
        falling.pop = 1;
    }

    private void land() {
        int c = falling.c;
        int r = landingRow(c);
        Tile t = makeTile(falling.v, r, c);
        t.x = falling.x;
        t.y = falling.vy;
        grid[r][c] = t;
        falling = null;
        drops++;
        combo = 0;
        candidates = new ArrayList<>();
        candidates.add(t);
        phase = Phase.RESOLVING;
        resolveTimer = 0.14;
        listener.onSound(Sfx.LAND, 0, 0);
    }

    private static final int[][] AROUND = {{1, 0}, {0, -1}, {0, 1}, {-1, 0}};

    private List<Tile> around(Tile t) {
        List<Tile> out = new ArrayList<>(4);
        for (int[] d : AROUND) {
            int r = t.r + d[0], c = t.c + d[1];
            if (r >= 0 && r < ROWS && c >= 0 && c < COLS && grid[r][c] != null) out.add(grid[r][c]);
        }
        return out;
    }

    /** A joker becomes the neighbour value that gives the biggest merge. */
    private int jokerValue(Tile t, java.util.Set<Tile> used) {
        Map<Integer, Integer> counts = new LinkedHashMap<>();
        for (Tile n : around(t)) {
            if (n.v != JOKER && !used.contains(n)) counts.merge(n.v, 1, Integer::sum);
        }
        int bestV = JOKER;
        long bestResult = 0;
        for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
            long res = (long) e.getKey() << e.getValue();
            if (res > bestResult) {
                bestResult = res;
                bestV = e.getKey();
            }
        }
        return bestV;
    }

    /** Drops tiles down their columns; returns the ones that moved. */
    private List<Tile> applyGravity() {
        List<Tile> moved = new ArrayList<>();
        for (int c = 0; c < COLS; c++) {
            int write = ROWS - 1;
            for (int r = ROWS - 1; r >= 0; r--) {
                Tile t = grid[r][c];
                if (t == null) continue;
                if (r != write) {
                    grid[write][c] = t;
                    grid[r][c] = null;
                    t.r = write;
                    moved.add(t);
                }
                write--;
            }
        }
        return moved;
    }

    /** One chain step: candidates merge with equal (or joker) neighbours, then gravity. */
    private boolean resolveStep() {
        java.util.Set<Tile> used = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Tile, Boolean>());
        List<Tile> merged = new ArrayList<>();
        boolean jokerUsed = false;
        List<Tile> cands = new ArrayList<>();
        for (Tile t : candidates) if (grid[t.r][t.c] == t) cands.add(t);
        Collections.sort(cands, (a, b) -> b.r - a.r); // stable, bottom rows first

        for (Tile t : cands) {
            if (used.contains(t)) continue;
            if (t.v == JOKER) {
                int v = jokerValue(t, used);
                if (v == JOKER) continue; // no numbers around — the joker waits on the board
                t.v = v;
                jokerUsed = true;
            }
            List<Tile> nbrs = new ArrayList<>();
            for (Tile n : around(t)) if (!used.contains(n) && (n.v == t.v || n.v == JOKER)) nbrs.add(n);
            if (nbrs.isEmpty()) continue;

            used.add(t);
            for (Tile n : nbrs) {
                used.add(n);
                grid[n.r][n.c] = null;
                if (n.v == JOKER) jokerUsed = true;
                Ghost g = new Ghost();
                g.v = n.v; g.x = n.x; g.y = n.y; g.tx = t.c; g.ty = t.r;
                ghosts.add(g);
            }
            t.v <<= nbrs.size();
            t.pop = 1;
            merged.add(t);
        }

        if (merged.isEmpty()) return false;

        combo++;
        bestCombo = Math.max(bestCombo, combo);
        int top = 0;
        Themes.Theme th = Themes.current;
        for (Tile t : merged) {
            int gain = t.v * combo;
            score += gain;
            maxTile = Math.max(maxTile, t.v);
            top = Math.max(top, t.v);
            addFloat("+" + gain, t.c + 0.5, t.r + 0.3, false, false);
            burst(t.c + 0.5, t.r + 0.5, th.tileBg(t.v), 10 + combo * 2);
        }
        if (jokerUsed) {
            listener.onSound(Sfx.JOKER, 0, 0);
            for (Tile t : merged) burst(t.c + 0.5, t.r + 0.5, th.jokerStar, 14);
        }
        listener.onSound(Sfx.MERGE, top, combo);
        // A merge is a short tick; a combo is a series with one pulse per chain step (up to 4).
        if (combo >= 2) {
            int n = Math.min(4, combo) * 2 - 1;
            int[] pattern = new int[n];
            for (int i = 0; i < n; i++) pattern[i] = (i % 2 == 1) ? 25 : 20;
            listener.onHaptic(pattern);
        } else {
            listener.onHaptic(new int[]{25});
        }
        if (combo >= 2) {
            addFloat(I18n.combo(combo), COLS / 2.0, ROWS / 2.0, true, false);
            listener.onSound(Sfx.COMBO, combo, 0);
        }
        if (combo >= 3) shake = Math.min(14, combo * 3);

        if (!recordAnnounced && bestAtStart > 0 && score > bestAtStart) {
            recordAnnounced = true;
            addFloat(I18n.t("newRecord"), COLS / 2.0, ROWS / 2.0 - 1, true, true);
            listener.onSound(Sfx.RECORD, 0, 0);
        }

        List<Tile> next = new ArrayList<>(merged);
        next.addAll(applyGravity());
        candidates = next;
        listener.onHud();
        return true;
    }

    private void addFloat(String text, double x, double y, boolean big, boolean gold) {
        Floater f = new Floater();
        f.text = text; f.x = x; f.y = y; f.big = big; f.gold = gold;
        floats.add(f);
    }

    private void burst(double cx, double cy, int color, int n) {
        for (int i = 0; i < n; i++) {
            double a = rng.nextDouble() * Math.PI * 2;
            double sp = 2 + rng.nextDouble() * 4;
            Particle p = new Particle();
            p.x = cx; p.y = cy;
            p.vx = Math.cos(a) * sp;
            p.vy = Math.sin(a) * sp - 2;
            p.size = 4 + rng.nextDouble() * 6;
            p.color = color;
            p.ttl = 0.4 + rng.nextDouble() * 0.35;
            particles.add(p);
        }
    }

    // ---- Pause / game over ----
    public boolean isPaused() { return overlay != Overlay.NONE; }

    /** Returns true if the game is now paused (the caller then shows the pause bar). */
    public boolean pause() {
        if (overlay == Overlay.COUNTDOWN) overlay = Overlay.NONE; // pause right during the countdown
        if (phase != Phase.PLAYING && phase != Phase.RESOLVING) return false;
        if (overlay == Overlay.PAUSED) return false;
        overlay = Overlay.PAUSED;
        releaseKeys();
        return true;
    }

    /** After a pause: 3-2-1 countdown, the board stays visible. */
    public void resume() {
        if (overlay != Overlay.PAUSED) return;
        overlay = Overlay.COUNTDOWN;
        countdownLeft = COUNTDOWN;
        listener.onSound(Sfx.CLICK, 0, 0);
    }

    private void tickCountdown(double dt) {
        int before = (int) Math.ceil(countdownLeft);
        countdownLeft -= dt;
        if (countdownLeft <= 0) {
            overlay = Overlay.NONE;
            listener.onSound(Sfx.SWAP, 0, 0);
        } else if ((int) Math.ceil(countdownLeft) != before) {
            listener.onSound(Sfx.CLICK, 0, 0);
        }
    }

    private void gameOver() {
        phase = Phase.OVER;
        falling = null;
        boolean isRecord = score > bestAtStart && score > 0;
        if (mode == Mode.DAILY) {
            saveDaily(dailyDay, true, score, maxTile);
        } else if (score > best) {
            best = score;
            store.set(mode == Mode.ZEN ? "drop2048-best-zen" : "drop2048-best", String.valueOf(best));
        }
        listener.onHud();
        listener.onSound(Sfx.GAME_OVER, 0, 0);
        overIsRecord = isRecord;
        listener.onGameOver(isRecord);
    }

    public int bestDisplay() { return Math.max(best, score); }

    // ---- Input ----
    /** Direction pressed (key down / button down). Auto-repeat is handled in {@link #update}. */
    public void holdDir(int dir) {
        heldDirs.remove(Integer.valueOf(dir));
        heldDirs.add(dir);
        move(dir);
        repeatTimer = REPEAT_DELAY;
    }

    public void releaseDir(int dir) {
        int i = heldDirs.indexOf(dir);
        if (i < 0) return;
        heldDirs.remove(i);
        // One released but the other still held — pause again before repeating towards it.
        if (i == heldDirs.size()) repeatTimer = REPEAT_DELAY;
    }

    public void releaseKeys() {
        heldDirs.clear();
        softDrop = false;
    }

    // ---- Frame ----
    /** Advances the game by dt seconds (the caller caps dt at 0.05). */
    public void frame(double dt) {
        if (overlay == Overlay.COUNTDOWN) tickCountdown(dt);
        if (overlay == Overlay.NONE) update(dt);
    }

    private void update(double dt) {
        if (phase == Phase.PLAYING || phase == Phase.RESOLVING) playTime += dt;

        // While the block isn't controllable (a chain is running) the timer "charges".
        if (!heldDirs.isEmpty()) {
            int dir = heldDirs.get(heldDirs.size() - 1);
            repeatTimer -= dt;
            if (canControl()) {
                while (repeatTimer <= 0) {
                    move(dir);
                    repeatTimer += REPEAT_RATE;
                }
            } else {
                repeatTimer = Math.max(repeatTimer, 0);
            }
        }

        if (phase == Phase.PLAYING && falling != null) {
            int floor = landingRow(falling.c);
            if (falling.dropping || softDrop) {
                if (falling.y < floor) {
                    falling.y = Math.min(floor, falling.y + dt * (falling.dropping ? DROP_SPEED : SOFT_DROP_SPEED));
                    falling.lock = 0;
                }
                if (falling.y >= floor) {
                    falling.y = floor;
                    if (falling.dropping) land();
                    else {
                        falling.lock += dt;
                        if (falling.lock >= LOCK_DELAY) land();
                    }
                }
            }
        } else if (phase == Phase.RESOLVING) {
            resolveTimer -= dt;
            if (resolveTimer <= 0) {
                if (resolveStep()) {
                    resolveTimer = STEP_DELAY;
                } else {
                    phase = Phase.PLAYING;
                    spawn();
                }
            }
        }

        double k = Math.min(1, dt * 18);
        for (Tile[] row : grid) {
            for (Tile t : row) {
                if (t == null) continue;
                t.x += (t.c - t.x) * k;
                t.y += (t.r - t.y) * k;
                t.pop = Math.max(0, t.pop - dt * 3.2);
            }
        }
        if (falling != null) {
            falling.x += (falling.c - falling.x) * Math.min(1, dt * 25);
            // Visible height: down — exactly at the block, up (pulled onto a stack) — smoothly.
            falling.vy = falling.y >= falling.vy ? falling.y : falling.vy + (falling.y - falling.vy) * Math.min(1, dt * 25);
            falling.pop = Math.max(0, falling.pop - dt * 4);
        }

        for (Ghost g : ghosts) g.t += dt / 0.26;
        for (int i = ghosts.size() - 1; i >= 0; i--) if (ghosts.get(i).t >= 1) ghosts.remove(i);
        for (Floater f : floats) f.t += dt / (f.big ? 0.9 : 0.7);
        for (int i = floats.size() - 1; i >= 0; i--) if (floats.get(i).t >= 1) floats.remove(i);
        for (Particle p : particles) {
            p.life += dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.vy += 9 * dt;
        }
        for (int i = particles.size() - 1; i >= 0; i--) if (particles.get(i).life >= particles.get(i).ttl) particles.remove(i);
        shake = Math.max(0, shake - dt * 40);
    }
}
