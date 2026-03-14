package com.minigame.androiddemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.migo.runtime.MigoGameView;
import com.migo.runtime.RuntimeConfig;
import com.migo.runtime.callback.GameSessionListener;

/**
 * Demo Activity showing how to embed a game using MigoGameView.
 * <p>
 * MigoGameView is a self-contained FrameLayout that manages the entire
 * game lifecycle internally. Just add it to your layout, configure it,
 * and call {@code loadGame()}.
 * <p>
 * This is ideal when you want to embed a game alongside other UI elements
 * (e.g., a title bar, native buttons, ads, etc.).
 */
public class EmbeddedGameActivity extends Activity {

    private static final String TAG = "EmbeddedGameActivity";

    private static final String GAME_ID = "migo-test-suit";
    private static final String GAME_ENTRY = "game.js";

    private MigoGameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Root layout: vertical - title bar on top, game view below
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        // Title bar
        FrameLayout titleBar = new FrameLayout(this);
        titleBar.setBackgroundColor(0xFF333333);
        int barHeight = (int) (44 * getResources().getDisplayMetrics().density);

        TextView titleText = new TextView(this);
        titleText.setText("Embedded MigoGameView");
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setTextSize(16);
        FrameLayout.LayoutParams titleLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLp.gravity = Gravity.CENTER;
        titleBar.addView(titleText, titleLp);

        TextView closeBtn = new TextView(this);
        closeBtn.setText("X");
        closeBtn.setTextColor(0xFFFFFFFF);
        closeBtn.setTextSize(18);
        closeBtn.setPadding(dp(16), 0, dp(16), 0);
        closeBtn.setGravity(Gravity.CENTER);
        closeBtn.setOnClickListener(v -> finish());
        FrameLayout.LayoutParams closeLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        closeLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        titleBar.addView(closeBtn, closeLp);

        root.addView(titleBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, barHeight));

        // Game view - takes remaining space
        gameView = new MigoGameView(this);

        // Configure
        RuntimeConfig config = new RuntimeConfig.Builder(this)
                .setDebugEnabled(true)
                .setCodeSigningEnabled(false)
                .build();
        gameView.setConfig(config);

        // Set listener
        gameView.setGameListener(new GameSessionListener() {
            @Override
            public void onGameReady() {
                Log.i(TAG, "Embedded game is ready!");
            }

            @Override
            public void onGameExit(int exitCode) {
                Log.i(TAG, "Embedded game exited: " + exitCode);
                runOnUiThread(() -> finish());
            }

            @Override
            public void onError(int errorCode, String message, boolean recoverable) {
                Log.e(TAG, "Embedded game error [" + errorCode + "]: " + message);
            }
        });

        root.addView(gameView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

        setContentView(root);

        // Load game
        gameView.loadGame(GAME_ID, GAME_ENTRY);
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
