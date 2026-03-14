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

游戏文件需要放置到设备的应用私有目录，路径格式为：

```
/data/data/com.minigame.androiddemo/files/migo/games/{gameId}/code/
├── game.js          # 游戏入口文件
├── images/          # 图片资源
└── ...              # 其他资源文件
```

其中 `{gameId}` 是游戏的唯一标识符（字母数字、下划线、连字符，1-64字符）。

示例：推送 `demo` 游戏：

```bash
adb push your-game/ /data/data/com.minigame.androiddemo/files/migo/games/demo/code/
```

### 3. 构建运行

使用 Android Studio 打开项目，或通过命令行构建：

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 三种集成方式

Demo 提供了三种不同的集成方式，按复杂度递增排列：

### 方式 1：MigoGameActivity（最简单）

一行代码启动游戏，SDK 内部处理全部生命周期、Surface 管理和触摸事件。

```java
import com.migo.runtime.MigoGameActivity;

// 一行启动
MigoGameActivity.launch(context, "demo", "game.js");

// 或带自定义配置
RuntimeConfig config = new RuntimeConfig.Builder(context)
    .setDebugEnabled(true)
    .build();
MigoGameActivity.launch(context, "demo", "game.js", config);
```

### 方式 2：自定义 Activity（完全控制）

手动管理 GameSession，适合需要自定义 UI 叠加层、自定义错误处理、或嵌入其他 Android 组件的场景。

```java
import com.migo.runtime.MigoRuntime;
import com.migo.runtime.GameSession;
import com.migo.runtime.RuntimeConfig;

// 创建配置
RuntimeConfig config = new RuntimeConfig.Builder(context)
    .setTargetFps(60)
    .setDebugEnabled(true)
    .setCodeSigningEnabled(false)
    .build();

// 创建会话（安全版本，不抛异常）
MigoRuntime.Result<GameSession> result = MigoRuntime.getInstance()
    .createSessionSafe(activity, surface, config, "demo");

if (result.isSuccess()) {
    GameSession session = result.getValue();
    session.setListener(listener);
    session.startGameSafe("game.js");
}

// 生命周期管理
session.pause();    // Activity.onPause()
session.resume();   // Activity.onResume()
session.restart();  // 重启游戏
session.close();    // Activity.onDestroy()
```

### 方式 3：MigoGameView（嵌入式）

将游戏作为 View 嵌入到任意布局中，适合需要在游戏周围放置原生 UI 元素的场景。

```java
import com.migo.runtime.MigoGameView;

MigoGameView gameView = new MigoGameView(context);

// 配置
RuntimeConfig config = new RuntimeConfig.Builder(context)
    .setDebugEnabled(true)
    .build();
gameView.setConfig(config);

// 设置监听器
gameView.setGameListener(listener);

// 添加到布局
myLayout.addView(gameView);

// 加载游戏
gameView.loadGame("demo", "game.js");
```

## 监听事件

```java
import com.migo.runtime.callback.GameSessionListener;

session.setListener(new GameSessionListener() {
    @Override
    public void onGameReady() {
        // 游戏加载完成，可以隐藏加载画面
    }

    @Override
    public void onGameExit(int exitCode) {
        // 游戏退出，exitCode == 0 表示正常退出
    }

    @Override
    public void onError(int errorCode, String message, boolean recoverable) {
        // 运行时错误
        // recoverable=true: 可恢复错误（可继续运行）
        // recoverable=false: 致命错误（建议关闭）
    }
});
```

## 项目结构

```
app/
├── libs/
│   └── migo.aar                        # Migo SDK（需手动放置）
└── src/main/
    ├── java/.../
    │   ├── MainActivity.java           # Demo 选择器
    │   ├── CustomGameActivity.java     # 方式 2: 手动 GameSession 管理
    │   ├── EmbeddedGameActivity.java   # 方式 3: MigoGameView 嵌入
    │   └── ui/
    │       └── CapsuleMenu.java        # 胶囊菜单 UI 组件
    └── AndroidManifest.xml
```

## 游戏目录结构

```
/data/data/{packageName}/files/migo/games/{gameId}/
├── code/           # 游戏代码目录 (只读)
│   ├── game.js
│   └── images/
├── cache/          # 缓存目录 (可读写)
└── data/           # 数据目录 (可读写)
```

## SDK 核心类

| 类 | 说明 |
|---|------|
| `MigoRuntime` | SDK 入口（单例），设备检查、会话创建 |
| `GameSession` | 游戏会话，管理生命周期、输入、Surface |
| `RuntimeConfig` | 配置（Builder 模式），FPS/调试/签名等 |
| `MigoGameActivity` | 开箱即用的 Activity，一行代码启动游戏 |
| `MigoGameView` | 可嵌入的 FrameLayout，自动管理生命周期 |
| `GameSessionListener` | 事件回调接口 |
| `ErrorCode` | 错误码常量 |
| `DebugOverlayView` | 调试面板（debug 模式自动显示） |

## 许可证

MIT License
