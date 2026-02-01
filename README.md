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

## 项目结构

```
app/
├── libs/
│   └── migo.aar                  # Migo SDK（需手动放置）
└── src/main/
    ├── java/.../
    │   ├── MainActivity.java     # 主 Activity
    └── AndroidManifest.xml
```

## SDK API 使用

### 初始化

```java
import com.migo.runtime.MigoRuntime;
import com.migo.runtime.GameSession;
import com.migo.runtime.RuntimeConfig;

// 创建配置
RuntimeConfig config = new RuntimeConfig.Builder(context)
    .setDebugEnabled(true)
    .build();

// 创建会话
GameSession session = MigoRuntime.getInstance()
    .createSession(activity, surface, config, "demo");  // "demo" 是 gameId
```

### 启动游戏

```java
// 游戏代码位于: filesDir/migo/games/{gameId}/code/
session.startGame("game.js");  // 入口文件
```

### 处理输入

```java
@Override
public boolean onTouchEvent(MotionEvent event) {
    return session.dispatchTouchEvent(event);
}
```

### 生命周期

```java
// Activity.onResume()
session.resume();

// Activity.onPause()
session.pause();

// Activity.onDestroy()
session.close();
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

## 许可证

MIT License
