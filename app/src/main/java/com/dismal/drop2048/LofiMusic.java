package com.dismal.drop2048;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.ArrayList;
import java.util.Random;

final class LofiMusic {
    private static final int SR = 22050;
    private static final double BPM = 72;
    private static final double STEP = 60 / BPM / 4;   // sixteenth, s
    private static final double SWING = STEP * 0.33;
    private static final float VOLUME = 0.2f;

    // Chord progressions: bass (MIDI) and voices, 4 bars each.
    private static final int[][][][] PROGRESSIONS = {
            {   // Dm9 – G13 – Cmaj9 – Am9
                    {{38}, {53, 57, 60, 64}}, {{43}, {53, 57, 59, 64}}, {{36}, {52, 55, 59, 62}}, {{45}, {55, 59, 60, 64}}},
            {   // Fmaj7 – Em7 – Dm7 – Cmaj7
                    {{41}, {53, 57, 60, 64}}, {{40}, {55, 59, 62, 64}}, {{38}, {53, 57, 60, 62}}, {{36}, {52, 55, 59, 60}}},
            {   // Am7 – Fmaj7 – Cmaj7 – G6
                    {{45}, {55, 60, 64, 67}}, {{41}, {52, 57, 60, 64}}, {{36}, {52, 55, 59, 64}}, {{43}, {52, 55, 59, 62}}},
    };
    private static final int[] MELODY = {72, 74, 76, 79, 81}; // C major pentatonic

    private final Store store;
    private volatile boolean enabled;
    private Runner runner;

    LofiMusic(Store store) {
        this.store = store;
        enabled = store.getBool("drop2048-music", true);
    }

    boolean isEnabled() { return enabled; }

    boolean toggle() {
        enabled = !enabled;
        store.setBool("drop2048-music", enabled);
        if (enabled) start(); else stop();
        return enabled;
    }

    synchronized void start() {
        if (!enabled || (runner != null && runner.alive)) return;
        runner = new Runner();
        runner.start();
    }

    synchronized void stop() {
        if (runner != null) runner.stopRequested = true;
    }

    // ---- Streaming thread ----
    private static final class Runner extends Thread {
        volatile boolean stopRequested;
        volatile boolean alive = true;

        Runner() { super("lofi-music"); setDaemon(true); }

        @Override public void run() {
            AudioTrack track = null;
            try {
                int min = AudioTrack.getMinBufferSize(SR, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                track = new AudioTrack(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
                        new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SR)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build(),
                        Math.max(min, 4096) * 2,
                        AudioTrack.MODE_STREAM,
                        AudioManager.AUDIO_SESSION_ID_GENERATE);
                track.play();
                Synth synth = new Synth();
                short[] buf = new short[512];
                while (true) {
                    if (stopRequested) synth.fadeOut();
                    synth.render(buf);
                    track.write(buf, 0, buf.length);
                    if (synth.silent()) break;
                }
            } catch (RuntimeException e) {
                // audio unavailable — stay silent
            } finally {
                alive = false;
                if (track != null) {
                    try { track.stop(); } catch (RuntimeException ignored) { }
                    track.release();
                }
            }
        }
    }

    // ---- Software synth ----
    private static double hz(double midi) { return 440 * Math.pow(2, (midi - 69) / 12); }

    /** Biquad filter (RBJ cookbook). */
    private static final class Biquad {
        private double b0, b1, b2, a1, a2, x1, x2, y1, y2;

        static Biquad lowpass(double f, double q) { return make(0, f, q); }
        static Biquad highpass(double f, double q) { return make(1, f, q); }
        static Biquad bandpass(double f, double q) { return make(2, f, q); }

        private static Biquad make(int kind, double f, double q) {
            Biquad b = new Biquad();
            double w = 2 * Math.PI * f / SR, cos = Math.cos(w), alpha = Math.sin(w) / (2 * q);
            double a0 = 1 + alpha;
            switch (kind) {
                case 0: b.b0 = (1 - cos) / 2; b.b1 = 1 - cos; b.b2 = (1 - cos) / 2; break;
                case 1: b.b0 = (1 + cos) / 2; b.b1 = -(1 + cos); b.b2 = (1 + cos) / 2; break;
                default: b.b0 = alpha; b.b1 = 0; b.b2 = -alpha; break;
            }
            b.a1 = -2 * cos / a0; b.a2 = (1 - alpha) / a0;
            b.b0 /= a0; b.b1 /= a0; b.b2 /= a0;
            return b;
        }

        float process(float x) {
            double y = b0 * x + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;
            x2 = x1; x1 = x; y2 = y1; y1 = y;
            return (float) y;
        }
    }

    /** Exponential ramp up to peak then down to silence (Web Audio gain ramps). */
    private static double envelope(double t, double peak, double attack, double decay) {
        if (t < 0) return 0;
        if (t < attack) return 0.0001 * Math.pow(peak / 0.0001, t / attack);
        double d = t - attack;
        if (d >= decay) return 0;
        return peak * Math.pow(0.0001 / peak, d / decay);
    }

    private static double tri(double p) { return 4 * Math.abs(p - Math.floor(p) - 0.5) - 1; }

    private abstract static class Voice {
        final long start;
        boolean toKeys; // routed through the electric-piano low-pass
        boolean done;

        Voice(long start) { this.start = start; }

        abstract float next(double t, double wobble);
    }

    private static final class KeysVoice extends Voice {
        final double freq, dur, vel, detune;
        double p1, p2;

        KeysVoice(long start, int midi, double dur, double vel, Random r) {
            super(start);
            toKeys = true;
            freq = hz(midi); this.dur = dur; this.vel = vel;
            detune = Math.pow(2, (r.nextDouble() * 12 - 6) / 1200);
        }

        @Override float next(double t, double wobble) {
            if (t > dur + 0.3) { done = true; return 0; }
            // hit → quick fall to a sustain → fade at the end, so a chord rings for the whole bar
            double g;
            if (t < 0.02) g = 0.0001 * Math.pow(vel / 0.0001, t / 0.02);
            else {
                double target = vel * 0.4;
                double tr = dur * 0.8;
                if (t < tr) g = target + (vel - target) * Math.exp(-(t - 0.02) / 0.35);
                else {
                    double gr = target + (vel - target) * Math.exp(-(tr - 0.02) / 0.35);
                    g = 0.0001 + (gr - 0.0001) * Math.exp(-(t - tr) / (dur * 0.07));
                }
            }
            double f = freq * detune * wobble;
            p1 += f / SR;
            p2 += 2 * f / SR;
            return (float) (g * (tri(p1) + 0.25 * Math.sin(2 * Math.PI * p2)));
        }
    }

    private static final class BassVoice extends Voice {
        final double freq, dur;
        double p;

        BassVoice(long start, int midi, double dur) { super(start); freq = hz(midi); this.dur = dur; }

        @Override float next(double t, double wobble) {
            if (t > 0.015 + dur) { done = true; return 0; }
            p += freq / SR;
            return (float) (envelope(t, 0.45, 0.015, dur) * Math.sin(2 * Math.PI * p));
        }
    }

    private static final class KickVoice extends Voice {
        final double vel;
        double p;

        KickVoice(long start, double vel) { super(start); this.vel = vel; }

        @Override float next(double t, double wobble) {
            if (t > 0.4) { done = true; return 0; }
            double f = t < 0.14 ? 110 * Math.pow(42.0 / 110.0, t / 0.14) : 42;
            p += f / SR;
            return (float) (envelope(t, 0.5 * vel, 0.004, 0.3) * Math.sin(2 * Math.PI * p));
        }
    }

    private static final class NoiseVoice extends Voice {
        final Biquad filter;
        final double peak, decay;
        final Random r;

        NoiseVoice(long start, Biquad filter, double peak, double decay, Random r) {
            super(start);
            this.filter = filter; this.peak = peak; this.decay = decay; this.r = r;
        }

        @Override float next(double t, double wobble) {
            if (t > decay + 0.05) { done = true; return 0; }
            return (float) (envelope(t, peak, 0.003, decay) * filter.process((float) (r.nextDouble() * 2 - 1)));
        }
    }

    private static final class LeadVoice extends Voice {
        final double freq;
        double p;

        LeadVoice(long start, int midi) { super(start); toKeys = true; freq = hz(midi); }

        @Override float next(double t, double wobble) {
            if (t > 0.85) { done = true; return 0; }
            double vib = Math.pow(2, 6 * Math.sin(2 * Math.PI * 5 * t) / 1200);
            p += freq * vib / SR;
            return (float) (envelope(t, 0.07, 0.03, 0.7) * Math.sin(2 * Math.PI * p));
        }
    }

    private static final class Synth {
        private final Random rnd = new Random();
        private final ArrayList<Voice> voices = new ArrayList<>();
        private final Biquad keysLp = Biquad.lowpass(1700, 1.12);
        private final Biquad busLp = Biquad.lowpass(3200, 1.12);
        private final Biquad crackleHp = Biquad.highpass(1200, 1.12);
        private long n;                    // samples rendered
        private double nextStep;           // sample index of the next sixteenth
        private int step, bar, prog;
        private double wobblePhase;
        private double wobbleFactor = 1;
        private double gain;               // bus gain
        private double gainTarget = VOLUME;
        private double gainRate = VOLUME / (1.5 * SR); // fade-in 1.5 s
        private double compEnv, compGain = 1;
        private boolean fading;

        void fadeOut() {
            if (fading) return;
            fading = true;
            gainTarget = 0;
            gainRate = Math.max(gain, 0.0001) / (0.5 * SR);
        }

        boolean silent() { return fading && gain <= 0; }

        void render(short[] out) {
            for (int i = 0; i < out.length; i++, n++) {
                if (n >= nextStep) {
                    playStep((long) nextStep);
                    nextStep += STEP * SR;
                    if (++step == 16) {
                        step = 0;
                        bar++;
                        if (bar % 8 == 0) prog = (prog + 1 + rnd.nextInt(PROGRESSIONS.length - 1)) % PROGRESSIONS.length;
                    }
                }
                if ((n & 31) == 0) { // tape "wobble": ±9 cents at 0.35 Hz, updated every 32 samples
                    wobblePhase = 2 * Math.PI * 0.35 * (n / (double) SR);
                    wobbleFactor = Math.pow(2, 9 * Math.sin(wobblePhase) / 1200);
                }
                float bus = 0, keys = 0;
                for (int v = voices.size() - 1; v >= 0; v--) {
                    Voice voice = voices.get(v);
                    if (n < voice.start) continue;
                    float s = voice.next((n - voice.start) / (double) SR, wobbleFactor);
                    if (voice.done) { voices.remove(v); continue; }
                    if (voice.toKeys) keys += s; else bus += s;
                }
                bus += keysLp.process(keys);
                // vinyl crackle: faint noise plus rare clicks, high-passed
                double c = (rnd.nextDouble() * 2 - 1) * 0.01 + (rnd.nextDouble() < 0.0006 ? (rnd.nextDouble() * 2 - 1) * 0.5 : 0);
                bus += crackleHp.process((float) c) * 0.35f;

                // bus gain ramp
                if (gain < gainTarget) gain = Math.min(gainTarget, gain + gainRate);
                else if (gain > gainTarget) gain = Math.max(gainTarget, gain - gainRate);
                float x = busLp.process((float) (bus * gain));

                // gentle compressor (threshold −20 dB, ratio 3)
                double a = Math.abs(x);
                compEnv = a > compEnv ? a : compEnv * 0.9995;
                double targetGain = compEnv > 0.1 ? (0.1 + (compEnv - 0.1) / 3) / compEnv : 1;
                compGain += (targetGain - compGain) * 0.01;
                float y = (float) (x * compGain);
                out[i] = (short) (Math.max(-1f, Math.min(1f, y)) * 32767);
            }
        }

        private void keys(long t, int midi, double dur, double vel) { voices.add(new KeysVoice(t, midi, dur, vel, rnd)); }

        private void bass(long t, int midi, double dur) { voices.add(new BassVoice(t, midi, dur)); }

        private double rnd(double a, double b) { return a + rnd.nextDouble() * (b - a); }

        private long off(long t, double seconds) { return t + (long) (seconds * SR); }

        private void playStep(long t) {
            int[][] chord = PROGRESSIONS[prog][bar % 4];
            int bassNote = chord[0][0];
            int[] keyNotes = chord[1];
            double bar16 = STEP * 16;
            long swung = step % 4 == 2 ? off(t, SWING) : t;
            boolean drums = bar >= 2; // first two bars: chords only, the beat comes in later

            if (step == 0) {
                for (int i = 0; i < keyNotes.length; i++) keys(off(t, i * 0.018), keyNotes[i], bar16 * 0.95, 0.09);
                bass(t, bassNote, STEP * 9);
            }
            if (step == 11 && rnd.nextDouble() < 0.4) {
                for (int i = 0; i < keyNotes.length; i++) keys(off(t, i * 0.015), keyNotes[i], STEP * 5, 0.05);
            }
            if (step == 10) bass(t, bassNote + (rnd.nextDouble() < 0.3 ? 7 : 0), STEP * 5);

            if (drums) {
                if (step == 0 || step == 10 || (step == 7 && rnd.nextDouble() < 0.35)) voices.add(new KickVoice(t, step == 0 ? 1 : 0.8));
                if (step == 4 || step == 12) {
                    voices.add(new NoiseVoice(swung, Biquad.bandpass(1500, 0.7), 0.3 * rnd(0.85, 1), 0.2, rnd));
                }
                if (step % 2 == 0) {
                    voices.add(new NoiseVoice(swung, Biquad.highpass(7500, 1.06), 0.07 * (step % 4 == 0 ? 1 : rnd(0.5, 0.8)), 0.05, rnd));
                }
            }
            // short melody: roughly every other bar, a couple of notes
            if (drums && bar % 2 == 1 && (step == 2 || step == 6 || step == 9 || step == 13) && rnd.nextDouble() < 0.45) {
                voices.add(new LeadVoice(swung, MELODY[rnd.nextInt(MELODY.length)]));
            }
        }
    }
}
