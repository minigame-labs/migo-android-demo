# Migo Android Demo

Android sample project for the [Migo](https://github.com/minigame-labs/migo) mini-game runtime engine.

## Quick Start

### 1. Get Migo AAR

Download the latest `migo.aar` from [Migo Releases](https://github.com/minigame-labs/migo/releases), or build from source:

```bash
git clone https://github.com/minigame-labs/migo.git
cd migo
./scripts/build-aar.ps1 -BuildType release
```

Copy the generated AAR file to `app/libs/migo.aar`.

### 2. Prepare Game Files

Game files should be placed in the app's private directory with the following path format:

```
/data/data/com.minigame.androiddemo/files/migo/games/{gameId}/code/
├── game.js          # Game entry file
├── images/          # Image assets
└── ...              # Other resources
```

Where `{gameId}` is a unique game identifier (alphanumeric, underscore, hyphen, 1-64 characters).

Example: Push a `demo` game:

```bash
adb push your-game/ /data/data/com.minigame.androiddemo/files/migo/games/demo/code/
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

## Project Structure

```
app/
├── libs/
│   └── migo.aar                        # Migo SDK (manual placement required)
└── src/main/
    ├── java/.../
    │   ├── MainActivity.java           # Demo selector
    │   ├── CustomGameActivity.java     # Approach 2: Manual GameSession control
    │   ├── EmbeddedGameActivity.java   # Approach 3: MigoGameView embedding
    │   └── ui/
    │       └── CapsuleMenu.java        # Capsule menu UI component
    └── AndroidManifest.xml
```

## Game Directory Structure

```
/data/data/{packageName}/files/migo/games/{gameId}/
├── code/           # Game code directory (read-only)
│   ├── game.js
│   └── images/
├── cache/          # Cache directory (read-write)
└── data/           # Data directory (read-write)
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
