package com.minigame.androiddemo;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.migo.runtime.GameSession;
import com.migo.runtime.MigoRuntime;
import com.migo.runtime.RuntimeConfig;

public final class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String GAME_ID = "migo-test-suit";

    private GameSession session;
    private final Activity activity;

    public GameSurfaceView(Activity activity) {
        super(activity);
        this.activity = activity;
        getHolder().addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        RuntimeConfig config = new RuntimeConfig.Builder(getContext())
                .setDebugEnabled(true)
                .build();

        session = MigoRuntime.getInstance()
                .createSession(activity, holder.getSurface(), config, GAME_ID);

        if (session != null) {
            // Game code should be deployed to: filesDir/games/{gameId}/code/
            // e.g., /data/data/com.minigame.androiddemo/files/games/demo/code/game.js
            session.startGame("game.js");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (session != null) {
            session.updateSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (session != null) {
            return session.dispatchTouchEvent(event);
        }
        return false;
    }

    public void onResume() {
        if (session != null) {
            session.resume();
        }
    }

    public void onPause() {
        if (session != null) {
            session.pause();
        }
    }

    public void release() {
        if (session != null) {
            session.close();
            session = null;
        }
    }
}
