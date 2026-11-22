package com.dismal.drop2048.core;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
public final class I18n {
    private I18n() {}

    public static final String[] LANGS = {"en", "ru", "es", "ja", "zh-CN", "zh-TW"};
    private static final String[] RU_LIKE = {"ru", "uk", "be", "kk", "ky", "uz", "tg", "hy", "az", "ka"};

    /** Native name of a language, for the picker. */
    public static String langName(String l) {
        if ("ru".equals(l)) return "Русский";
        if ("es".equals(l)) return "Español";
        if ("ja".equals(l)) return "日本語";
        if ("zh-CN".equals(l)) return "简体中文";
        if ("zh-TW".equals(l)) return "繁體中文";
        return "English";
    }

    private static final String[][] RU = {
        {"title", "Drop 2048 — лови кубики, сливай числа"},
        {"language", "Язык"},
        {"pause", "Пауза"},
        {"resumeShort", "Дальше"},
        {"score", "Очки"},
        {"best", "Рекорд"},
        {"next", "Следующий"},
        {"nextTitle", "Поменять с текущим (↑)"},
        {"swapHint", "↑ обмен"},
        {"swapHintTouch", "тап — обмен"},
        {"tagline", "Лови падающие кубики, сливай одинаковые числа и собирай цепочки комбо."},
        {"classic", "Классика"},
        {"classicSub", "скорость растёт"},
        {"zen", "Дзен"},
        {"zenSub", "без ускорения"},
        {"daily", "Испытание дня"},
        {"dailyBtn", "★ Испытание дня"},
        {"dailyUsed", "попытка использована · таблица дня →"},
        {"howTo", "Как играть"},
        {"appearance", "Оформление"},
        {"settings", "Настройки"},
        {"about", "О приложении"},
        {"version", "Версия"},
        {"license", "Лицензия"},
        {"licenseNotice", "Свободное ПО под лицензией GNU GPL v3.0."},
        {"sourceCode", "Исходный код"},
        {"install", "Установить приложение"},
        {"iosHint", "Установить на iPhone: «Поделиться» → «На экран „Домой“»"},
        {"rule1", "Одинаковые числа рядом сливаются: 4 + 4 = 8. Два соседа сразу — ×4."},
        {"rule2", "После слияния всё проседает — получаются цепочки, каждый шаг даёт комбо."},
        {"rule3", "Меняет текущий кубик местами со следующим."},
        {"rule4", "Джокер сливается с любым числом. Нет соседей — ждёт на поле."},
        {"rule5", "Колонка заполнилась до верха — игра окончена. В «Классике» скорость растёт, в «Дзене» — нет."},
        {"keyboard", "Клавиатура"},
        {"keyboardText", "← → двигать · ↓ ускорить · Пробел бросить · ↑ обмен · P пауза"},
        {"phone", "Телефон"},
        {"phoneText", "тяни кубик влево-вправо по полю, отпусти — бросить; тап по «Следующий» — обмен"},
        {"hint", "Тяни кубик · отпусти — бросить"},
        {"gotIt", "Понятно"},
        {"done", "Готово"},
        {"gameOver", "Игра окончена"},
        {"newRecord", "Новый рекорд!"},
        {"dailyComplete", "Испытание пройдено"},
        {"maxTile", "Макс. плитка"},
        {"bestCombo", "Лучшее комбо"},
        {"dropsTime", "Кубиков · время"},
        {"again", "Ещё раз"},
        {"share", "Поделиться"},
        {"back", "Назад"},
        {"sound", "Звук"},
        {"music", "Музыка"},
        {"theme", "Тема"},
        {"neonTiles", "Неоновые плитки"},
        {"on", "Вкл"},
        {"off", "Выкл"},
        {"help", "Помощь"},
        {"resume", "Продолжить"},
        {"restart", "Заново"},
        {"controls", "Управление"},
        {"left", "Влево"},
        {"right", "Вправо"},
        {"drop", "бросить"},
        {"callDaily", "У всех одни и те же кубики — побей мой результат!"},
        {"callNormal", "Сможешь больше?"},
        {"copied", "Скопировано — вставь в чат"},
        {"err.bad_name", "Введите имя"},
        {"err.bad_score", "Некорректный счёт"},
        {"err.bad_tile", "Некорректная плитка"},
        {"err.bad_data", "Некорректные данные"},
        {"err.daily_over", "Это испытание уже закончилось"},
        {"err.daily_dup", "Результат испытания уже записан"},
        {"err.too_often", "Слишком часто, подождите пару секунд"},
        {"theme.aurora", "Аврора"},
        {"theme.classic", "Классика"},
        {"theme.night", "Ночь"},
        {"theme.tetris", "Тетрис"},
        {"theme.gameboy", "Game Boy"},
        {"theme.neon", "Неон"},
        {"theme.ocean", "Океан"},
        {"theme.forest", "Лес"},
        {"theme.sunset", "Закат"},
        {"theme.pastel", "Пастель"},
        {"theme.mono", "Монохром"},
    };

    private static final String[][] EN = {
        {"title", "Drop 2048 — catch blocks, merge numbers"},
        {"language", "Language"},
        {"pause", "Pause"},
        {"resumeShort", "Resume"},
        {"score", "Score"},
        {"best", "Best"},
        {"next", "Next"},
        {"nextTitle", "Swap with current (↑)"},
        {"swapHint", "↑ swap"},
        {"swapHintTouch", "tap to swap"},
        {"tagline", "Catch falling blocks, merge equal numbers and build combo chains."},
        {"classic", "Classic"},
        {"classicSub", "speeds up"},
        {"zen", "Zen"},
        {"zenSub", "no speed-up"},
        {"daily", "Daily challenge"},
        {"dailyBtn", "★ Daily challenge"},
        {"dailyUsed", "try used · today's board →"},
        {"howTo", "How to play"},
        {"appearance", "Appearance"},
        {"settings", "Settings"},
        {"about", "About"},
        {"version", "Version"},
        {"license", "License"},
        {"licenseNotice", "Free software licensed under the GNU General Public License v3.0."},
        {"sourceCode", "Source code"},
        {"install", "Install app"},
        {"iosHint", "Install on iPhone: Share → Add to Home Screen"},
        {"rule1", "Equal numbers next to each other merge: 4 + 4 = 8. Two neighbours at once — ×4."},
        {"rule2", "After a merge everything settles down — chains form, every step adds to the combo."},
        {"rule3", "Swaps the current block with the next one."},
        {"rule4", "The joker merges with any number. No neighbours — it waits on the board."},
        {"rule5", "A column filled to the top ends the game. In Classic the speed grows, in Zen it doesn't."},
        {"keyboard", "Keyboard"},
        {"keyboardText", "← → move · ↓ faster · Space drop · ↑ swap · P pause"},
        {"phone", "Phone"},
        {"phoneText", "drag the block left/right on the board, lift your finger to drop; tap “Next” to swap"},
        {"hint", "Drag the block · lift to drop"},
        {"gotIt", "Got it"},
        {"done", "Done"},
        {"gameOver", "Game over"},
        {"newRecord", "New best!"},
        {"dailyComplete", "Challenge complete"},
        {"maxTile", "Max tile"},
        {"bestCombo", "Best combo"},
        {"dropsTime", "Drops · time"},
        {"again", "Play again"},
        {"share", "Share"},
        {"back", "Back"},
        {"sound", "Sound"},
        {"music", "Music"},
        {"theme", "Theme"},
        {"neonTiles", "Neon tiles"},
        {"on", "On"},
        {"off", "Off"},
        {"help", "Help"},
        {"resume", "Resume"},
        {"restart", "Restart"},
        {"controls", "Controls"},
        {"left", "Left"},
        {"right", "Right"},
        {"drop", "drop"},
        {"callDaily", "Same blocks for everyone — beat my score!"},
        {"callNormal", "Can you beat it?"},
        {"copied", "Copied — paste it into a chat"},
        {"err.bad_name", "Enter a name"},
        {"err.bad_score", "Invalid score"},
        {"err.bad_tile", "Invalid tile"},
        {"err.bad_data", "Invalid data"},
        {"err.daily_over", "This challenge is over"},
        {"err.daily_dup", "Your challenge result is already saved"},
        {"err.too_often", "Too often — wait a couple of seconds"},
        {"theme.aurora", "Aurora"},
        {"theme.classic", "Classic"},
        {"theme.night", "Night"},
        {"theme.tetris", "Tetris"},
        {"theme.gameboy", "Game Boy"},
        {"theme.neon", "Neon"},
        {"theme.ocean", "Ocean"},
        {"theme.forest", "Forest"},
        {"theme.sunset", "Sunset"},
        {"theme.pastel", "Pastel"},
        {"theme.mono", "Mono"},
    };

    private static final String[][] ES = {
        {"title", "Drop 2048 — atrapa bloques, combina números"},
        {"language", "Idioma"},
        {"pause", "Pausa"},
        {"resumeShort", "Seguir"},
        {"score", "Puntos"},
        {"best", "Récord"},
        {"next", "Siguiente"},
        {"nextTitle", "Cambiar con el actual (↑)"},
        {"swapHint", "↑ cambiar"},
        {"swapHintTouch", "toca para cambiar"},
        {"tagline", "Atrapa los bloques que caen, combina números iguales y crea cadenas de combo."},
        {"classic", "Clásico"},
        {"classicSub", "se acelera"},
        {"zen", "Zen"},
        {"zenSub", "sin acelerar"},
        {"daily", "Reto diario"},
        {"dailyBtn", "★ Reto diario"},
        {"dailyUsed", "intento usado · tabla de hoy →"},
        {"howTo", "Cómo jugar"},
        {"appearance", "Apariencia"},
        {"settings", "Ajustes"},
        {"about", "Acerca de"},
        {"version", "Versión"},
        {"license", "Licencia"},
        {"licenseNotice", "Software libre bajo la GNU General Public License v3.0."},
        {"sourceCode", "Código fuente"},
        {"install", "Instalar la app"},
        {"iosHint", "Instalar en iPhone: Compartir → Añadir a pantalla de inicio"},
        {"rule1", "Los números iguales se combinan al estar juntos: 4 + 4 = 8. Dos vecinos a la vez — ×4."},
        {"rule2", "Tras la combinación todo baja — se forman cadenas y cada paso suma al combo."},
        {"rule3", "Cambia el bloque actual por el siguiente."},
        {"rule4", "El comodín se combina con cualquier número. Sin vecinos, espera en el tablero."},
        {"rule5", "Si una columna se llena hasta arriba, la partida termina. En Clásico la velocidad sube; en Zen, no."},
        {"keyboard", "Teclado"},
        {"keyboardText", "← → mover · ↓ acelerar · Espacio soltar · ↑ cambiar · P pausa"},
        {"phone", "Teléfono"},
        {"phoneText", "arrastra el bloque a izquierda o derecha sobre el tablero y suelta para lanzarlo; toca «Siguiente» para cambiar"},
        {"hint", "Arrastra el bloque · suelta para lanzarlo"},
        {"gotIt", "Entendido"},
        {"done", "Listo"},
        {"gameOver", "Fin de la partida"},
        {"newRecord", "¡Nuevo récord!"},
        {"dailyComplete", "Reto completado"},
        {"maxTile", "Ficha máxima"},
        {"bestCombo", "Mejor combo"},
        {"dropsTime", "Bloques · tiempo"},
        {"again", "Otra vez"},
        {"share", "Compartir"},
        {"back", "Atrás"},
        {"sound", "Sonido"},
        {"music", "Música"},
        {"theme", "Tema"},
        {"neonTiles", "Fichas de neón"},
        {"on", "Sí"},
        {"off", "No"},
        {"help", "Ayuda"},
        {"resume", "Continuar"},
        {"restart", "Reiniciar"},
        {"controls", "Controles"},
        {"left", "Izquierda"},
        {"right", "Derecha"},
        {"drop", "soltar"},
        {"callDaily", "Mismos bloques para todos — ¡supera mi puntuación!"},
        {"callNormal", "¿Puedes superarlo?"},
        {"copied", "Copiado — pégalo en el chat"},
        {"err.bad_name", "Escribe un nombre"},
        {"err.bad_score", "Puntuación no válida"},
        {"err.bad_tile", "Ficha no válida"},
        {"err.bad_data", "Datos no válidos"},
        {"err.daily_over", "Este reto ya terminó"},
        {"err.daily_dup", "Tu resultado del reto ya está guardado"},
        {"err.too_often", "Demasiado seguido — espera un par de segundos"},
        {"theme.aurora", "Aurora"},
        {"theme.classic", "Clásico"},
        {"theme.night", "Noche"},
        {"theme.tetris", "Tetris"},
        {"theme.gameboy", "Game Boy"},
        {"theme.neon", "Neón"},
        {"theme.ocean", "Océano"},
        {"theme.forest", "Bosque"},
        {"theme.sunset", "Atardecer"},
        {"theme.pastel", "Pastel"},
        {"theme.mono", "Monocromo"},
    };

    private static final String[][] JA = {
        {"title", "Drop 2048 — ブロックをキャッチして数字を合成"},
        {"language", "言語"},
        {"pause", "一時停止"},
        {"resumeShort", "再開"},
        {"score", "スコア"},
        {"best", "ベスト"},
        {"next", "次"},
        {"nextTitle", "今のブロックと交換（↑）"},
        {"swapHint", "↑ 交換"},
        {"swapHintTouch", "タップで交換"},
        {"tagline", "落ちてくるブロックをキャッチし、同じ数字を合成してコンボをつなげよう。"},
        {"classic", "クラシック"},
        {"classicSub", "加速あり"},
        {"zen", "Zen"},
        {"zenSub", "加速なし"},
        {"daily", "デイリーチャレンジ"},
        {"dailyBtn", "★ デイリーチャレンジ"},
        {"dailyUsed", "挑戦済み · 今日のランキング →"},
        {"howTo", "遊び方"},
        {"appearance", "外観"},
        {"settings", "設定"},
        {"about", "このアプリについて"},
        {"version", "バージョン"},
        {"license", "ライセンス"},
        {"licenseNotice", "GNU GPL v3.0 の自由ソフトウェアです。"},
        {"sourceCode", "ソースコード"},
        {"install", "アプリをインストール"},
        {"iosHint", "iPhone へのインストール: 共有 → ホーム画面に追加"},
        {"rule1", "同じ数字が隣り合うと合成されます: 4 + 4 = 8。両隣同時なら ×4。"},
        {"rule2", "合成後は全体が落ちて連鎖します。1段ごとにコンボが加算されます。"},
        {"rule3", "今のブロックを次のブロックと交換します。"},
        {"rule4", "ジョーカーはどの数字とも合成できます。隣に何もなければ盤面で待機します。"},
        {"rule5", "列が一番上まで埋まるとゲーム終了。クラシックは加速し、Zen は加速しません。"},
        {"keyboard", "キーボード"},
        {"keyboardText", "← → 移動 · ↓ 加速 · Space 落とす · ↑ 交換 · P 一時停止"},
        {"phone", "スマホ"},
        {"phoneText", "盤面でブロックを左右にドラッグして指を離すと落下。「次」をタップで交換"},
        {"hint", "ブロックをドラッグ · 離して落下"},
        {"gotIt", "了解"},
        {"done", "完了"},
        {"gameOver", "ゲームオーバー"},
        {"newRecord", "自己ベスト更新！"},
        {"dailyComplete", "チャレンジ達成"},
        {"maxTile", "最大タイル"},
        {"bestCombo", "最高コンボ"},
        {"dropsTime", "ブロック · 時間"},
        {"again", "もう一度"},
        {"share", "共有"},
        {"back", "戻る"},
        {"sound", "サウンド"},
        {"music", "音楽"},
        {"theme", "テーマ"},
        {"neonTiles", "ネオンタイル"},
        {"on", "オン"},
        {"off", "オフ"},
        {"help", "ヘルプ"},
        {"resume", "再開"},
        {"restart", "やり直す"},
        {"controls", "操作"},
        {"left", "左"},
        {"right", "右"},
        {"drop", "落とす"},
        {"callDaily", "全員同じブロック — このスコアを超えてみて！"},
        {"callNormal", "超えられる？"},
        {"copied", "コピーしました — チャットに貼り付け"},
        {"err.bad_name", "名前を入力してください"},
        {"err.bad_score", "スコアが不正です"},
        {"err.bad_tile", "タイルが不正です"},
        {"err.bad_data", "データが不正です"},
        {"err.daily_over", "このチャレンジは終了しました"},
        {"err.daily_dup", "このチャレンジの結果は保存済みです"},
        {"err.too_often", "回数が多すぎます — 数秒お待ちください"},
        {"theme.aurora", "オーロラ"},
        {"theme.classic", "クラシック"},
        {"theme.night", "ナイト"},
        {"theme.tetris", "テトリス"},
        {"theme.gameboy", "ゲームボーイ"},
        {"theme.neon", "ネオン"},
        {"theme.ocean", "オーシャン"},
        {"theme.forest", "フォレスト"},
        {"theme.sunset", "サンセット"},
        {"theme.pastel", "パステル"},
        {"theme.mono", "モノクロ"},
    };

    private static final String[][] ZH_CN = {
        {"title", "Drop 2048 — 接住方块，合并数字"},
        {"language", "语言"},
        {"pause", "暂停"},
        {"resumeShort", "继续"},
        {"score", "分数"},
        {"best", "最高分"},
        {"next", "下一个"},
        {"nextTitle", "与当前方块交换（↑）"},
        {"swapHint", "↑ 交换"},
        {"swapHintTouch", "点击交换"},
        {"tagline", "接住下落的方块，合并相同的数字，连成连击链。"},
        {"classic", "经典"},
        {"classicSub", "速度会加快"},
        {"zen", "禅模式"},
        {"zenSub", "不会加速"},
        {"daily", "每日挑战"},
        {"dailyBtn", "★ 每日挑战"},
        {"dailyUsed", "已尝试 · 今日排行榜 →"},
        {"howTo", "玩法"},
        {"appearance", "外观"},
        {"settings", "设置"},
        {"about", "关于"},
        {"version", "版本"},
        {"license", "许可证"},
        {"licenseNotice", "自由软件，采用 GNU 通用公共许可证第 3 版（GPLv3）。"},
        {"sourceCode", "源代码"},
        {"install", "安装应用"},
        {"iosHint", "在 iPhone 上安装：分享 → 添加到主屏幕"},
        {"rule1", "相同的数字相邻时会合并：4 + 4 = 8。同时两个相邻 — ×4。"},
        {"rule2", "合并后所有方块下落，形成连锁，每一步都会累积连击。"},
        {"rule3", "把当前方块与下一个交换。"},
        {"rule4", "万能方块能与任意数字合并。旁边没有方块时会在棋盘上等待。"},
        {"rule5", "一列堆到顶部游戏结束。经典模式速度会加快，禅模式不会。"},
        {"keyboard", "键盘"},
        {"keyboardText", "← → 移动 · ↓ 加速 · 空格 落下 · ↑ 交换 · P 暂停"},
        {"phone", "手机"},
        {"phoneText", "在棋盘上左右拖动方块，松手即落下；点击“下一个”可交换"},
        {"hint", "拖动方块 · 松手落下"},
        {"gotIt", "知道了"},
        {"done", "完成"},
        {"gameOver", "游戏结束"},
        {"newRecord", "新纪录！"},
        {"dailyComplete", "挑战完成"},
        {"maxTile", "最大方块"},
        {"bestCombo", "最佳连击"},
        {"dropsTime", "方块 · 时间"},
        {"again", "再来一局"},
        {"share", "分享"},
        {"back", "返回"},
        {"sound", "音效"},
        {"music", "音乐"},
        {"theme", "主题"},
        {"neonTiles", "霓虹方块"},
        {"on", "开"},
        {"off", "关"},
        {"help", "帮助"},
        {"resume", "继续"},
        {"restart", "重新开始"},
        {"controls", "操作"},
        {"left", "左"},
        {"right", "右"},
        {"drop", "落下"},
        {"callDaily", "所有人的方块相同 — 来超过我的分数！"},
        {"callNormal", "你能超过吗？"},
        {"copied", "已复制 — 粘贴到聊天中"},
        {"err.bad_name", "请输入名字"},
        {"err.bad_score", "分数无效"},
        {"err.bad_tile", "方块无效"},
        {"err.bad_data", "数据无效"},
        {"err.daily_over", "这个挑战已结束"},
        {"err.daily_dup", "你的挑战成绩已保存"},
        {"err.too_often", "太频繁了，请稍等几秒"},
        {"theme.aurora", "极光"},
        {"theme.classic", "经典"},
        {"theme.night", "夜晚"},
        {"theme.tetris", "俄罗斯方块"},
        {"theme.gameboy", "Game Boy"},
        {"theme.neon", "霓虹"},
        {"theme.ocean", "海洋"},
        {"theme.forest", "森林"},
        {"theme.sunset", "日落"},
        {"theme.pastel", "柔彩"},
        {"theme.mono", "单色"},
    };

    private static final String[][] ZH_TW = {
        {"title", "Drop 2048 — 接住方塊，合併數字"},
        {"language", "語言"},
        {"pause", "暫停"},
        {"resumeShort", "繼續"},
        {"score", "分數"},
        {"best", "最高分"},
        {"next", "下一個"},
        {"nextTitle", "與目前的方塊交換（↑）"},
        {"swapHint", "↑ 交換"},
        {"swapHintTouch", "點擊交換"},
        {"tagline", "接住掉落的方塊，合併相同數字，串起連擊。"},
        {"classic", "經典"},
        {"classicSub", "速度會加快"},
        {"zen", "禪模式"},
        {"zenSub", "不會加速"},
        {"daily", "每日挑戰"},
        {"dailyBtn", "★ 每日挑戰"},
        {"dailyUsed", "已嘗試 · 今日排行榜 →"},
        {"howTo", "玩法"},
        {"appearance", "外觀"},
        {"settings", "設定"},
        {"about", "關於"},
        {"version", "版本"},
        {"license", "授權條款"},
        {"licenseNotice", "自由軟體，採用 GNU 通用公共許可證第 3 版（GPLv3）。"},
        {"sourceCode", "原始碼"},
        {"install", "安裝應用程式"},
        {"iosHint", "在 iPhone 上安裝：分享 → 加入主畫面"},
        {"rule1", "相同的數字相鄰時會合併：4 + 4 = 8。同時兩個相鄰 — ×4。"},
        {"rule2", "合併後所有方塊落下，形成連鎖，每一步都會累積連擊。"},
        {"rule3", "將目前的方塊與下一個交換。"},
        {"rule4", "萬用方塊能與任何數字合併。旁邊沒有方塊時會在棋盤上等待。"},
        {"rule5", "一欄堆到頂部遊戲結束。經典模式速度會加快，禪模式不會。"},
        {"keyboard", "鍵盤"},
        {"keyboardText", "← → 移動 · ↓ 加速 · 空白鍵 落下 · ↑ 交換 · P 暫停"},
        {"phone", "手機"},
        {"phoneText", "在棋盤上左右拖曳方塊，放開即落下；點選「下一個」可交換"},
        {"hint", "拖曳方塊 · 放開落下"},
        {"gotIt", "了解"},
        {"done", "完成"},
        {"gameOver", "遊戲結束"},
        {"newRecord", "新紀錄！"},
        {"dailyComplete", "挑戰完成"},
        {"maxTile", "最大方塊"},
        {"bestCombo", "最佳連擊"},
        {"dropsTime", "方塊 · 時間"},
        {"again", "再玩一次"},
        {"share", "分享"},
        {"back", "返回"},
        {"sound", "音效"},
        {"music", "音樂"},
        {"theme", "主題"},
        {"neonTiles", "霓虹方塊"},
        {"on", "開"},
        {"off", "關"},
        {"help", "說明"},
        {"resume", "繼續"},
        {"restart", "重新開始"},
        {"controls", "操作"},
        {"left", "左"},
        {"right", "右"},
        {"drop", "落下"},
        {"callDaily", "所有人的方塊相同 — 來超越我的分數！"},
        {"callNormal", "你能超越嗎？"},
        {"copied", "已複製 — 貼到聊天中"},
        {"err.bad_name", "請輸入名字"},
        {"err.bad_score", "分數無效"},
        {"err.bad_tile", "方塊無效"},
        {"err.bad_data", "資料無效"},
        {"err.daily_over", "這個挑戰已結束"},
        {"err.daily_dup", "你的挑戰成績已儲存"},
        {"err.too_often", "太頻繁了，請稍等幾秒"},
        {"theme.aurora", "極光"},
        {"theme.classic", "經典"},
        {"theme.night", "夜晚"},
        {"theme.tetris", "俄羅斯方塊"},
        {"theme.gameboy", "Game Boy"},
        {"theme.neon", "霓虹"},
        {"theme.ocean", "海洋"},
        {"theme.forest", "森林"},
        {"theme.sunset", "日落"},
        {"theme.pastel", "粉彩"},
        {"theme.mono", "單色"},
    };

    private static final Map<String, Map<String, String>> DICTS = new HashMap<>();

    static {
        DICTS.put("en", build(EN));
        DICTS.put("ru", build(RU));
        DICTS.put("es", build(ES));
        DICTS.put("ja", build(JA));
        DICTS.put("zh-CN", build(ZH_CN));
        DICTS.put("zh-TW", build(ZH_TW));
    }

    private static Map<String, String> build(String[][] rows) {
        Map<String, String> m = new HashMap<>();
        for (String[] kv : rows) m.put(kv[0], kv[1]);
        return m;
    }

    private static String lang = "en";

    public static String lang() { return lang; }

    public static void set(String l) {
        for (String x : LANGS) if (x.equals(l)) { lang = l; return; }
    }

    /** Picks a language from the system locale (falls back to English). */
    public static String detect(String saved, Locale locale) {
        for (String x : LANGS) if (x.equals(saved)) return x;
        String base = locale.getLanguage().toLowerCase(Locale.ROOT);
        if (base.equals("zh")) {
            String script = locale.getScript();
            String country = locale.getCountry().toUpperCase(Locale.ROOT);
            if ("Hant".equalsIgnoreCase(script) || country.equals("TW") || country.equals("HK") || country.equals("MO")) {
                return "zh-TW";
            }
            return "zh-CN";
        }
        for (String x : LANGS) if (x.equals(base)) return x;
        for (String x : RU_LIKE) if (x.equals(base)) return "ru";
        return "en";
    }

    public static boolean has(String key) {
        return dict().containsKey(key) || DICTS.get("en").containsKey(key);
    }

    private static Map<String, String> dict() {
        Map<String, String> m = DICTS.get(lang);
        return m != null ? m : DICTS.get("en");
    }

    public static String t(String key) {
        String s = dict().get(key);
        if (s == null) s = DICTS.get("en").get(key);
        if (s == null) s = DICTS.get("ru").get(key);
        return s != null ? s : key;
    }

    private static String ruPlural(int n, String one, String few, String many) {
        int m10 = n % 10, m100 = n % 100;
        if (m10 == 1 && m100 != 11) return one;
        if (m10 >= 2 && m10 <= 4 && (m100 < 12 || m100 > 14)) return few;
        return many;
    }

    private static boolean is(String l) { return lang.equals(l); }

    public static String points(int n) {
        if (is("ru")) return n + " " + ruPlural(n, "очко", "очка", "очков");
        if (is("es")) return n + (n == 1 ? " punto" : " puntos");
        if (is("ja")) return n + "点";
        if (is("zh-CN") || is("zh-TW")) return n + " 分";
        return n + (n == 1 ? " point" : " points");
    }

    public static String dailySub(String date) {
        if (is("ru")) return date + " · у всех одни кубики · 1 попытка";
        if (is("es")) return date + " · mismos bloques para todos · 1 intento";
        if (is("ja")) return date + " · 全員同じブロック · 1回";
        if (is("zh-CN")) return date + " · 所有人相同方块 · 1 次";
        if (is("zh-TW")) return date + " · 所有人相同方塊 · 1 次";
        return date + " · same blocks for everyone · 1 try";
    }

    public static String dailyDone(String points) {
        if (is("ru")) return "сыграно: " + points;
        if (is("es")) return "jugado: " + points;
        if (is("ja")) return "プレイ済み: " + points;
        if (is("zh-CN") || is("zh-TW")) return "已玩：" + points;
        return "played: " + points;
    }

    public static String overMode(String mode) {
        if (is("ru")) return "Режим «" + mode + "»";
        if (is("es")) return "Modo «" + mode + "»";
        if (is("ja")) return mode + "モード";
        if (is("zh-CN") || is("zh-TW")) return "模式「" + mode + "」";
        return "Mode: " + mode;
    }

    public static String overDaily(String date) {
        if (is("ru")) return "Испытание дня · " + date;
        if (is("es")) return "Reto diario · " + date;
        if (is("ja")) return "デイリーチャレンジ · " + date;
        if (is("zh-CN")) return "每日挑战 · " + date;
        if (is("zh-TW")) return "每日挑戰 · " + date;
        return "Daily challenge · " + date;
    }

    public static String playMode(String mode) {
        if (is("ru")) return "Играть: " + mode;
        if (is("es")) return "Jugar: " + mode;
        if (is("ja")) return mode + " でプレイ";
        if (is("zh-CN")) return "游玩：" + mode;
        if (is("zh-TW")) return "遊玩：" + mode;
        return "Play " + mode;
    }

    public static String combo(int n) {
        String word;
        if (is("ru")) word = "Комбо ×";
        else if (is("es")) word = "Combo ×";
        else if (is("ja")) word = "コンボ ×";
        else if (is("zh-CN")) word = "连击 ×";
        else if (is("zh-TW")) word = "連擊 ×";
        else word = "Combo ×";
        return word + n;
    }


    public static String themeTip(String name) {
        if (is("ru")) return "Тема: " + name;
        if (is("es")) return "Tema: " + name;
        if (is("ja")) return "テーマ: " + name;
        if (is("zh-CN")) return "主题：" + name;
        if (is("zh-TW")) return "主題：" + name;
        return "Theme: " + name;
    }

    public static String shareDaily(String date) {
        if (is("ru")) return "Испытание дня " + date;
        if (is("es")) return "Reto diario " + date;
        if (is("ja")) return "デイリーチャレンジ " + date;
        if (is("zh-CN")) return "每日挑战 " + date;
        if (is("zh-TW")) return "每日挑戰 " + date;
        return "Daily challenge " + date;
    }

    public static String shareText(String what, String points, int tile, int combo, boolean daily) {
        String call = t(daily ? "callDaily" : "callNormal");
        String tail;
        if (is("ru")) tail = "плитка " + tile;
        else if (is("es")) tail = "ficha " + tile;
        else if (is("ja")) tail = "タイル " + tile;
        else if (is("zh-CN")) tail = "方块 " + tile;
        else if (is("zh-TW")) tail = "方塊 " + tile;
        else tail = "tile " + tile;
        String comboPart = combo > 0 ? ", " + combo(combo) : "";
        return "Drop 2048 · " + what + ": " + points + ", " + tail + comboPart + ". " + call;
    }
}
