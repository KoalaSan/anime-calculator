package com.buddyapps.anicalc;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Anime Calc - calculadora con tema anime, tres paletas de color y herramientas para fans.
 * Sin dependencias externas: solo Android framework y Java 8.
 */
public class MainActivity extends Activity {

    private static final String RELEASES_API =
            "https://api.github.com/repos/KoalaSan/anime-calculator/releases/latest";
    private static final String REPO_URL =
            "https://github.com/KoalaSan/anime-calculator/releases";
    private static final String CURRENT_VERSION = "1.2";

    private static final String THEME_SAKURA = "✿ Sakura";
    private static final String THEME_HOSHI = "✧ Hoshizora";
    private static final String THEME_YORU = "☾ Yoru";

    // bgTop, bgMid, bgBottom, accent, accent2, digit, operator, clear
    private static final int[][] THEMES = {
            {0xFF140F24, 0xFF2A1748, 0xFF100D1E, 0xFFFF6FA9, 0xFF9B6DFF, 0xFF2E2650, 0xFF452F79, 0xFF5A2547},
            {0xFF08182B, 0xFF103A5E, 0xFF071320, 0xFF63D9FF, 0xFF4C9BFF, 0xFF153A5D, 0xFF1C5487, 0xFF17395F},
            {0xFF130C28, 0xFF2C1A4E, 0xFF0B0818, 0xFFC68BFF, 0xFF7A5CFF, 0xFF2A1F48, 0xFF3E2A6B, 0xFF47276E}
    };

    private int themeIndex = 0;
    private int accent, accent2, bgTop, bgMid, bgBottom, digitBg, opBg, clearBg;

    private FrameLayout frame;
    private SparkleView sparkles;
    private TextView title, subtitle, footer;
    private Button themeButton, calcTab, animeTab;
    private View calcPanel, animePanel;
    private LinearLayout displayCard;
    private TextView display, historyView;

    private final List<Button> digitButtons = new ArrayList<>();
    private final List<Button> opButtons = new ArrayList<>();
    private final List<Button> clearButtons = new ArrayList<>();
    private final List<Button> wideButtons = new ArrayList<>();
    private final List<TextView> sectionTexts = new ArrayList<>();
    private final List<TextView> resultTexts = new ArrayList<>();
    private final List<EditText> fields = new ArrayList<>();
    private final List<LinearLayout> cards = new ArrayList<>();
    private Button equalsButton;

    // Estado de la calculadora
    private String input = "0";
    private String history = "";
    private double first = 0;
    private String pendingOp = null;
    private boolean waiting = false;
    private boolean errorState = false;

    private EditText totalEp;
    private EditText seenEp;
    private EditText minutesEp;
    private TextView progressResult;
    private EditText notesInput;
    private TextView notesResult;

    private final DecimalFormat df = new DecimalFormat("0.##########", new DecimalFormatSymbols(Locale.US));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        frame = new FrameLayout(this);
        sparkles = new SparkleView(this);
        frame.addView(sparkles, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        frame.addView(root, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(frame);

        root.addView(buildHeader());
        root.addView(buildTabs());

        calcPanel = buildCalculator();
        animePanel = buildAnimeTools();

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        content.addView(calcPanel, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.addView(animePanel, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(content);
        animePanel.setVisibility(View.GONE);

        footer = new TextView(this);
        footer.setText("Hecho con ♥ para fans del anime · v" + CURRENT_VERSION);
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(6), 0, dp(10));
        root.addView(footer);

        applyTheme();
        updateDisplay();
    }

    // ---------------- Utils de UI ----------------

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int mix(int a, int b, float ratio) {
        int r = (int) (Color.red(a) * (1 - ratio) + Color.red(b) * ratio);
        int g = (int) (Color.green(a) * (1 - ratio) + Color.green(b) * ratio);
        int bl = (int) (Color.blue(a) * (1 - ratio) + Color.blue(b) * ratio);
        return Color.rgb(r, g, bl);
    }

    private GradientDrawable rounded(int fill, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(fill);
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), stroke);
        return d;
    }

    private StateListDrawable pressable(int fill, int stroke, int radius) {
        StateListDrawable d = new StateListDrawable();
        d.addState(new int[]{android.R.attr.state_pressed}, rounded(mix(fill, Color.WHITE, 0.18f), stroke, radius));
        d.addState(new int[]{}, rounded(fill, stroke, radius));
        return d;
    }

    private void buzz() {
        try {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) v.vibrate(12);
        } catch (Exception ignored) {
        }
    }

    // ---------------- Temas ----------------

    private void applyTheme() {
        int[] t = THEMES[themeIndex];
        bgTop = t[0];
        bgMid = t[1];
        bgBottom = t[2];
        accent = t[3];
        accent2 = t[4];
        digitBg = t[5];
        opBg = t[6];
        clearBg = t[7];
        int muted = mix(accent2, Color.WHITE, 0.35f);

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[]{bgTop, bgMid, bgBottom});
        frame.setBackground(bg);
        sparkles.setColors(accent, accent2);

        String[] names = {THEME_SAKURA, THEME_HOSHI, THEME_YORU};
        themeButton.setText(names[themeIndex]);
        themeButton.setTextSize(12);
        themeButton.setTextColor(Color.WHITE);
        themeButton.setBackground(pressable(opBg, accent, 20));

        title.setTextColor(accent);
        subtitle.setTextColor(muted);
        footer.setTextColor(mix(accent2, Color.WHITE, 0.2f));

        styleTab(calcTab, calcPanel.getVisibility() == View.VISIBLE);
        styleTab(animeTab, animePanel.getVisibility() == View.VISIBLE);

        displayCard.setBackground(rounded(mix(bgBottom, Color.WHITE, 0.06f), accent2, 18));
        display.setTextColor(Color.WHITE);
        historyView.setTextColor(muted);

        for (Button b : digitButtons) {
            b.setBackground(pressable(digitBg, mix(accent2, Color.WHITE, 0.25f), 14));
            b.setTextColor(Color.WHITE);
        }
        for (Button b : opButtons) {
            b.setBackground(pressable(opBg, accent, 14));
            b.setTextColor(accent);
        }
        for (Button b : clearButtons) {
            b.setBackground(pressable(clearBg, mix(accent, Color.WHITE, 0.25f), 14));
            b.setTextColor(mix(accent, Color.WHITE, 0.7f));
        }
        equalsButton.setBackground(pressable(accent, accent2, 14));
        equalsButton.setTextColor(bgTop);

        for (TextView s : sectionTexts) s.setTextColor(accent);
        for (TextView r : resultTexts) {
            r.setBackground(rounded(mix(opBg, Color.BLACK, 0.2f), accent2, 12));
            r.setTextColor(mix(accent2, Color.WHITE, 0.65f));
        }
        for (Button b : wideButtons) {
            b.setBackground(pressable(opBg, accent, 12));
            b.setTextColor(Color.WHITE);
        }
        for (EditText e : fields) {
            e.setBackground(rounded(mix(bgBottom, Color.WHITE, 0.06f), accent2, 10));
            e.setTextColor(Color.WHITE);
            e.setHintTextColor(mix(accent2, Color.WHITE, 0.45f));
        }
        for (LinearLayout c : cards) {
            c.setBackground(rounded(mix(bgBottom, Color.WHITE, 0.05f), mix(accent2, Color.BLACK, 0.35f), 18));
        }
    }

    private void styleTab(Button b, boolean active) {
        b.setBackground(pressable(active ? opBg : mix(bgBottom, Color.WHITE, 0.04f),
                active ? accent : mix(accent2, Color.BLACK, 0.45f), 20));
        b.setTextColor(active ? Color.WHITE : mix(accent2, Color.WHITE, 0.35f));
    }

    // ---------------- Cabecera y pestañas ----------------

    private View buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(16), dp(14), dp(8));

        int mascotId = getResources().getIdentifier("mascot", "drawable", getPackageName());
        if (mascotId != 0) {
            ImageView mascot = new ImageView(this);
            mascot.setImageResource(mascotId);
            mascot.setAdjustViewBounds(true);
            mascot.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(76), dp(76));
            lp.rightMargin = dp(10);
            mascot.setLayoutParams(lp);
            header.addView(mascot);
        }

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        title = new TextView(this);
        title.setText("✿ ANIME CALC ✿");
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        titles.addView(title);

        subtitle = new TextView(this);
        subtitle.setText("(◕‿◕)  計算する  がんばって！");
        subtitle.setTextSize(12);
        titles.addView(subtitle);

        header.addView(titles);

        themeButton = new Button(this);
        themeButton.setText(THEME_SAKURA);
        themeButton.setAllCaps(false);
        themeButton.setMinWidth(0);
        themeButton.setMinimumWidth(0);
        themeButton.setLayoutParams(new LinearLayout.LayoutParams(dp(112), dp(44)));
        themeButton.setOnClickListener(v -> {
            themeIndex = (themeIndex + 1) % THEMES.length;
            applyTheme();
            buzz();
        });
        header.addView(themeButton);
        return header;
    }

    private View buildTabs() {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(16), dp(4), dp(16), dp(8));

        calcTab = new Button(this);
        calcTab.setText("🧮 Calculadora");
        animeTab = new Button(this);
        animeTab.setText("🌸 Anime");
        for (Button b : new Button[]{calcTab, animeTab}) {
            b.setTextSize(14);
            b.setAllCaps(false);
            b.setMinWidth(0);
            b.setMinimumWidth(0);
            b.setLayoutParams(new LinearLayout.LayoutParams(0, dp(46), 1f));
        }
        calcTab.setOnClickListener(v -> showPanel(true));
        animeTab.setOnClickListener(v -> showPanel(false));
        tabs.addView(calcTab);
        tabs.addView(animeTab);
        return tabs;
    }

    private void showPanel(boolean calculator) {
        calcPanel.setVisibility(calculator ? View.VISIBLE : View.GONE);
        animePanel.setVisibility(calculator ? View.GONE : View.VISIBLE);
        styleTab(calcTab, calculator);
        styleTab(animeTab, !calculator);
        buzz();
    }

    // ---------------- Calculadora ----------------

    private View buildCalculator() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(4), dp(16), dp(8));

        displayCard = new LinearLayout(this);
        displayCard.setOrientation(LinearLayout.VERTICAL);
        displayCard.setPadding(dp(14), dp(10), dp(14), dp(10));
        displayCard.setElevation(dp(6));
        displayCard.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.6f));

        historyView = new TextView(this);
        historyView.setTextSize(14);
        historyView.setGravity(Gravity.END);
        historyView.setPadding(0, 0, dp(4), dp(4));
        displayCard.addView(historyView);

        display = new TextView(this);
        display.setTextSize(46);
        display.setTypeface(Typeface.DEFAULT_BOLD);
        display.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        display.setSingleLine(true);
        display.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        displayCard.addView(display);
        panel.addView(displayCard);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setRowCount(5);
        grid.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.4f));

        String[][] keys = {
                {"C", "⌫", "%", "÷"},
                {"7", "8", "9", "×"},
                {"4", "5", "6", "−"},
                {"1", "2", "3", "+"},
                {"±", "0", ".", "="}
        };

        for (String[] row : keys) {
            for (String key : row) {
                Button b = makeCalcButton(key);
                b.setOnClickListener(v -> onKey(key));
                grid.addView(b);
            }
        }
        panel.addView(grid);
        return panel;
    }

    private Button makeCalcButton(String key) {
        Button b = new Button(this);
        b.setText(key);
        b.setTextSize(19);
        b.setAllCaps(false);
        b.setMinWidth(0);
        b.setMinimumWidth(0);

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f));
        lp.width = 0;
        lp.height = 0;
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        b.setLayoutParams(lp);

        if ("+-×÷".contains(key) && key.length() == 1) {
            opButtons.add(b);
        } else if (key.equals("=")) {
            b.setTextSize(22);
            b.setTypeface(Typeface.DEFAULT_BOLD);
            equalsButton = b;
        } else if (key.equals("C") || key.equals("⌫")) {
            clearButtons.add(b);
        } else {
            digitButtons.add(b);
        }
        return b;
    }

    private void onKey(String key) {
        buzz();
        switch (key) {
            case "C": clearAll(); break;
            case "⌫": backspace(); break;
            case "%": percent(); break;
            case "±": toggleSign(); break;
            case ".": decimal(); break;
            case "=": equals(); break;
            case "+": case "−": case "×": case "÷": operator(key); break;
            default: digit(key); break;
        }
        updateDisplay();
    }

    private void digit(String d) {
        if (errorState) clearAll();
        if (waiting || input.equals("0")) {
            input = d;
            waiting = false;
        } else if (input.replace(".", "").replace("-", "").length() < 12) {
            input += d;
        }
    }

    private void decimal() {
        if (errorState) clearAll();
        if (waiting) {
            input = "0.";
            waiting = false;
        } else if (!input.contains(".")) {
            input += ".";
        }
    }

    private void operator(String op) {
        if (errorState) return;
        if (pendingOp != null && !waiting) compute();
        first = parse(input);
        pendingOp = op;
        waiting = true;
        history = format(first) + " " + op;
    }

    private void equals() {
        if (errorState || pendingOp == null) return;
        double a = first;
        String op = pendingOp;
        double b = parse(input);
        compute();
        history = format(a) + " " + op + " " + format(b) + " =";
        pendingOp = null;
        waiting = true;
    }

    private void compute() {
        if (pendingOp == null) return;
        double b = parse(input);
        double r;
        switch (pendingOp) {
            case "+": r = first + b; break;
            case "−": r = first - b; break;
            case "×": r = first * b; break;
            case "÷":
                if (b == 0) {
                    showError("No se puede dividir entre 0");
                    return;
                }
                r = first / b;
                break;
            default: return;
        }
        first = r;
        input = format(r);
    }

    private void percent() {
        if (errorState) return;
        input = format(parse(input) / 100.0);
        waiting = true;
    }

    private void toggleSign() {
        if (errorState) return;
        if (input.startsWith("-")) input = input.substring(1);
        else if (!input.equals("0")) input = "-" + input;
    }

    private void backspace() {
        if (errorState) { clearAll(); return; }
        if (waiting) return;
        if (input.length() <= 1 || (input.length() == 2 && input.startsWith("-"))) {
            input = "0";
        } else {
            input = input.substring(0, input.length() - 1);
        }
    }

    private void clearAll() {
        input = "0";
        history = "";
        first = 0;
        pendingOp = null;
        waiting = false;
        errorState = false;
    }

    private void showError(String message) {
        input = "Error";
        history = message;
        errorState = true;
        pendingOp = null;
        waiting = true;
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private double parse(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return "Error";
        String s = df.format(value);
        if (s.length() > 14) s = String.format(Locale.US, "%.4e", value);
        return s;
    }

    private void updateDisplay() {
        display.setText(input);
        historyView.setText(history);
    }

    // ---------------- Herramientas anime ----------------

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(12), dp(14), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        c.setLayoutParams(lp);
        cards.add(c);
        return c;
    }

    private View buildAnimeTools() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(8), dp(16), dp(12));

        // Tarjeta de progreso
        LinearLayout progressCard = card();
        progressCard.addView(section("📺  Progreso de anime"));
        totalEp = field("Episodios totales", "12");
        seenEp = field("Episodios vistos", "5");
        minutesEp = field("Minutos por episodio", "24");
        progressCard.addView(totalEp);
        progressCard.addView(seenEp);
        progressCard.addView(minutesEp);
        Button calcProgress = wideButton("Calcular progreso ✦");
        calcProgress.setOnClickListener(v -> calculateProgress());
        progressCard.addView(calcProgress);
        progressResult = resultView("Introduce los datos y pulsa calcular.");
        progressCard.addView(progressResult);
        panel.addView(progressCard);

        // Tarjeta de notas
        LinearLayout notesCard = card();
        notesCard.addView(section("⭐  Promedio de notas"));
        notesInput = field("Notas separadas por coma", "8.5, 9, 7.5");
        notesCard.addView(notesInput);
        Button calcNotes = wideButton("Calcular promedio ✦");
        calcNotes.setOnClickListener(v -> calculateNotes());
        notesCard.addView(calcNotes);
        notesResult = resultView("Escribe varias notas y pulsa calcular.");
        notesCard.addView(notesResult);
        panel.addView(notesCard);

        // Tarjeta de GitHub
        LinearLayout githubCard = card();
        githubCard.addView(section("⬇️  Copia de seguridad / actualizaciones"));
        TextView info = new TextView(this);
        info.setText("El código y el APK de cada versión están en GitHub. Pulsa el botón para comprobar si hay una versión nueva.");
        info.setTextSize(12);
        info.setPadding(dp(2), dp(2), dp(2), dp(10));
        githubCard.addView(info);
        Button update = wideButton("🔄  Buscar actualización");
        update.setOnClickListener(v -> checkForUpdate());
        githubCard.addView(update);
        Button openRepo = wideButton("🌐  Abrir GitHub");
        openRepo.setOnClickListener(v -> openUrl(REPO_URL));
        githubCard.addView(openRepo);
        panel.addView(githubCard);

        scroll.addView(panel);
        return scroll;
    }

    private TextView section(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(17);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(dp(2), dp(2), dp(2), dp(10));
        sectionTexts.add(t);
        return t;
    }

    private EditText field(String hint, String sample) {
        EditText e = new EditText(this);
        e.setHint(hint + "  (ej. " + sample + ")");
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        e.setLayoutParams(lp);
        fields.add(e);
        return e;
    }

    private Button wideButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        lp.bottomMargin = dp(8);
        b.setLayoutParams(lp);
        wideButtons.add(b);
        return b;
    }

    private TextView resultView(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(14);
        t.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(4);
        t.setLayoutParams(lp);
        resultTexts.add(t);
        return t;
    }

    private void calculateProgress() {
        try {
            double total = parse(totalEp.getText().toString().trim());
            double seen = parse(seenEp.getText().toString().trim());
            double mins = parse(minutesEp.getText().toString().trim());
            if (total <= 0 || seen < 0 || mins < 0) throw new IllegalArgumentException();
            double seenClamped = Math.min(seen, total);
            double pct = seenClamped / total * 100;
            String text = "Vistos: " + humanTime(seenClamped * mins)
                    + "\nRestante: " + humanTime((total - seenClamped) * mins)
                    + "\nProgreso: " + new DecimalFormat("0.#").format(pct) + "%"
                    + (seen > total ? "\n⚠ Vistos > totales, se ajustó al total." : "");
            progressResult.setText(text);
        } catch (Exception e) {
            progressResult.setText("⚠ Revisa los datos (números mayores o iguales que 0).");
        }
        buzz();
    }

    private void calculateNotes() {
        try {
            String raw = notesInput.getText().toString().trim();
            String[] parts = raw.split("[,;\\s]+");
            double sum = 0;
            int count = 0;
            for (String p : parts) {
                if (p.isEmpty()) continue;
                sum += Double.parseDouble(p);
                count++;
            }
            if (count == 0) throw new IllegalArgumentException();
            double avg = sum / count;
            notesResult.setText("Promedio: " + new DecimalFormat("0.##").format(avg)
                    + "  (" + count + " notas)\n" + mood(avg));
        } catch (Exception e) {
            notesResult.setText("⚠ Escribe notas válidas separadas por comas.");
        }
        buzz();
    }

    private String mood(double avg) {
        if (avg >= 9) return "✧ ¡Obra maestra! (≧▽≦)";
        if (avg >= 7) return "♪ Muy buen anime (◕‿◕)";
        if (avg >= 5) return "◦ Está bien, sigue viendo (・_・)";
        return "… Podrías buscar otro (˘･_･˘)";
    }

    private String humanTime(double minutes) {
        int total = (int) Math.round(minutes);
        int h = total / 60;
        int m = total % 60;
        if (h == 0) return m + " min";
        return h + " h " + m + " min";
    }

    // ---------------- Actualizaciones ----------------

    private void checkForUpdate() {
        Toast.makeText(this, "Buscando actualización…", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            String result;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(RELEASES_API);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setRequestProperty("User-Agent", "AnimeCalc");
                int code = conn.getResponseCode();
                if (code != 200) {
                    result = "No se pudo consultar GitHub (código " + code + ").";
                } else {
                    StringBuilder sb = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line).append('\n');
                    reader.close();
                    String json = sb.toString();
                    String tag = extract(json, "tag_name");
                    String link = extract(json, "html_url");
                    if (tag == null) {
                        result = "No hay releases publicadas todavía.";
                    } else if (normalize(tag).equals(normalize(CURRENT_VERSION))) {
                        result = "Ya tienes la última versión (" + CURRENT_VERSION + ") ✿";
                    } else {
                        result = "NEW|" + tag + "|" + (link == null ? REPO_URL : link);
                    }
                }
            } catch (Exception e) {
                result = "Sin conexión: " + e.getMessage();
            } finally {
                if (conn != null) conn.disconnect();
            }
            final String message = result;
            runOnUiThread(() -> showUpdateResult(message));
        }).start();
    }

    private String extract(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private String normalize(String v) {
        return v.replaceAll("(?i)^v", "").trim();
    }

    private void showUpdateResult(String message) {
        if (message.startsWith("NEW|")) {
            String[] parts = message.split("\\|", 3);
            final String link = parts[2];
            new AlertDialog.Builder(this)
                    .setTitle("✨ Nueva versión disponible")
                    .setMessage("Hay una versión nueva: " + parts[1] + "\n¿Abrir GitHub para descargarla?")
                    .setPositiveButton("Abrir GitHub", (d, w) -> openUrl(link))
                    .setNegativeButton("Ahora no", null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Actualizaciones")
                    .setMessage(message)
                    .setPositiveButton("Aceptar", null)
                    .show();
        }
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show();
        }
    }

    /** Fondo de partículas: estrellas y pétalos flotando suavemente. */
    private class SparkleView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float[] xs = new float[24];
        private final float[] ys = new float[24];
        private final float[] sizes = new float[24];
        private final float[] phases = new float[24];
        private int c1 = Color.WHITE;
        private int c2 = Color.WHITE;
        private float phase = 0f;

        SparkleView(Activity activity) {
            super(activity);
            Random random = new Random(2026);
            for (int i = 0; i < xs.length; i++) {
                xs[i] = random.nextFloat();
                ys[i] = random.nextFloat();
                sizes[i] = 1.5f + random.nextFloat() * 3.5f;
                phases[i] = random.nextFloat() * 6.28f;
            }
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(5200);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.addUpdateListener(a -> {
                phase = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        void setColors(int first, int second) {
            c1 = first;
            c2 = second;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            for (int i = 0; i < xs.length; i++) {
                float alpha = 0.2f + 0.55f * (0.5f + 0.5f * (float) Math.sin(phase * 6.283f + phases[i]));
                paint.setColor(i % 2 == 0 ? c1 : c2);
                paint.setAlpha((int) (alpha * 170));
                float cx = xs[i] * w;
                float cy = ((ys[i] - phase * 0.05f + 1f) % 1f) * h;
                canvas.drawCircle(cx, cy, sizes[i], paint);
                if (i % 4 == 0) {
                    paint.setStrokeWidth(1.4f);
                    canvas.drawLine(cx - sizes[i] * 2.4f, cy, cx + sizes[i] * 2.4f, cy, paint);
                    canvas.drawLine(cx, cy - sizes[i] * 2.4f, cx, cy + sizes[i] * 2.4f, paint);
                }
            }
        }
    }
}
