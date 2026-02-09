package com.minigame.androiddemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.view.ViewGroup;

import com.migo.runtime.DebugOverlayView;
import com.migo.runtime.ErrorCode;
import com.migo.runtime.GameSession;
import com.migo.runtime.MigoRuntime;
import com.migo.runtime.RuntimeConfig;
import com.migo.runtime.callback.OnErrorListener;
import com.migo.runtime.callback.OnGameEventListener;
import com.migo.runtime.callback.OnLifecycleListener;
import com.minigame.androiddemo.ui.CapsuleMenu;

/**
 * Sample Activity demonstrating Migo Runtime integration.
 * <p>
 * This example shows:
 * - Creating and configuring the runtime
 * - Managing the game session lifecycle
 * - Handling touch events
 * - Using callbacks for game events and errors
 */
public class MainActivity extends Activity {

    private static final String TAG = "GameActivity";

    private GameSession session;
    private SurfaceView surfaceView;
    private FrameLayout rootLayout;

    // Game configuration (adjust to your needs)
    // gameId is used to create isolated directories in:
    //   files/migo/games/{gameId}/ and cache/migo/games/{gameId}/
    private static final String GAME_ID = "demo";
//    private static final String GAME_ID = "migo-test-suit";
    private static final String GAME_ENTRY = "game.js";
    
    // UI
    private CapsuleMenu capsuleMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if runtime is supported
        MigoRuntime runtime = MigoRuntime.getInstance();
        if (!runtime.isDeviceSupported()) {
            Log.e(TAG, "Device not supported");
            finish();
            return;
        }

        Log.i(TAG, "Migo Runtime v" + runtime.getVersion() + " (native: " + runtime.getNativeVersion() + ")");

        // Create root layout
        rootLayout = new FrameLayout(this);

        // Create surface view for rendering
        surfaceView = new SurfaceView(this);
        rootLayout.addView(surfaceView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(rootLayout);

        // Add Capsule Menu
        addCapsuleMenu();

        // Set up surface callbacks
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (session != null && session.isValid()) {
                    // Surface recreated (e.g., returning from background).
                    // Re-attach the new surface so the render thread can recreate
                    // its EGL onscreen context.
                    session.updateSurface(holder.getSurface());
                    Log.d(TAG, "Surface recreated, updated session surface");
                } else {
                    // First-time initialization.
                    initializeGame(holder);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (session != null && session.isValid()) {
                    session.updateSurface(holder.getSurface());
                    Log.d(TAG, "Surface changed: " + width + "x" + height);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // Do NOT destroy the session when the surface is lost.
                // On Android, the surface is destroyed when the activity goes
                // to background (onStop). The session is paused via onPause()
                // and will be resumed when surfaceCreated is called again.
                // Full cleanup happens in onDestroy().
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

    private void addCapsuleMenu() {
        capsuleMenu = new CapsuleMenu(this, new CapsuleMenu.OnMenuActionListener() {
            @Override
            public void onRestart() {
                restartGame();
            }

            @Override
            public void onExit() {
                finish();
            }
        });

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        
        lp.gravity = Gravity.TOP | Gravity.END;
        float density = getResources().getDisplayMetrics().density;
        // Position it below the status bar (approx) and with right margin
        // Assuming status bar + padding is around 40-50dp
        lp.topMargin = (int) (40 * density); 
        lp.rightMargin = (int) (10 * density); 
        
        // Ensure it's above other views
        capsuleMenu.setElevation(10 * density);
        
        rootLayout.addView(capsuleMenu, lp);
    }

    private void restartGame() {
        Log.i(TAG, "Restarting game...");
        if (session != null) {
            // Use native restart command which is more efficient and reliable
            // as it preserves the surface and handles cleanup internally.
            session.restart();
        } else {
            // Fallback to full re-initialization if session is null
            initializeGame(surfaceView.getHolder());
        }
    }

    /**
     * Initialize the game session.
     */
    private void initializeGame(SurfaceHolder holder) {
        // Build configuration
        RuntimeConfig config = new RuntimeConfig.Builder(this).setTargetFps(60).setDebugEnabled(true).setLogLevel(RuntimeConfig.LogLevel.DEBUG).build();

        Log.d(TAG, "Config: " + config);

        // Create session (safe version with error handling)
        // The gameId is used to create isolated directories for this game
        MigoRuntime.Result<GameSession> result = MigoRuntime.getInstance().createSessionSafe(this, holder.getSurface(), config, GAME_ID);

        if (result.isFailure()) {
            Log.e(TAG, "Failed to create session: " + ErrorCode.getMessage(result.getErrorCode()) + " - " + result.getErrorMessage());
            finish();
            return;
        }

        session = result.getValue();

        // Add debug overlay if available
        DebugOverlayView overlay = session.getDebugOverlay();
        if (overlay != null) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP | Gravity.END);

            float density = getResources().getDisplayMetrics().density;
            int margin = (int) (8 * density);
            int topMargin = (int) (30 * density);

            // Move debug overlay to left side to avoid overlap with CapsuleMenu
            lp.gravity = Gravity.TOP | Gravity.START;
            lp.setMargins(margin, topMargin, 0, 0);
            
            rootLayout.addView(overlay, lp);
        }

        // Set up callbacks
        setupCallbacks();

        // Start the game - path generation is handled by native layer
        // Game code should be deployed to session.getPaths().getCodeDir() before this call
        int startResult = session.startGameSafe(GAME_ENTRY);
        if (startResult != ErrorCode.SUCCESS) {
            Log.e(TAG, "Failed to start game: " + ErrorCode.getMessage(startResult));
            // Don't finish - let the error callback handle it
        }
    }

    /**
     * Set up game callbacks.
     */
    private void setupCallbacks() {
        // Game events
        session.setOnGameEventListener(new OnGameEventListener() {
            @Override
            public void onGameReady() {
                Log.i(TAG, "Game is ready!");
                runOnUiThread(() -> {
                    // Hide loading screen, start animations, etc.
                });
            }

            @Override
            public void onGameExit(int exitCode) {
                Log.i(TAG, "Game exited with code: " + exitCode);
                runOnUiThread(() -> {
                    if (exitCode == 0) {
                        finish();
                    } else {
                        // Show error or restart option
                    }
                });
            }

            @Override
            public void onLoadingStart() {
                Log.d(TAG, "Loading started");
                runOnUiThread(() -> {
                    // Show loading indicator
                });
            }

            @Override
            public void onLoadingEnd() {
                Log.d(TAG, "Loading ended");
                runOnUiThread(() -> {
                    // Hide loading indicator
                });
            }

            @Override
            public void onLoadingProgress(float progress, String message) {
                Log.d(TAG, "Loading: " + (int) (progress * 100) + "% - " + message);
                runOnUiThread(() -> {
                    // Update progress bar
                });
            }
        });

        // Error handling
        session.setOnErrorListener((errorCode, message, recoverable) -> {
            Log.e(TAG, "Error [" + errorCode + "]: " + message + " (recoverable: " + recoverable + ")");

            if (!recoverable) {
                runOnUiThread(() -> {
                    // Show error dialog and finish
                    showErrorAndFinish(message);
                });
            }
        });

        // Lifecycle events
        session.setOnLifecycleListener(new OnLifecycleListener() {
            @Override
            public void onInitialized() {
                Log.d(TAG, "Runtime initialized");
            }

            @Override
            public void onDestroyed() {
                Log.d(TAG, "Runtime destroyed");
            }

            @Override
            public void onPaused() {
                Log.d(TAG, "Runtime paused");
            }

            @Override
            public void onResumed() {
                Log.d(TAG, "Runtime resumed");
            }

            @Override
            public void onSurfaceChanged(int width, int height) {
                Log.d(TAG, "Surface: " + width + "x" + height);
            }
        });
    }

    /**
     * Clean up the game session.
     */
    private void destroyGame() {
        if (session != null) {
            session.close();
            session = null;
            Log.d(TAG, "Game session destroyed");
        }
    }

    /**
     * Show error dialog and finish activity.
     */
    private void showErrorAndFinish(String message) {
        new android.app.AlertDialog.Builder(this).setTitle("Error").setMessage(message).setPositiveButton("OK", (d, w) -> finish()).setCancelable(false).show();
    }

    // ==================== Activity Lifecycle ====================

    @Override
    protected void onPause() {
        super.onPause();
        if (session != null && session.isValid()) {
            session.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session != null && session.isValid()) {
            session.resume();
        }
    }

    @Override
    protected void onDestroy() {
        destroyGame();
        super.onDestroy();
    }
}

