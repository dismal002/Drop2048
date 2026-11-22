package com.dismal.drop2048;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;

import com.dismal.drop2048.core.GameEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Game sound effects. The block and merge cues are the provided WAV assets; the remaining effects
 * are synthesized in code into small WAVs in the cache dir. Synthesized effects go through SoundPool;
 * the two WAV cues are played with preloaded AudioTrack voices, which is the most reliable path for
 * short game sounds across devices.
 */
final class SoundFx {
    private static final int SR = 44100;
    private static final float MASTER = 0.5f;

    private static final int SINE = 0, TRI = 1, SQUARE = 2;

    private final Context ctx;
    private final Store store;
    private SoundPool pool;
    private final Map<String, Integer> ids = new HashMap<>();
    private WavEffect block, merge;
    private boolean muted;

    SoundFx(Context ctx, Store store) {
        this.ctx = ctx.getApplicationContext();
        this.store = store;
        muted = store.getBool("drop2048-muted", false);
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        pool = new SoundPool.Builder().setMaxStreams(8).setAudioAttributes(attrs).build();
        block = new WavEffect(this.ctx, pool, R.raw.sfx_block);
        merge = new WavEffect(this.ctx, pool, R.raw.sfx_merge);
        new Thread(this::buildAll, "sfx-synth").start();
    }

    boolean isMuted() { return muted; }

    boolean toggle() {
        muted = !muted;
        store.setBool("drop2048-muted", muted);
        return muted;
    }

    void release() {
        if (block != null) block.release();
        if (merge != null) merge.release();
        if (pool != null) pool.release();
        pool = null;
    }

    // ---- Playback ----
    void play(GameEngine.Sfx sfx, int a, int b) {
        switch (sfx) {
            case MOVE: play("move"); break;
            case SWAP: play("swap"); break;
            case LAND: if (!muted && block != null) block.play(); break;
            case MERGE: if (!muted && merge != null) merge.play(); break;
            case COMBO: play(a >= 3 ? "combo4" : "combo3"); break;
            case JOKER: play("joker"); break;
            case RECORD: play("record"); break;
            case GAME_OVER: play("gameover"); break;
            case CLICK: play("click"); break;
        }
    }

    void click() { play("click"); }

    private void play(String name) { play(name, 1f, MASTER); }

    private void play(String name, float rate, float volume) {
        if (muted || pool == null) return;
        Integer id = ids.get(name);
        if (id == null || id == 0) return; // not loaded yet
        pool.play(id, volume, volume, 1, 0, rate);
    }

    // ---- Provided WAV cues, played through preloaded AudioTrack voices ----
    private static final class WavEffect {
        private static final int VOICES = 3;
        private final AudioTrack[] tracks = new AudioTrack[VOICES];
        private int next;
        private final SoundPool fallbackPool;
        private final int fallbackId;

        WavEffect(Context ctx, SoundPool fallbackPool, int res) {
            this.fallbackPool = fallbackPool;
            this.fallbackId = fallbackPool == null ? 0 : fallbackPool.load(ctx, res, 1);
            short[] pcm;
            int rate = 22050;
            try {
                byte[] bytes;
                try (InputStream in = ctx.getResources().openRawResource(res)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                    bytes = out.toByteArray();
                }
                int p = 12;
                while (p + 8 <= bytes.length) {
                    String id = new String(bytes, p, 4, StandardCharsets.US_ASCII);
                    int len = le32(bytes, p + 4);
                    if ("fmt ".equals(id) && p + 16 <= bytes.length) {
                        rate = le32(bytes, p + 12);
                    } else if ("data".equals(id)) {
                        int count = Math.min(len, bytes.length - (p + 8)) / 2;
                        if (rate <= 0) rate = 22050;
                        // Pad very short cues so a static buffer is always accepted by the device.
                        int min = rate / 10;
                        pcm = new short[Math.max(count, min)];
                        for (int i = 0; i < count; i++) {
                            pcm[i] = (short) (((bytes[p + 9 + i * 2] & 0xFF) << 8) | (bytes[p + 8 + i * 2] & 0xFF));
                        }
                        AudioAttributes attrs = new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build();
                        AudioFormat fmt = new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(rate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build();
                        for (int i = 0; i < VOICES; i++) {
                            try {
                                AudioTrack t = new AudioTrack(attrs, fmt, pcm.length * 2,
                                        AudioTrack.MODE_STATIC, AudioManager.AUDIO_SESSION_ID_GENERATE);
                                t.write(pcm, 0, pcm.length);
                                t.setVolume(1f);
                                tracks[i] = t;
                            } catch (RuntimeException ex) {
                                break;
                            }
                        }
                        return;
                    }
                    p += 8 + len + (len & 1);
                }
            } catch (IOException | RuntimeException ignored) {
                // fall back to SoundPool below
            }
        }

        void play() {
            AudioTrack t = tracks[next];
            if (t != null) {
                next = (next + 1) % VOICES;
                try {
                    t.stop();
                    t.reloadStaticData();
                    t.play();
                    return;
                } catch (RuntimeException ignored) {
                    // fall through to the SoundPool voice
                }
            }
            if (fallbackPool != null && fallbackId != 0) {
                fallbackPool.play(fallbackId, 1f, 1f, 1, 0, 1f);
            }
        }

        void release() {
            for (int i = 0; i < VOICES; i++) {
                if (tracks[i] == null) continue;
                try { tracks[i].release(); } catch (RuntimeException ignored) { }
                tracks[i] = null;
            }
        }

        private static int le32(byte[] b, int o) {
            return (b[o] & 255) | ((b[o + 1] & 255) << 8) | ((b[o + 2] & 255) << 16) | ((b[o + 3] & 255) << 24);
        }
    }

    // ---- Synthesis (effects that have no WAV asset) ----
    private static final class Tone {
        final double freq, to, dur, vol, delay, attack;
        final int type;
        Tone(double freq, double to, int type, double dur, double vol, double delay) {
            this.freq = freq; this.to = to; this.type = type; this.dur = dur; this.vol = vol; this.delay = delay;
            this.attack = 0.005;
        }
    }

    private static Tone tone(double freq, int type, double dur, double vol) { return new Tone(freq, 0, type, dur, vol, 0); }

    private static Tone tone(double freq, int type, double dur, double vol, double delay) { return new Tone(freq, 0, type, dur, vol, delay); }

    private static Tone sweep(double freq, double to, int type, double dur, double vol) { return new Tone(freq, to, type, dur, vol, 0); }

    private void buildAll() {
        File dir = new File(ctx.getCacheDir(), "sfx");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        Map<String, List<Tone>> defs = new HashMap<>();
        defs.put("move", list(tone(520, TRI, 0.04, 0.08)));
        defs.put("swap", list(sweep(380, 760, TRI, 0.09, 0.14)));
        defs.put("click", list(tone(700, SINE, 0.05, 0.1)));
        int[] comboNotes = {0, 4, 7, 12};
        for (int n = 3; n <= 4; n++) { // combo of 2 plays 3 notes, 3+ plays 4
            List<Tone> t = new ArrayList<>();
            for (int i = 0; i < n; i++) t.add(tone(523.25 * Math.pow(2, comboNotes[i] / 12.0), SQUARE, 0.16, 0.06, 0.05 + i * 0.09));
            defs.put("combo" + n, t);
        }
        int[] jokerNotes = {0, 7, 12, 19};
        List<Tone> joker = new ArrayList<>();
        for (int i = 0; i < jokerNotes.length; i++) joker.add(tone(659.25 * Math.pow(2, jokerNotes[i] / 12.0), SINE, 0.2, 0.12, i * 0.05));
        defs.put("joker", joker);
        int[] recNotes = {0, 4, 7, 12, 16};
        List<Tone> rec = new ArrayList<>();
        for (int i = 0; i < recNotes.length; i++) rec.add(tone(392 * Math.pow(2, recNotes[i] / 12.0), TRI, 0.25, 0.16, i * 0.09));
        defs.put("record", rec);
        int[] overNotes = {7, 4, 0, -5};
        List<Tone> over = new ArrayList<>();
        for (int i = 0; i < overNotes.length; i++) over.add(tone(330 * Math.pow(2, overNotes[i] / 12.0), TRI, 0.3, 0.18, i * 0.16));
        defs.put("gameover", over);

        for (Map.Entry<String, List<Tone>> e : defs.entrySet()) {
            File f = new File(dir, "v2_" + e.getKey() + ".wav");
            try {
                if (!f.exists()) writeWav(f, render(e.getValue()));
                SoundPool p = pool;
                if (p == null) return;
                int id = p.load(f.getAbsolutePath(), 1);
                ids.put(e.getKey(), id);
            } catch (IOException ex) {
                // a missing effect is not worth crashing for
            }
        }
    }

    private static List<Tone> list(Tone... t) {
        List<Tone> l = new ArrayList<>();
        for (Tone x : t) l.add(x);
        return l;
    }

    private static short[] render(List<Tone> tones) {
        double total = 0;
        for (Tone t : tones) total = Math.max(total, t.delay + t.dur + 0.02);
        float[] mix = new float[(int) (total * SR) + 1];
        for (Tone t : tones) {
            int start = (int) (t.delay * SR);
            int n = (int) (t.dur * SR);
            double phase = 0;
            for (int i = 0; i < n && start + i < mix.length; i++) {
                double time = i / (double) SR;
                double f = t.to > 0 ? t.freq * Math.pow(t.to / t.freq, time / t.dur) : t.freq;
                phase += f / SR;
                double p = phase - Math.floor(phase);
                double osc;
                if (t.type == SINE) osc = Math.sin(2 * Math.PI * p);
                else if (t.type == TRI) osc = 4 * Math.abs(p - 0.5) - 1;
                else osc = p < 0.5 ? 1 : -1;
                double g;
                if (time < t.attack) g = 0.0001 * Math.pow(t.vol / 0.0001, time / t.attack);
                else g = t.vol * Math.pow(0.0001 / t.vol, (time - t.attack) / (t.dur - t.attack));
                mix[start + i] += (float) (osc * g);
            }
        }
        short[] out = new short[mix.length];
        for (int i = 0; i < mix.length; i++) out[i] = (short) (Math.max(-1f, Math.min(1f, mix[i])) * 32767);
        return out;
    }

    private static void writeWav(File f, short[] pcm) throws IOException {
        int dataLen = pcm.length * 2;
        byte[] b = new byte[44 + dataLen];
        putStr(b, 0, "RIFF"); putInt(b, 4, 36 + dataLen); putStr(b, 8, "WAVE");
        putStr(b, 12, "fmt "); putInt(b, 16, 16); putShort(b, 20, 1); putShort(b, 22, 1);
        putInt(b, 24, SR); putInt(b, 28, SR * 2); putShort(b, 32, 2); putShort(b, 34, 16);
        putStr(b, 36, "data"); putInt(b, 40, dataLen);
        for (int i = 0; i < pcm.length; i++) putShort(b, 44 + i * 2, pcm[i]);
        File tmp = new File(f.getPath() + ".tmp");
        try (FileOutputStream out = new FileOutputStream(tmp)) { out.write(b); }
        if (!tmp.renameTo(f)) throw new IOException("rename failed");
    }

    private static void putStr(byte[] b, int o, String s) { for (int i = 0; i < s.length(); i++) b[o + i] = (byte) s.charAt(i); }

    private static void putInt(byte[] b, int o, int v) { b[o] = (byte) v; b[o + 1] = (byte) (v >> 8); b[o + 2] = (byte) (v >> 16); b[o + 3] = (byte) (v >> 24); }

    private static void putShort(byte[] b, int o, int v) { b[o] = (byte) v; b[o + 1] = (byte) (v >> 8); }
}
