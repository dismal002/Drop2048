package com.dismal.drop2048.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Themes {
    private Themes() {}

    /** One colour theme. All colours are ARGB ints. */
    public static final class Theme {
        public String id;
        public boolean bevel, glow, mono, hasBorder, modern;
        public int border;
        public float radius;
        // page (UI chrome)
        public int bg, text, textStrong, panel, panelText, button, buttonHover, buttonFg, soft, softHover,
                logo, logoFg, screen, inputBg, inputBorder, rowAlt, me, ok, err;
        // board
        public int board, cell, column, outline;
        public int[] tileBg = new int[11], tileFg = new int[11];
        public int bigBg, bigFg;
        public int[] jokerStops;
        public int jokerStar;
        public int floatStroke, floatText, floatCombo, floatRecord;

        private int idx(int v) {
            int i = Integer.numberOfTrailingZeros(v) - 1; // log2(v) - 1
            return (v > 1 && Integer.bitCount(v) == 1 && i >= 0 && i < 11) ? i : -1;
        }

        public int tileBg(int v) { int i = idx(v); return i < 0 ? bigBg : tileBg[i]; }
        public int tileFg(int v) { int i = idx(v); return i < 0 ? bigFg : tileFg[i]; }

        /** True if the page background is light (decides dark/light system bar icons). */
        public boolean isLight() {
            double l = 0.299 * ((bg >> 16) & 255) + 0.587 * ((bg >> 8) & 255) + 0.114 * (bg & 255);
            return l > 140;
        }
    }

    /** Parses #rrggbb, #rgb and rgba(r, g, b, a) into an ARGB int. */
    public static int parse(String s) {
        s = s.trim();
        if (s.startsWith("#")) {
            String h = s.substring(1);
            if (h.length() == 3) h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
            return 0xFF000000 | (int) Long.parseLong(h, 16);
        }
        int open = s.indexOf('('), close = s.indexOf(')');
        String[] p = s.substring(open + 1, close).split(",");
        int r = Integer.parseInt(p[0].trim()), g = Integer.parseInt(p[1].trim()), b = Integer.parseInt(p[2].trim());
        int a = p.length > 3 ? Math.round(Float.parseFloat(p[3].trim()) * 255f) : 255;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static Theme make(String id, boolean bevel, boolean glow, boolean mono, String border, float radius,
                              String[] page, String board, String cell, String column, String outline,
                              String[] tiles, String bigBg, String bigFg, String[] stops, String star,
                              String fStroke, String fText, String fCombo, String fRecord) {
        Theme t = new Theme();
        t.id = id; t.bevel = bevel; t.glow = glow; t.mono = mono; t.radius = radius;
        t.hasBorder = border != null;
        t.border = border != null ? parse(border) : 0;
        int[] p = new int[page.length];
        for (int i = 0; i < p.length; i++) p[i] = parse(page[i]);
        t.bg = p[0]; t.text = p[1]; t.textStrong = p[2]; t.panel = p[3]; t.panelText = p[4]; t.button = p[5];
        t.buttonHover = p[6]; t.buttonFg = p[7]; t.soft = p[8]; t.softHover = p[9]; t.logo = p[10]; t.logoFg = p[11];
        t.screen = p[12]; t.inputBg = p[13]; t.inputBorder = p[14]; t.rowAlt = p[15]; t.me = p[16]; t.ok = p[17]; t.err = p[18];
        t.board = parse(board); t.cell = parse(cell); t.column = parse(column); t.outline = parse(outline);
        for (int i = 0; i < 11; i++) { t.tileBg[i] = parse(tiles[i * 2]); t.tileFg[i] = parse(tiles[i * 2 + 1]); }
        t.bigBg = parse(bigBg); t.bigFg = parse(bigFg);
        t.jokerStops = new int[stops.length];
        for (int i = 0; i < stops.length; i++) t.jokerStops[i] = parse(stops[i]);
        t.jokerStar = parse(star);
        t.floatStroke = parse(fStroke); t.floatText = parse(fText); t.floatCombo = parse(fCombo); t.floatRecord = parse(fRecord);
        return t;
    }

    public static final List<Theme> LIST;
    /** Currently active theme. Set it through {@link #set(String)}. */
    public static Theme current;

    static {
        List<Theme> list = new ArrayList<>();
        Theme aurora = make("aurora", false, false, false, null, 0.16f,
                new String[]{"#12141a", "#9aa3b5", "#f7f9ff", "#212632", "#f7f9ff", "#4d7cfe", "#3f6bea", "#ffffff", "#262b37", "#313847", "#ff4d7d", "#ffffff", "rgba(18, 20, 26, 0.94)", "#1a1e27", "#333a4a", "rgba(255, 255, 255, 0.05)", "rgba(77, 124, 254, 0.28)", "#35d07f", "#ff5c6c"},
                "#232732", "#1b1f28", "rgba(255,255,255,0.05)", "rgba(255,255,255,0.55)",
                new String[]{"#ff57dd", "#ffffff", "#88b74f", "#ffffff", "#1d88f6", "#ffffff", "#ffb400", "#ffffff", "#e1502f", "#ffffff", "#8c7bff", "#ffffff", "#53c2d5", "#ffffff", "#fd457e", "#ffffff", "#27b873", "#ffffff", "#7d8794", "#ffffff", "#ffd83d", "#4a3500"},
                "#ffd83d", "#4a3500",
                new String[]{"#ffb400", "#ff4d7d"}, "#ffffff",
                "rgba(0,0,0,0.8)", "#ffffff", "#ffd83d", "#7dffb0");
        aurora.modern = true;
        list.add(aurora);
        list.add(make("classic", false, false, false, null, 0.09f,
                new String[]{"#faf8ef", "#776e65", "#5b5148", "#bbada0", "#f9f6f2", "#8f7a66", "#7c6957", "#f9f6f2", "#eee4da", "#e4d8ca", "#edc22e", "#ffffff", "rgba(250, 248, 239, 0.9)", "#ffffff", "#d8ccbf", "rgba(238, 228, 218, 0.6)", "#f7e7a8", "#4a8c3a", "#c0392b"},
                "#bbada0", "rgba(238,228,218,0.35)", "rgba(255,255,255,0.12)", "rgba(255,255,255,0.7)",
                new String[]{"#eee4da", "#776e65", "#ede0c8", "#776e65", "#f2b179", "#f9f6f2", "#f59563", "#f9f6f2", "#f67c5f", "#f9f6f2", "#f65e3b", "#f9f6f2", "#edcf72", "#f9f6f2", "#edcc61", "#f9f6f2", "#edc850", "#f9f6f2", "#edc53f", "#f9f6f2", "#edc22e", "#f9f6f2"},
                "#3c3a32", "#f9f6f2",
                new String[]{"#c86dd7", "#3023ae"}, "#fff6c9",
                "rgba(119,110,101,0.9)", "#ffffff", "#ffd54a", "#fff2a8"));
        list.add(make("night", false, false, false, null, 0.09f,
                new String[]{"#16181f", "#a9b1c6", "#e6e9f2", "#2a2e3b", "#e6e9f2", "#5b6cff", "#4a5ae6", "#ffffff", "#252935", "#2f3442", "#5b6cff", "#ffffff", "rgba(22, 24, 31, 0.9)", "#1d2029", "#3a3f50", "rgba(255, 255, 255, 0.04)", "rgba(91, 108, 255, 0.3)", "#6fcf97", "#ff7b7b"},
                "#232733", "rgba(255,255,255,0.05)", "rgba(255,255,255,0.06)", "rgba(255,255,255,0.5)",
                new String[]{"#3a4256", "#dfe4f0", "#45506b", "#e6eaf5", "#3f6fd8", "#ffffff", "#4b5ce6", "#ffffff", "#6a4ee6", "#ffffff", "#8a44d8", "#ffffff", "#b23fc4", "#ffffff", "#d23b9a", "#ffffff", "#e8456e", "#ffffff", "#f2644a", "#ffffff", "#f5a623", "#ffffff"},
                "#ffd166", "#222222",
                new String[]{"#29d3c2", "#2b6be0"}, "#ffffff",
                "rgba(0,0,0,0.6)", "#ffffff", "#ffd166", "#ffe7a3"));
        list.add(make("tetris", true, false, false, null, 0.04f,
                new String[]{"#0b0b12", "#9aa0b5", "#ffffff", "#1c1c2b", "#ffffff", "#2d6cdf", "#2559b8", "#ffffff", "#1a1a28", "#24243a", "#f7d308", "#111111", "rgba(11, 11, 18, 0.9)", "#141420", "#2c2c44", "rgba(255, 255, 255, 0.04)", "rgba(247, 211, 8, 0.25)", "#3ddc84", "#ff5c5c"},
                "#12121c", "rgba(255,255,255,0.04)", "rgba(255,255,255,0.07)", "rgba(255,255,255,0.55)",
                new String[]{"#00e5ff", "#003040", "#ffe500", "#403800", "#a000f0", "#ffffff", "#00d000", "#003000", "#f00000", "#ffffff", "#0050ff", "#ffffff", "#ff8c00", "#3a1f00", "#ff4fa3", "#ffffff", "#7c4dff", "#ffffff", "#00c4a7", "#ffffff", "#ffffff", "#111111"},
                "#c0c0c0", "#111111",
                new String[]{"#f00000", "#ff8c00", "#ffe500", "#00d000", "#00e5ff", "#a000f0"}, "#ffffff",
                "rgba(0,0,0,0.75)", "#ffffff", "#ffe500", "#ffffff"));
        list.add(make("gameboy", false, false, true, "#0f380f", 0.02f,
                new String[]{"#9bbc0f", "#306230", "#0f380f", "#306230", "#c4d86a", "#0f380f", "#1e4a1e", "#c4d86a", "#8bac0f", "#7d9c0d", "#0f380f", "#9bbc0f", "rgba(155, 188, 15, 0.92)", "#c4d86a", "#306230", "rgba(15, 56, 15, 0.08)", "rgba(15, 56, 15, 0.22)", "#0f380f", "#0f380f"},
                "#8bac0f", "rgba(15,56,15,0.12)", "rgba(15,56,15,0.08)", "rgba(15,56,15,0.65)",
                new String[]{"#c4d86a", "#0f380f", "#9bbc0f", "#0f380f", "#7a9a10", "#0f380f", "#5a7f1f", "#e0f0a0", "#306230", "#c4d86a", "#24502a", "#9bbc0f", "#0f380f", "#9bbc0f", "#0f380f", "#c4d86a", "#0f380f", "#e8f5b0", "#0f380f", "#ffffff", "#0f380f", "#ffffff"},
                "#0f380f", "#ffffff",
                new String[]{"#306230", "#0f380f"}, "#c4d86a",
                "#0f380f", "#c4d86a", "#e0f0a0", "#ffffff"));
        list.add(make("neon", false, true, false, null, 0.09f,
                new String[]{"#07070d", "#8b8fb3", "#f0f0ff", "#141428", "#f0f0ff", "#ff2bd6", "#e01fbd", "#ffffff", "#11111f", "#1b1b33", "#00f0ff", "#001018", "rgba(7, 7, 13, 0.9)", "#0e0e1c", "#2b2b55", "rgba(255, 255, 255, 0.04)", "rgba(255, 43, 214, 0.25)", "#39ff14", "#ff3864"},
                "#0d0d1a", "rgba(0,240,255,0.06)", "rgba(0,240,255,0.08)", "rgba(0,240,255,0.8)",
                new String[]{"#00f0ff", "#001018", "#39ff14", "#021a00", "#ffe600", "#1a1600", "#ff8a00", "#1a0d00", "#ff2bd6", "#ffffff", "#b026ff", "#ffffff", "#00a2ff", "#ffffff", "#ff3864", "#ffffff", "#7dfcff", "#002022", "#c6ff00", "#1a2000", "#ffffff", "#111111"},
                "#ff00ff", "#ffffff",
                new String[]{"#ff2bd6", "#00f0ff"}, "#ffffff",
                "rgba(0,0,0,0.8)", "#ffffff", "#00f0ff", "#ff2bd6"));
        list.add(make("ocean", false, false, false, null, 0.09f,
                new String[]{"#eaf6fb", "#3d6b80", "#1d3f50", "#4a90a8", "#f2fbff", "#1f6f8b", "#195b72", "#f2fbff", "#d3ebf3", "#c3e2ec", "#ff8a5b", "#ffffff", "rgba(234, 246, 251, 0.92)", "#ffffff", "#b6d9e6", "rgba(74, 144, 168, 0.08)", "#ffe0cc", "#2e8b57", "#d64545"},
                "#4a90a8", "rgba(255,255,255,0.18)", "rgba(255,255,255,0.12)", "rgba(255,255,255,0.8)",
                new String[]{"#e0f4fa", "#2e5d70", "#bfe7f3", "#2e5d70", "#8fd3e8", "#10394a", "#5bbfd9", "#ffffff", "#2fa3c4", "#ffffff", "#1781a6", "#ffffff", "#ffd27a", "#5a3b00", "#ffb35c", "#ffffff", "#ff8a5b", "#ffffff", "#ff6b6b", "#ffffff", "#f94f8a", "#ffffff"},
                "#0b3d5c", "#ffffff",
                new String[]{"#ff8a5b", "#f94f8a"}, "#ffffff",
                "rgba(16,57,74,0.85)", "#ffffff", "#ffd27a", "#fff1c9"));
        list.add(make("forest", false, false, false, null, 0.09f,
                new String[]{"#f1f3e8", "#5d6b4f", "#37432c", "#7d8f63", "#f5f7ee", "#4f6b3a", "#435c31", "#f5f7ee", "#e1e7d0", "#d4dcbf", "#c98b2b", "#ffffff", "rgba(241, 243, 232, 0.92)", "#ffffff", "#c7d1b0", "rgba(125, 143, 99, 0.1)", "#f3e2b8", "#4f8a3a", "#b8432f"},
                "#7d8f63", "rgba(255,255,255,0.18)", "rgba(255,255,255,0.12)", "rgba(255,255,255,0.75)",
                new String[]{"#eef2dc", "#4a5a3a", "#dbe6b8", "#4a5a3a", "#b8d47a", "#2e3d1d", "#8fbf5a", "#ffffff", "#6aa84f", "#ffffff", "#4e8a3e", "#ffffff", "#d9b44a", "#ffffff", "#c98b2b", "#ffffff", "#a8642a", "#ffffff", "#8a4b2a", "#ffffff", "#6b3a24", "#ffffff"},
                "#2f3b26", "#ffffff",
                new String[]{"#e86a5a", "#a8324a"}, "#fff3d6",
                "rgba(55,67,44,0.85)", "#ffffff", "#f2c94c", "#fff3c4"));
        list.add(make("sunset", false, false, false, null, 0.09f,
                new String[]{"#fff3ec", "#8a5a5a", "#5c3040", "#5a3d6b", "#fff1e8", "#b8476a", "#9e3b5a", "#fff8f4", "#fde2d6", "#f8d2c2", "#ff9f43", "#ffffff", "rgba(255, 243, 236, 0.92)", "#ffffff", "#f2c3b0", "rgba(217, 142, 122, 0.1)", "#ffe3a8", "#3f8a5a", "#c0392b"},
                "#5a3d6b", "rgba(255,255,255,0.08)", "rgba(255,255,255,0.1)", "rgba(255,255,255,0.7)",
                new String[]{"#ffe8d6", "#7a4b5a", "#ffd3b5", "#7a4b5a", "#ffb38a", "#ffffff", "#ff9466", "#ffffff", "#ff7461", "#ffffff", "#f25c6e", "#ffffff", "#e0457b", "#ffffff", "#c43a8a", "#ffffff", "#9b3a96", "#ffffff", "#6e3aa0", "#ffffff", "#ffd56b", "#6b3a00"},
                "#2b1f40", "#ffd56b",
                new String[]{"#ffd56b", "#ff7461"}, "#ffffff",
                "rgba(60,30,60,0.85)", "#ffffff", "#ffd56b", "#fff0c2"));
        list.add(make("pastel", false, false, false, null, 0.18f,
                new String[]{"#fdf7fb", "#8b7b93", "#5e4b68", "#cdb4db", "#ffffff", "#a685c2", "#9474b0", "#ffffff", "#f3e8f5", "#eadcee", "#ffafcc", "#ffffff", "rgba(253, 247, 251, 0.92)", "#ffffff", "#e5d4ec", "rgba(205, 180, 219, 0.15)", "#ffe5ec", "#5a9a6a", "#c4566a"},
                "#cdb4db", "rgba(255,255,255,0.3)", "rgba(255,255,255,0.15)", "rgba(255,255,255,0.85)",
                new String[]{"#fff1f5", "#8b6b8a", "#ffe5ec", "#8b6b8a", "#ffc8dd", "#7a4b6a", "#ffafcc", "#ffffff", "#bde0fe", "#3d5a80", "#a2d2ff", "#ffffff", "#caffbf", "#3d6b3d", "#9bf6ff", "#2d6b70", "#ffd6a5", "#7a5230", "#fdffb6", "#6b6b2d", "#b8a1e3", "#ffffff"},
                "#7b6b8f", "#ffffff",
                new String[]{"#f472a8", "#6a8dff"}, "#ffffff",
                "rgba(94,75,104,0.8)", "#ffffff", "#fff3b0", "#ffffff"));
        list.add(make("mono", false, false, false, null, 0.09f,
                new String[]{"#f4f4f4", "#666666", "#222222", "#8a8a8a", "#ffffff", "#333333", "#111111", "#ffffff", "#e6e6e6", "#dadada", "#222222", "#ffffff", "rgba(244, 244, 244, 0.92)", "#ffffff", "#cccccc", "rgba(0, 0, 0, 0.04)", "#dcdcdc", "#222222", "#b00020"},
                "#9e9e9e", "rgba(255,255,255,0.18)", "rgba(255,255,255,0.12)", "rgba(255,255,255,0.85)",
                new String[]{"#f7f7f7", "#444444", "#ebebeb", "#444444", "#dcdcdc", "#222222", "#cdcdcd", "#111111", "#636363", "#ffffff", "#525252", "#ffffff", "#424242", "#ffffff", "#333333", "#ffffff", "#252525", "#ffffff", "#171717", "#ffffff", "#000000", "#ffffff"},
                "#000000", "#ffffff",
                new String[]{"#ffffff", "#000000"}, "#ff3b30",
                "rgba(0,0,0,0.75)", "#ffffff", "#ffffff", "#ffffff"));
        LIST = Collections.unmodifiableList(list);
        current = LIST.get(0);
    }

    public static Theme byId(String id) {
        for (Theme t : LIST) if (t.id.equals(id)) return t;
        return null;
    }

    public static Theme set(String id) {
        Theme t = byId(id);
        current = t != null ? t : LIST.get(0);
        return current;
    }
}
