package com.minigame.androiddemo;

import android.app.Activity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.migo.runtime.MigoRuntime;
import com.migo.runtime.RuntimeConfig;

/**
 * Demo launcher Activity.
 * <p>
 * Shows three integration approaches:
 * <ol>
 *   <li><b>MigoGameActivity</b> - Zero-boilerplate, launch with one line</li>
 *   <li><b>Custom Activity</b> - Full control with manual GameSession management</li>
 *   <li><b>MigoGameView</b> - Embed a game inside any layout</li>
 * </ol>
 */
public class MainActivity extends Activity {

    private static final String TAG = "MigoDemo";

    // Game configuration
    private static final String GAME_ID = "migo-test-suit";
    private static final String GAME_ENTRY = "game.js";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request permissions that game APIs might need
        requestPermissionsIfNeeded();

        MigoRuntime runtime = MigoRuntime.getInstance();
        if (!runtime.isDeviceSupported()) {
            Log.e(TAG, "Device not supported");
            finish();
            return;
        }

        Log.i(TAG, "Migo Runtime v" + runtime.getVersion()
                + " (native: " + runtime.getNativeVersion() + ")");

        // Build a simple launcher UI
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(48), dp(24), dp(24));

        TextView title = new TextView(this);
        title.setText("Migo Android Demo");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);

        TextView version = new TextView(this);
        version.setText("v" + runtime.getVersion());
        version.setTextSize(14);
        version.setGravity(Gravity.CENTER);
        version.setPadding(0, dp(4), 0, dp(32));

        // Option 1: MigoGameActivity (simplest)
        Button btn1 = createButton("1. MigoGameActivity (Simplest)");
        btn1.setOnClickListener(v -> launchWithGameActivity());

        // Option 2: Custom Activity with full GameSession control
        Button btn2 = createButton("2. Custom Activity (Full Control)");
        btn2.setOnClickListener(v -> launchCustomActivity());

        // Option 3: Embedded MigoGameView
        Button btn3 = createButton("3. Embedded MigoGameView");
        btn3.setOnClickListener(v -> launchEmbeddedView());

        root.addView(title);
        root.addView(version);
        root.addView(btn1, buttonParams());
        root.addView(btn2, buttonParams());
        root.addView(btn3, buttonParams());

        setContentView(root);
    }

    /**
     * Option 1: Launch game using MigoGameActivity.
     * This is the simplest integration - one line of code.
     */
    private void launchWithGameActivity() {
        RuntimeConfig config = new RuntimeConfig.Builder(this)
                .setDebugEnabled(true)
                .setCodeSigningEnabled(false)
                .build();
        com.migo.runtime.MigoGameActivity.launch(this, GAME_ID, GAME_ENTRY, config);
    }

    /**
     * Option 2: Launch game using a custom Activity with full control.
     */
    private void launchCustomActivity() {
        Intent intent = new Intent(this, CustomGameActivity.class);
        intent.putExtra("game_id", GAME_ID);
        intent.putExtra("entry_point", GAME_ENTRY);
        startActivity(intent);
    }

    /**
     * Option 3: Launch the embedded MigoGameView demo.
     */
    private void launchEmbeddedView() {
        startActivity(new Intent(this, EmbeddedGameActivity.class));
    }

    // ---- Helpers ----

    private void requestPermissionsIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            String[] perms = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
            };
            for (String perm : perms) {
                if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(perms, 200);
                    break;
                }
            }
        }
    }

    private Button createButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setAllCaps(false);
        btn.setTextSize(16);
        return btn;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        return lp;
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
