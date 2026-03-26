package com.minigame.androiddemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.migo.runtime.MigoGameActivity;
import com.migo.runtime.RuntimeConfig;
import com.migo.runtime.callback.GameSessionListener;

import java.lang.reflect.Field;

/**
 * Diagnostic wrapper for MigoGameActivity.
 *
 * Adds high-signal logs for game startup key path:
 * - Activity lifecycle
 * - Surface lifecycle
 * - Game session listener callbacks
 */
public class DebugMigoGameActivity extends MigoGameActivity {

    private static final String TAG = "DebugMigoGameAct";

    public static void launch(Context context, String gameId, String entryPoint, RuntimeConfig config) {
        if (config != null) {
            applyPendingConfigForBaseActivity(config);
        }
        Intent intent = new Intent(context, DebugMigoGameActivity.class);
        intent.putExtra(EXTRA_GAME_ID, gameId);
        intent.putExtra(EXTRA_ENTRY_POINT, entryPoint);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private static void applyPendingConfigForBaseActivity(RuntimeConfig config) {
        try {
            Field lockField = MigoGameActivity.class.getDeclaredField("sConfigLock");
            Field pendingField = MigoGameActivity.class.getDeclaredField("sPendingConfig");
            lockField.setAccessible(true);
            pendingField.setAccessible(true);
            Object lock = lockField.get(null);
            synchronized (lock) {
                pendingField.set(null, config);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to apply pending config via reflection, fallback to default", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String gameId = getIntent().getStringExtra(EXTRA_GAME_ID);
        String entryPoint = getIntent().getStringExtra(EXTRA_ENTRY_POINT);
        Log.i(TAG, "onCreate gameId=" + gameId + ", entryPoint=" + entryPoint);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated valid=" + (holder != null && holder.getSurface() != null
                && holder.getSurface().isValid()));
        super.surfaceCreated(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged format=" + format + ", size=" + width + "x" + height);
        super.surfaceChanged(holder, format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        super.surfaceDestroyed(holder);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected GameSessionListener onCreateGameListener() {
        return new GameSessionListener() {
            @Override
            public void onGameReady() {
                Log.i(TAG, "listener.onGameReady");
            }

            @Override
            public void onGameExit(int exitCode) {
                Log.i(TAG, "listener.onGameExit exitCode=" + exitCode);
            }

            @Override
            public void onError(int errorCode, String message, boolean recoverable) {
                Log.e(TAG, "listener.onError code=" + errorCode + ", recoverable="
                        + recoverable + ", message=" + message);
            }

            @Override
            public void onLoadingStart() {
                Log.i(TAG, "listener.onLoadingStart");
            }

            @Override
            public void onLoadingEnd() {
                Log.i(TAG, "listener.onLoadingEnd");
            }

            @Override
            public void onLoadingProgress(float progress, String message) {
                Log.i(TAG, "listener.onLoadingProgress progress=" + progress + ", message=" + message);
            }

            @Override
            public void onPaused() {
                Log.i(TAG, "listener.onPaused");
            }

            @Override
            public void onResumed() {
                Log.i(TAG, "listener.onResumed");
            }

            @Override
            public void onDestroyed() {
                Log.i(TAG, "listener.onDestroyed");
            }
        };
    }
}
