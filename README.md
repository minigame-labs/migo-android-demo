# Migo Android Demo

[Migo](https://github.com/minigame-labs/migo) 小游戏运行时引擎的 Android 示例项目。

## 快速开始

### 1. 获取 Migo AAR

从 [Migo Releases](https://github.com/minigame-labs/migo/releases) 下载最新的 `migo.aar`，或从源码构建：

```bash
git clone https://github.com/minigame-labs/migo.git
cd migo
./scripts/build-aar.ps1 -BuildType release
```

将生成的 AAR 文件复制到 `app/libs/migo.aar`。

### 2. 准备游戏文件

将小游戏文件放置到设备的应用私有目录：

```
/data/data/com.minigame.androiddemo/files/minigame/
├── game.js          # 游戏入口文件
└── ...              # 其他资源文件
```

可以通过 `adb push` 推送文件：

```bash
adb push your-game/ /data/data/com.minigame.androiddemo/files/minigame/
```

### 3. 构建运行

使用 Android Studio 打开项目，或通过命令行构建：

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 项目结构

```
app/
├── libs/
│   └── migo.aar              # Migo SDK（需手动放置）
└── src/main/
    ├── java/.../
    │   ├── MainActivity.java      # 主 Activity
    │   └── GameSurfaceView.java   # 游戏渲染 View
    └── AndroidManifest.xml
```

## 核心代码说明

### GameSurfaceView.java

展示了如何集成 Migo SDK：

1. **初始化引擎**：在 `surfaceCreated` 中调用 `MiniGameSDK.getInstance().initialize()`
2. **启动游戏**：调用 `host.startGame(codeDir, "game.js")`
3. **处理触摸**：将 `MotionEvent` 转发给 `host.onTouch()`
4. **生命周期**：在 Activity 的 `onResume`/`onPause`/`onDestroy` 中调用对应方法

## 许可证

MIT License
