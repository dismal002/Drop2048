# Drop 2048

Fun 2048 drop merge game with no ads and no tracking where you aren't the product.

## Build

Needs JDK 17 and the Android SDK (platform 34).

No third-party dependencies (not even AndroidX). minSdk 21, targetSdk 34, portrait only.

## Layout

    core/GameEngine.java   rules + simulation
    core/Themes.java       the 10 colour themes
    core/I18n.java         ru / en / es / ja / zh-CN / zh-TW strings
    GameView.java          Canvas renderer + Choreographer frame loop
    TileRenderer.java      one tile: bevel / glow / border / joker gradient
    SoundFx.java           effects synthesized to WAV in cache, played via SoundPool
    LofiMusic.java         live lo-fi generator (chords, bass, swung beat, crackle) -> AudioTrack
    ScreenBuilder.java     start / help / settings / about / appearance / game-over panels
    MainActivity.java      layout, touch + keyboard input, lifecycle, navigation
    Ui.java                small themed view builders

## License

Drop 2048 is free software: you can redistribute it and/or modify it under the terms of the
**GNU General Public License, version 3** as published by the Free Software Foundation.
See [LICENSE](LICENSE) for the full text, or <https://www.gnu.org/licenses/gpl-3.0.html>.

It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

Source code: <https://github.com/dismal002/Drop2048>
