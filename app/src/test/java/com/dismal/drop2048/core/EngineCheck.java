package com.dismal.drop2048.core;

import java.util.*;

/** Plain-JVM sanity checks for the engine (run with: java -cp out com.dismal.drop2048.core.EngineCheck). */
public final class EngineCheck {
    static int failures = 0;
    static void check(boolean ok, String msg) { if (!ok) { failures++; System.out.println("FAIL: " + msg); } }

    static final class Mem implements GameEngine.KeyValue {
        final Map<String, String> m = new HashMap<>();
        public String get(String k) { return m.get(k); }
        public void set(String k, String v) { m.put(k, v); }
    }

    static final class L implements GameEngine.Listener {
        int merges, combos, gameOvers; boolean lastRecord;
        public void onHud() {}
        public void onNextChanged() {}
        public void onSound(GameEngine.Sfx s, int a, int b) { if (s == GameEngine.Sfx.MERGE) merges++; if (s == GameEngine.Sfx.COMBO) combos++; }
        public void onHaptic(int[] p) {}
        public void onGameOver(boolean r) { gameOvers++; lastRecord = r; }
        public void onTrack(String t, Map<String, Object> d) {}
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("daily")) {
            // print doubles for comparison with the JS reference
            for (String day : new String[]{"2026-11-22", "2026-01-01"})
                for (int i = 1; i <= 5; i++) { double[] r = GameEngine.dailyRandoms(day, i); System.out.println(day + " " + i + " " + r[0] + " " + r[1]); }
            return;
        }

        // 1) merge rules on a hand-built position, via a game with scripted drops
        Mem mem = new Mem(); L l = new L();
        GameEngine e = new GameEngine(mem, l, new Random(1));
        check(e.newGame(GameEngine.Mode.CLASSIC), "newGame classic");
        check(e.phase == GameEngine.Phase.PLAYING && e.falling != null, "playing after start");

        // soft check: drop 2s everywhere through swap/hard drop, verify the game ends eventually and score > 0
        Random r = new Random(7);
        int guard = 0;
        while (e.phase != GameEngine.Phase.OVER && guard++ < 200000) {
            if (e.canControl()) {
                int dir = r.nextInt(3) - 1;
                if (dir != 0) e.move(dir);
                if (r.nextInt(6) == 0) e.swapWithNext();
                if (r.nextInt(4) == 0) e.hardDrop();
            }
            e.frame(0.05);
        }
        check(e.phase == GameEngine.Phase.OVER, "game over reached (guard=" + guard + ")");
        check(l.gameOvers == 1, "exactly one gameOver event");
        check(e.score > 0 && l.merges > 0, "scored with merges: score=" + e.score + " merges=" + l.merges + " combos=" + l.combos);
        check(mem.get("drop2048-best") != null && Integer.parseInt(mem.get("drop2048-best")) == e.score, "best saved");
        System.out.println("random game: score=" + e.score + " maxTile=" + e.maxTile + " drops=" + e.drops + " bestCombo=" + e.bestCombo);

        // 2) daily: one attempt per day, deterministic block sequence
        GameEngine.fakeNowMillis = 1_795_000_000_000L;
        Mem mem2 = new Mem();
        GameEngine d1 = new GameEngine(mem2, new L(), new Random(1));
        check(d1.newGame(GameEngine.Mode.DAILY), "daily starts");
        List<Integer> seqA = new ArrayList<>();
        seqA.add(d1.falling.v); seqA.add(d1.nextVal);
        GameEngine d2 = new GameEngine(new Mem(), new L(), new Random(99));
        d2.newGame(GameEngine.Mode.DAILY);
        check(d2.falling.v == seqA.get(0) && d2.nextVal == seqA.get(1), "daily sequence identical on two devices");
        check(!d1.newGame(GameEngine.Mode.DAILY), "second daily attempt refused");
        System.out.println("daily first blocks: " + seqA + " day=" + d1.dailyDay);
        GameEngine.fakeNowMillis = -1;

        // 3) pause/resume/countdown
        GameEngine p = new GameEngine(new Mem(), new L(), new Random(3));
        p.newGame(GameEngine.Mode.ZEN);
        check(p.pause() && p.isPaused(), "pause");
        double y0 = p.falling.y; p.frame(0.05);
        check(p.falling.y == y0, "no movement while paused");
        p.resume(); check(p.overlay == GameEngine.Overlay.COUNTDOWN, "countdown");
        for (int i = 0; i < 70; i++) p.frame(0.05);
        check(p.overlay == GameEngine.Overlay.NONE, "countdown finished");

        System.out.println(failures == 0 ? "ALL OK" : failures + " FAILURES");
        System.exit(failures == 0 ? 0 : 1);
    }
}
