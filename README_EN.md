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

## Project Structure

```
app/
├── libs/
│   └── migo.aar                  # Migo SDK (manual placement required)
└── src/main/
    ├── java/.../
    │   ├── MainActivity.java     # Main Activity
    └── AndroidManifest.xml
```

## SDK API Usage

### Initialization

```java
import com.migo.runtime.MigoRuntime;
import com.migo.runtime.GameSession;
import com.migo.runtime.RuntimeConfig;

// Create configuration
RuntimeConfig config = new RuntimeConfig.Builder(context)
    .setDebugEnabled(true)
    .build();

// Create session
GameSession session = MigoRuntime.getInstance()
    .createSession(activity, surface, config, "demo");  // "demo" is the gameId

// Start Game
session.startGame("game.js");

```

### Handle Input

```java
@Override
public boolean onTouchEvent(MotionEvent event) {
    return session.dispatchTouchEvent(event);
}
```

### Lifecycle Management

```java
// Activity.onResume()
session.resume();

// Activity.onPause()
session.pause();

// Activity.onDestroy()
session.close();
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

## License

MIT License
