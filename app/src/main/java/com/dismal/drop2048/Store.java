package com.dismal.drop2048;

import android.content.Context;
import android.content.SharedPreferences;

import com.dismal.drop2048.core.GameEngine;

final class Store implements GameEngine.KeyValue {
    private final SharedPreferences prefs;

    Store(Context ctx) {
        prefs = ctx.getSharedPreferences("drop2048", Context.MODE_PRIVATE);
    }

    @Override public String get(String key) { return prefs.getString(key, null); }

    @Override public void set(String key, String value) { prefs.edit().putString(key, value).apply(); }

    boolean getBool(String key, boolean def) {
        String v = get(key);
        return v == null ? def : "1".equals(v);
    }

    void setBool(String key, boolean value) { set(key, value ? "1" : "0"); }
}
