package com.buddyapps.anicalc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Anime Calc - calculadora con tema anime y pequeñas herramientas para seguidores de anime.
 * Sin dependencias externas: solo Android framework y Java 8.
 */
public class MainActivity extends Activity {

    private static final String RELEASES_API =
            "https://api.github.com/repos/KoalaSan/anime-calculator/releases/latest";
    private static final String REPO_URL =
            "https://github.com/KoalaSan/anime-calculator/releases";
    private static final String CURRENT_VERSION = "1.0";

    private static final int BG_DARK = 0xFF141024;
    private static final int PANEL = 0xFF211A38;
    private static final int PINK = 0xFFFF6FA9;
    private static final int PURPLE = 0xFF9B6DFF;
    private static final int DIGIT_BG = 0xFF2E2650;
    private static final int OP_BG = 0xFF452F79;
    private static final int MUTED = 0xFFC3B2E6;
    private static final int TEXT = 0xFFFFFFFF;

    // Estado de la calculadora
    private String input = "0";
    private String history = "";
    private double first = 0;
    private String pendingOp = null;
    private boolean waiting = false;
    private boolean errorState = false;

    private TextView display;
    private TextView historyView;
    private View calcPanel;
    private View animePanel;
    private Button calcTab;
    private Button animeTab;

    private EditText totalEp;
    private EditText seenEp;
    private EditText minutesEp;
    private TextView progressResult;
    private EditText notesInput;
    private TextView notesResult;

    private final DecimalFormat df = new DecimalFormat("0.##########", new java.text.DecimalFormatSymbols(Locale.US));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG_DARK);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{BG_DARK, 0xFF241640, 0xFF12101F});
        root.setBackground(bg);

        // Cabecera
        TextView title = new TextView(this);
        title.setText("✿ ANIME CALC ✿");
        title.setTextSize(22);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(PINK);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(18), 0, dp(2));
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("(◕‿◕)  計算する  がんばって！");
        subtitle.setTextSize(12);
        subtitle.setTextColor(MUTED);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, dp(14));
        root.addView(subtitle);

        // Pestañas
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(14), 0, dp(14), dp(8));
        calcTab = tabButton("🧮 Calculadora", true);
        animeTab = tabButton("🌸 Anime", false);
        calcTab.setLayoutParams(rowParams(1));
        animeTab.setLayoutParams(rowParams(1));
        calcTab.setOnClickListener(v -> showPanel(true));
        animeTab.setOnClickListener(v -> showPanel(false));
        tabs.addView(calcTab);
        tabs.addView(animeTab);
        root.addView(tabs);

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

        TextView footer = new TextView(this);
        footer.setText("Hecho con ♥ para fans del anime · v" + CURRENT_VERSION);
        footer.setTextSize(11);
        footer.setTextColor(MUTED);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(6), 0, dp(10));
        root.addView(footer);

        setContentView(root);
        updateDisplay();
    }

    // ---------------- UI helpers ----------------

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private LinearLayout.LayoutParams rowParams(float weight) {
        return new LinearLayout.LayoutParams(0, dp(44), weight);
    }

    private GradientDrawable rounded(int fill, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(fill);
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), stroke);
        return d;
    }

    private Button tabButton(String text, boolean active) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTextColor(active ? TEXT : MUTED);
        b.setBackground(rounded(active ? PURPLE : PANEL, active ? PINK : 0xFF4B3B72, 12));
        return b;
    }

    private void showPanel(boolean calculator) {
        calcPanel.setVisibility(calculator ? View.VISIBLE : View.GONE);
        animePanel.setVisibility(calculator ? View.GONE : View.VISIBLE);
        calcTab.setBackground(rounded(calculator ? PURPLE : PANEL, calculator ? PINK : 0xFF4B3B72, 12));
        calcTab.setTextColor(calculator ? TEXT : MUTED);
        animeTab.setBackground(rounded(calculator ? PANEL : PURPLE, calculator ? 0xFF4B3B72 : PINK, 12));
        animeTab.setTextColor(calculator ? MUTED : TEXT);
        buzz();
    }

    private void buzz() {
        try {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) v.vibrate(12);
        } catch (Exception ignored) {
        }
    }

    private View buildCalculator() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(4), dp(14), dp(6));

        historyView = new TextView(this);
        historyView.setTextSize(14);
        historyView.setTextColor(MUTED);
        historyView.setGravity(Gravity.END);
        historyView.setPadding(dp(6), dp(6), dp(8), dp(2));
        panel.addView(historyView);

        display = new TextView(this);
        display.setTextSize(42);
        display.setTextColor(TEXT);
        display.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        display.setSingleLine(true);
        display.setPadding(dp(8), dp(2), dp(8), dp(14));
        display.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.55f));
        panel.addView(display);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setRowCount(5);
        grid.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.45f));

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
        int bg = DIGIT_BG;
        int fg = TEXT;
        if ("+-×÷".contains(key) && key.length() == 1) {
            bg = OP_BG;
            fg = PINK;
        }
        if (key.equals("=")) {
            bg = PINK;
            fg = BG_DARK;
        }
        if (key.equals("C")) {
            bg = 0xFF5A2547;
            fg = 0xFFFFD6E8;
        }
        Button b = new Button(this);
        b.setText(key);
        b.setTextSize(18);
        b.setAllCaps(false);
        b.setTextColor(fg);
        b.setBackground(rounded(bg, key.equals("=") ? PINK : 0xFFFFFFFF & 0x22FFFFFF, 14));
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f));
        lp.width = 0;
        lp.height = 0;
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        b.setLayoutParams(lp);
        return b;
    }

    private View buildAnimeTools() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(10), dp(16), dp(10));

        TextView t1 = section("📺  Progreso de anime");
        panel.addView(t1);

        totalEp = field("Episodios totales", "12");
        seenEp = field("Episodios vistos", "5");
        minutesEp = field("Minutos por episodio", "24");
        panel.addView(totalEp);
        panel.addView(seenEp);
        panel.addView(minutesEp);

        Button calcProgress = wideButton("Calcular progreso ✦");
        calcProgress.setOnClickListener(v -> calculateProgress());
        panel.addView(calcProgress);

        progressResult = resultView("Introduce los datos y pulsa calcular.");
        panel.addView(progressResult);

        TextView t2 = section("⭐  Promedio de notas");
        panel.addView(t2);

        notesInput = field("Notas separadas por coma", "8.5, 9, 7.5");
        panel.addView(notesInput);

        Button calcNotes = wideButton("Calcular promedio ✦");
        calcNotes.setOnClickListener(v -> calculateNotes());
        panel.addView(calcNotes);

        notesResult = resultView("Escribe varias notas y pulsa calcular.");
        panel.addView(notesResult);

        TextView t3 = section("⬇️  Copia de seguridad / actualizaciones");
        panel.addView(t3);

        TextView info = new TextView(this);
        info.setText("El código y el APK de cada versión están en GitHub. Pulsa el botón para comprobar si hay una versión nueva.");
        info.setTextSize(12);
        info.setTextColor(MUTED);
        info.setPadding(dp(4), dp(2), dp(4), dp(10));
        panel.addView(info);

        Button update = wideButton("🔄  Buscar actualización");
        update.setOnClickListener(v -> checkForUpdate());
        panel.addView(update);

        Button openRepo = wideButton("🌐  Abrir GitHub");
        openRepo.setOnClickListener(v -> openUrl(REPO_URL));
        panel.addView(openRepo);

        scroll.addView(panel);
        return scroll;
    }

    private TextView section(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(17);
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        t.setTextColor(PINK);
        t.setPadding(dp(4), dp(16), dp(4), dp(8));
        return t;
    }

    private EditText field(String hint, String sample) {
        EditText e = new EditText(this);
        e.setHint(hint + "  (ej. " + sample + ")");
        e.setHintTextColor(0xFF8C7CB5);
        e.setTextColor(TEXT);
        e.setTextSize(15);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        e.setBackground(rounded(PANEL, PURPLE, 10));
        e.setPadding(dp(12), dp(10), dp(12), dp(10));
        e.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) e.getLayoutParams();
        lp.bottomMargin = dp(8);
        e.setLayoutParams(lp);
        return e;
    }

    private Button wideButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTextColor(TEXT);
        b.setBackground(rounded(OP_BG, PINK, 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        lp.bottomMargin = dp(8);
        b.setLayoutParams(lp);
        return b;
    }

    private TextView resultView(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(14);
        t.setTextColor(0xFFD8CBFF);
        t.setBackground(rounded(PANEL, 0xFF4B3B72, 10));
        t.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(6);
        t.setLayoutParams(lp);
        return t;
    }

    // ---------------- Calculator logic ----------------

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
        compute();
        history = format(first) + " " + pendingOp + " " + format(parse(input)) + " =";
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

    // ---------------- Anime tools ----------------

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

    // ---------------- GitHub update check ----------------

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
}
