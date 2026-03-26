# Migo Android Demo

Android sample project for the [Migo](https://github.com/minigame-labs/migo) mini-game runtime engine.

## Quick Start

### 1. Build Migo AAR

This demo currently depends on the local `../migo` build output:

- `../migo/platforms/android/dist/migo-debug.aar`

Build it first in the `migo` repo:

```bash
cd ../migo
bash scripts/build-aar.sh debug
```

If you want release AAR instead, build:

```bash
cd ../migo
bash scripts/build-aar.sh release
```

Then adjust the AAR path in `app/build.gradle.kts` if needed.

### 2. Prepare Game Files

Game files should be placed in the app's private directory with the following path format:

```
/data/data/com.minigame.androiddemo/files/migo/games/{gameId}/code/
├── game.js          # Game entry file
├── images/          # Image assets
├── workers/         # Worker scripts (if used)
└── ...              # Other resources
```

Where `{gameId}` is a unique game identifier (alphanumeric, underscore, hyphen, 1-64 characters).

Example: Push a `demo` game:

```bash
adb push your-game/ /data/data/com.minigame.androiddemo/files/migo/games/demo/code/
```

> Note: the SDK no longer auto-reads `game.json` for `workers` / `subPackages`.
> This demo now reads `code/game.json` on the host side and injects these fields into `RuntimeConfig`.
> You can also inject them manually:

```java
RuntimeConfig config = new RuntimeConfig.Builder(context)
    .setWorkersPath("workers")
    // .addSubPackage("stage1", "subpackages/stage1")
    .build();
```

### 3. Build and Run

Open the project in Android Studio, or build via command line:

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Three Integration Approaches

The demo showcases three integration approaches, in order of increasing complexity:

### Approach 1: MigoGameActivity (Simplest)

Launch a game with one line of code. The SDK handles all lifecycle, Surface management, and touch events internally.

```java
import com.migo.runtime.MigoGameActivity;

// One-line launch
MigoGameActivity.launch(context, "demo", "game.js");

// Or with custom config
RuntimeConfig config = new RuntimeConfig.Builder(context)
    .setDebugEnabled(true)
    .build();
MigoGameActivity.launch(context, "demo", "game.js", config);
```

### Approach 2: Custom Activity (Full Control)

Manually manage GameSession for scenarios that require custom UI overlays, custom error handling, or integration with other Android components.

```java
import com.migo.runtime.MigoRuntime;
import com.migo.runtime.GameSession;
import com.migo.runtime.RuntimeConfig;

// Create configuration
RuntimeConfig config = new RuntimeConfig.Builder(context)
    .setTargetFps(60)
    .setDebugEnabled(true)
    .setCodeSigningEnabled(false)
    .build();

// Create session (safe version, no exceptions)
MigoRuntime.Result<GameSession> result = MigoRuntime.getInstance()
    .createSessionSafe(activity, surface, config, "demo");

if (result.isSuccess()) {
    GameSession session = result.getValue();
    session.setListener(listener);

    // Latest API: optional host handlers (prefer before startGame)
    session.setAuthHandler(authHandler);
    session.setGameLogHandler(gameLogHandler);
    session.setSubpackageHandler(subpackageHandler);

    session.startGameSafe("game.js");
}

// Lifecycle management
session.pause();    // Activity.onPause()
session.resume();   // Activity.onResume()
session.restart();  // Restart game
session.close();    // Activity.onDestroy()
```

### Approach 3: MigoGameView (Embedded)

Embed the game as a View in any layout. Ideal when you need native UI elements around the game.

```java
import com.migo.runtime.MigoGameView;

MigoGameView gameView = new MigoGameView(context);

// Configure
RuntimeConfig config = new RuntimeConfig.Builder(context)
    .setDebugEnabled(true)
    .build();
gameView.setConfig(config);

// Set listener
gameView.setGameListener(listener);

// Add to layout
myLayout.addView(gameView);

// Load game
gameView.loadGame("demo", "game.js");

// MigoGameView creates session internally, so register handlers when available
GameSession session = gameView.getSession();
if (session != null) {
    session.setAuthHandler(authHandler);
    session.setGameLogHandler(gameLogHandler);
    session.setSubpackageHandler(subpackageHandler);
}
```

## Event Handling

```java
import com.migo.runtime.callback.GameSessionListener;

session.setListener(new GameSessionListener() {
    @Override
    public void onGameReady() {
        // Game loaded and ready - hide loading screen
    }

    @Override
    public void onGameExit(int exitCode) {
        // Game exited, exitCode == 0 means normal exit
    }

    @Override
    public void onError(int errorCode, String message, boolean recoverable) {
        // Runtime error
        // recoverable=true: non-fatal, game can continue
        // recoverable=false: fatal, should close
    }
});
```

## Host Handlers (Latest API)

`GameSession` now supports these host callback handlers:

- `setAuthHandler(AuthHandler)`: handles `wx.login` / `wx.checkSession` / `wx.getUserInfo` / `wx.getPhoneNumber`
- `setGameLogHandler(GameLogHandler)`: receives game-reported logs (JSON)
- `setSubpackageHandler(SubpackageHandler)`: handles `loadSubpackage` / `preDownloadSubpackage`

Sample implementations in this demo:

- `app/src/main/java/com/minigame/androiddemo/auth/ProxyAuthHandler.java`
- `app/src/main/java/com/minigame/androiddemo/DemoGameLogHandler.java`
- `app/src/main/java/com/minigame/androiddemo/DemoSubpackageHandler.java`

For auth proxy operation details, see: `AUTH_PROXY_RUNBOOK.md`

## Project Structure

```
app/
└── src/main/
    ├── java/.../
    │   ├── MainActivity.java           # Demo selector
    │   ├── CustomGameActivity.java     # Approach 2: Manual GameSession control
    │   ├── EmbeddedGameActivity.java   # Approach 3: MigoGameView embedding
    │   ├── DemoGameLogHandler.java     # GameLogHandler sample
    │   ├── DemoSubpackageHandler.java  # SubpackageHandler sample
    │   ├── auth/
    │   │   └── ProxyAuthHandler.java   # AuthHandler sample
    │   └── ui/
    │       └── CapsuleMenu.java        # Capsule menu UI component
    └── AndroidManifest.xml

AUTH_PROXY_RUNBOOK.md                    # Separate auth proxy runbook
```

## Game Directory Structure

```
/data/data/{packageName}/files/migo/games/{gameId}/
├── code/           # Game code directory (read-only)
│   ├── game.js
│   └── images/
└── user_data/      # User data directory (read-write)

/data/data/{packageName}/cache/migo/games/{gameId}/
└── tmp/            # Temporary directory (auto-cleaned on session close)
```

## Core SDK Classes

| Class | Description |
|-------|-------------|
| `MigoRuntime` | SDK entry point (singleton), device checks, session creation |
| `GameSession` | Game session, manages lifecycle, input, Surface |
| `RuntimeConfig` | Configuration (Builder pattern), FPS/debug/signing etc. |
| `MigoGameActivity` | Ready-to-use Activity, one-line game launch |
| `MigoGameView` | Embeddable FrameLayout, auto lifecycle management |
| `GameSessionListener` | Event callback interface |
| `ErrorCode` | Error code constants |
| `DebugOverlayView` | Debug overlay (auto-shown in debug mode) |

## License

MIT License
