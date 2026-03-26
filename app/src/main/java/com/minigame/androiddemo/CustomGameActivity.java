package com.minigame.androiddemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.migo.runtime.ErrorCode;
import com.migo.runtime.GameSession;
import com.migo.runtime.MigoRuntime;
import com.migo.runtime.RuntimeConfig;
import com.migo.runtime.callback.GameSessionListener;
import com.minigame.androiddemo.auth.ProxyAuthHandler;
import com.minigame.androiddemo.ui.CapsuleMenu;

/**
 * Demo Activity with full manual GameSession control.
 * <p>
 * This approach gives you maximum control over:
 * <ul>
 *   <li>Surface creation and lifecycle</li>
 *   <li>Touch event dispatching</li>
 *   <li>Session pause/resume/restart/close</li>
 *   <li>Error handling and recovery</li>
 * </ul>
 * <p>
 * For a simpler integration, see {@link com.migo.runtime.MigoGameActivity}.
 */
public class CustomGameActivity extends Activity {

    private static final String TAG = "CustomGameActivity";
    public static final String EXTRA_AUTH_RELAY_URL = "auth_relay_url";

    // Auth proxy relay server URL.
    // - Emulator:    "http://10.0.2.2:9527"
    // - Real device: "http://<PC_LAN_IP>:9527" (e.g. "http://192.168.1.100:9527")
    private static final String DEFAULT_AUTH_RELAY_URL = "http://10.0.2.2:9527";

    private GameSession session;
    private SurfaceView surfaceView;
    private FrameLayout rootLayout;
    private CapsuleMenu capsuleMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String gameId = getIntent().getStringExtra("game_id");
        String entryPoint = getIntent().getStringExtra("entry_point");
        if (gameId == null) gameId = "demo";
        if (entryPoint == null) entryPoint = "game.js";
        String relayUrl = getIntent().getStringExtra(EXTRA_AUTH_RELAY_URL);
        if (relayUrl == null || relayUrl.trim().isEmpty()) {
            relayUrl = DEFAULT_AUTH_RELAY_URL;
        }

        final String finalGameId = gameId;
        final String finalEntryPoint = entryPoint;
        final String finalRelayUrl = relayUrl;

        // Create root layout
        rootLayout = new FrameLayout(this);

        // Create surface view for rendering
        surfaceView = new SurfaceView(this);
        rootLayout.addView(surfaceView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(rootLayout);

        // Add capsule menu overlay
        addCapsuleMenu();

        // Set up surface callbacks
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (session != null && session.isValid()) {
                    // Surface recreated (e.g., returning from background).
                    session.updateSurface(holder.getSurface());
                    Log.d(TAG, "Surface recreated, updated session surface");
                } else {
                    // First-time initialization.
                    initializeGame(holder, finalGameId, finalEntryPoint, finalRelayUrl);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (session != null && session.isValid()) {
                    session.updateSurface(holder.getSurface());
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // Keep session alive - cleanup happens in onDestroy().
                Log.d(TAG, "Surface destroyed (session kept alive)");
            }
        });

        // Set up touch handling
        surfaceView.setOnTouchListener((v, event) -> {
            if (session != null && session.isValid()) {
                return session.dispatchTouchEvent(event);
            }
            return false;
        });
    }

    private void initializeGame(SurfaceHolder holder, String gameId, String entryPoint, String relayUrl) {
        // Build configuration
        RuntimeConfig.Builder builder = new RuntimeConfig.Builder(this)
                .setTargetFps(60)
                .setDebugEnabled(true)
                .setLogLevel(RuntimeConfig.LogLevel.DEBUG)
                .setCodeSigningEnabled(false)
                ;
        RuntimeConfigCompat.injectFromGameConfig(builder, GameConfigLoader.load(this, gameId));
        RuntimeConfig config = builder.build();

        // Create session (safe version with error handling)
        MigoRuntime.Result<GameSession> result = MigoRuntime.getInstance()
                .createSessionSafe(this, holder.getSurface(), config, gameId);

        if (result.isFailure()) {
            Log.e(TAG, "Failed to create session: "
                    + ErrorCode.getMessage(result.getErrorCode())
                    + " - " + result.getErrorMessage());
            finish();
            return;
        }

        session = result.getValue();

        // Register latest host handlers before startGame() for best compatibility.
        session.setGameLogHandler(new DemoGameLogHandler());
        session.setSubpackageHandler(new DemoSubpackageHandler(session.getPaths().getCodeDir()));

        // Register auth handler — proxies wx.login/checkSession/getUserInfo to
        // a relay server that forwards to a real WeChat instance on PC.
        session.setAuthHandler(new ProxyAuthHandler(relayUrl, gameId));
        Log.i(TAG, "Host handlers enabled: auth/gameLog/subpackage, relay=" + relayUrl);

        // Set up callbacks
        session.setListener(new GameSessionListener() {
            @Override
            public void onGameReady() {
                Log.i(TAG, "Game is ready!");
            }

            @Override
            public void onGameExit(int exitCode) {
                Log.i(TAG, "Game exited with code: " + exitCode);
                runOnUiThread(() -> {
                    if (exitCode == 0) finish();
                });
            }

            @Override
            public void onError(int errorCode, String message, boolean recoverable) {
                Log.e(TAG, "Error [" + errorCode + "]: " + message
                        + " (recoverable: " + recoverable + ")");
                if (!recoverable) {
                    runOnUiThread(() -> showErrorAndFinish(message));
                }
            }
        });

        // Start the game
        int startResult = session.startGameSafe(entryPoint);
        if (startResult != ErrorCode.SUCCESS) {
            Log.e(TAG, "Failed to start game: " + ErrorCode.getMessage(startResult));
        }
    }

    private void addCapsuleMenu() {
        capsuleMenu = new CapsuleMenu(this, new CapsuleMenu.OnMenuActionListener() {
            @Override
            public void onRestart() {
                if (session != null) session.restart();
            }

            @Override
            public void onExit() {
                finish();
            }
        });

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        float density = getResources().getDisplayMetrics().density;
        lp.gravity = Gravity.TOP | Gravity.END;
        lp.topMargin = (int) (40 * density);
        lp.rightMargin = (int) (10 * density);
        capsuleMenu.setElevation(10 * density);

        rootLayout.addView(capsuleMenu, lp);
    }

    private void showErrorAndFinish(String message) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    // ==================== Activity Lifecycle ====================

    @Override
    protected void onPause() {
        super.onPause();
        if (session != null && session.isValid()) session.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session != null && session.isValid()) session.resume();
    }

    @Override
    protected void onDestroy() {
        if (session != null) {
            session.close();
            session = null;
        }
        super.onDestroy();
    }
}
