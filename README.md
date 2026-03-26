# Migo Android Demo

[Migo](https://github.com/minigame-labs/migo) 小游戏运行时引擎的 Android 示例项目。

## 快速开始

### 1. 构建 Migo AAR

当前 Demo 默认依赖本地 `../migo` 仓库产物：

- `../migo/platforms/android/dist/migo-debug.aar`

先在 `migo` 仓库执行：

```bash
cd ../migo
bash scripts/build-aar.sh debug
```

如果你希望改用 release AAR，可先构建：

```bash
cd ../migo
bash scripts/build-aar.sh release
```

然后按需修改 `app/build.gradle.kts` 中的 AAR 路径。

### 2. 准备游戏文件

游戏文件需要放置到设备的应用私有目录，路径格式为：

```
/data/data/com.minigame.androiddemo/files/migo/games/{gameId}/code/
├── game.js          # 游戏入口文件
├── images/          # 图片资源
├── workers/         # Worker 脚本目录（如有）
└── ...              # 其他资源文件
```

其中 `{gameId}` 是游戏的唯一标识符（字母数字、下划线、连字符，1-64字符）。

示例：推送 `demo` 游戏：

```bash
adb push your-game/ /data/data/com.minigame.androiddemo/files/migo/games/demo/code/
```

> 注意：SDK 现在不会自动读取 `game.json` 来解析 `workers` / `subPackages`。
> 本 Demo 已在宿主侧实现了 `code/game.json` 读取，并在创建 `RuntimeConfig` 时注入这些配置。
> 你也可以手动注入：

```java
RuntimeConfig config = new RuntimeConfig.Builder(context)
    .setWorkersPath("workers")
    // .addSubPackage("stage1", "subpackages/stage1")
    .build();
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

    // 最新 API: 可选注册宿主 handler（建议在 startGame 前）
    session.setAuthHandler(authHandler);
    session.setGameLogHandler(gameLogHandler);
    session.setSubpackageHandler(subpackageHandler);

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

// MigoGameView 会在内部创建 session，需在 session 可用后注册 handler
GameSession session = gameView.getSession();
if (session != null) {
    session.setAuthHandler(authHandler);
    session.setGameLogHandler(gameLogHandler);
    session.setSubpackageHandler(subpackageHandler);
}
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

## 宿主 Handler（最新 API）

`GameSession` 当前支持以下宿主回调：

- `setAuthHandler(AuthHandler)`：处理 `wx.login` / `wx.checkSession` / `wx.getUserInfo` / `wx.getPhoneNumber`
- `setGameLogHandler(GameLogHandler)`：接收小游戏上报日志（JSON）
- `setSubpackageHandler(SubpackageHandler)`：处理 `loadSubpackage` / `preDownloadSubpackage`

Demo 中的对应 sample 实现：

- `app/src/main/java/com/minigame/androiddemo/auth/ProxyAuthHandler.java`
- `app/src/main/java/com/minigame/androiddemo/DemoGameLogHandler.java`
- `app/src/main/java/com/minigame/androiddemo/DemoSubpackageHandler.java`

Auth 代理的独立操作文档见：`AUTH_PROXY_RUNBOOK.md`

## 项目结构

```
app/
└── src/main/
    ├── java/.../
    │   ├── MainActivity.java           # Demo 选择器
    │   ├── CustomGameActivity.java     # 方式 2: 手动 GameSession 管理
    │   ├── EmbeddedGameActivity.java   # 方式 3: MigoGameView 嵌入
    │   ├── DemoGameLogHandler.java     # GameLogHandler sample
    │   ├── DemoSubpackageHandler.java  # SubpackageHandler sample
    │   ├── auth/
    │   │   └── ProxyAuthHandler.java   # AuthHandler sample
    │   └── ui/
    │       └── CapsuleMenu.java        # 胶囊菜单 UI 组件
    └── AndroidManifest.xml

AUTH_PROXY_RUNBOOK.md                    # Auth 代理独立操作文档
```

## 游戏目录结构

```
/data/data/{packageName}/files/migo/games/{gameId}/
├── code/           # 游戏代码目录 (只读)
│   ├── game.js
│   └── images/
└── user_data/      # 用户数据目录 (可读写)

/data/data/{packageName}/cache/migo/games/{gameId}/
└── tmp/            # 临时目录 (会话结束自动清理)
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
