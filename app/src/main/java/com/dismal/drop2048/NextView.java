package com.dismal.drop2048;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import com.dismal.drop2048.core.GameEngine;
import com.dismal.drop2048.core.Themes;

/** The little "next block" preview in the header. */
final class NextView extends View {
    private static final float LOGICAL = 56f;
    private final TileRenderer renderer;
    private GameEngine engine;

    NextView(Context ctx) {
        super(ctx);
        renderer = new TileRenderer(ctx);
    }

    void bind(GameEngine e) { engine = e; }

    @Override protected void onDraw(Canvas canvas) {
        if (engine == null) return;
        float k = getWidth() / LOGICAL;
        canvas.save();
        canvas.scale(k, k);
        renderer.draw(canvas, Themes.current, engine.nextVal, 0, 0, LOGICAL, 1f, 1f);
        canvas.restore();
    }
}
