# 4.5.0 版本升级与迁移说明

> 从 **common 4.4.x 单体** 升级到 **common 4.5.1 + core-\* 1.0.1**。  
> 模块职责与日常用法见 [MODULES.md](./MODULES.md)。

---

## 1. 本次升级摘要

| 类别 | 变更 |
|------|------|
| 模块 | 新增 `core-utils` / `core-base` / `core-network` / `core-db` / `core-ui`；`common` 改为 facade |
| 包名 | 业务仍使用 `io.coderf.arklab.common.*`（源码在 `core-base` 等，一般 **import 不用改**） |
| 网络 | 新增 `DefaultNetworkRepository` + `RequestResult`；旧 Repository **已 Deprecated，未删除** |
| 解耦 | `user` 去掉对 `mqttcomponent` / `commonmedia` 的直接依赖，改走 Gateway |
| 稳定 | ViewModel UI 重绑、Dialog Context 泄漏、`startActivity` extras、ABI、`ANDROID_ID` 等已修 |
| 安全 | 去掉 `o-appSecret` 请求头；release 网络安全配置不再信任用户 CA |
| 发布 | `common` 与各 `core-*` 补齐 `maven-publish`；混淆规则按模块拆到 `consumer-rules.pro` |

---

## 2. 宿主工程怎么升

### 2.1 仍用工程依赖（本仓库 / 多 module）

依赖入口可不变：

```gradle
implementation project(':common')
```

确保 `settings.gradle` 已 `include` 全部 `core-*` 与 `:common`。

### 2.2 改用 Maven

```gradle
implementation 'io.coderf.arklab.common:common:4.5.1'
// 若 POM 未完整传递，按需显式补：
// implementation 'io.coderf.arklab.core:base:1.0.1'
// implementation 'io.coderf.arklab.core:network:1.0.1'
// …
```

媒体 / MQTT 仍按需单独依赖实现库，并在 **app 组装层** 做 Hilt 绑定（业务模块不要直接依赖实现）。

### 2.3 必查项

1. **Clean 后重装**（Manifest 合并变化：`InitProvider` 在 `core-base`，facade 不再声明组件）。
2. 若宿主曾拷贝旧 `common` 的 `AndroidManifest` 到自己的 facade，删掉相对名 `InitProvider` / Activity，避免解析成错误包名。
3. Release 开启混淆时，确认各 AAR 的 `consumer-rules` 已合并，或参考 app 内对 `io.coderf.arklab.common.**` / `core.**` / gateway 的 keep。

---

## 3. 代码迁移清单

### 3.1 业务模块依赖（必须）

**之前：**

```gradle
implementation project(':common')
implementation project(':mqttcomponent')
implementation project(':commonmedia')
```

**之后：**

```gradle
implementation project(':common')
implementation project(':base')
implementation project(':userapi')
// mqtt / media：禁止写在业务 module，由 app 绑定 Gateway
```

Java / Kotlin 中：

| 旧写法 | 新写法 |
|--------|--------|
| 直接 `MqttClient` / mqtt API | `@Inject MessageGateway`（接口在 `:base`） |
| 直接 `MediaHelper` / `MediaBuilder` | `@Inject MediaGateway`（接口在 `:base`） |

app 侧绑定参考：

- `MessageGateway` → `app/.../GatewayModule.kt` + `app/.../mqtt/MqttMessageGateway`
- `MediaGateway` → `app/.../MediaGatewayModule.kt` + `app/.../media/MediaHelperGateway`（app 依赖 `commonmedia`）

### 3.2 网络请求（建议逐步）

旧代码可继续编译：

```java
// @Deprecated — 勿新增
repository.sendRequest(api.xxx(), options, liveData, …);
```

新代码：

```kotlin
repository.request(RequestOptions(showLoading = true)) { api.xxx() }
    .collect { result ->
        when (result) {
            is RequestResult.Success -> { /* data */ }
            is RequestResult.Error -> { /* AppError */ }
            else -> Unit
        }
    }
```

新旧 UI 桥接：`common` 内 `RequestUiBridge`（可选）。

### 3.3 Base / 生命周期（行为变化，无需改调用）

- 配置变更后会 **重新绑定** ViewModel 的 UI / `RequestUi`，并在销毁时 `unbindView()`。
- `MediaHelper` 必须 `bindLifeCycle`；若在 Activity 已 `RESUMED` 后才创建，内部会走安全的 Activity Result 注册（避免崩溃）。

### 3.4 安全相关（必须知悉）

- 请求头不再带 `o-appSecret`；签名仍用本地 secret。
- assets 中 `APP_SECRET` 请换成环境占位 / 本地配置，勿提交真实生产密钥。
- Debug 与 Release 的网络安全配置不同：Release 不信任用户安装的 CA。

---

## 4. 验证建议

```bash
# 编译 Demo
./gradlew :app:assembleDebug

# user 不应再直接依赖 mqtt / media 实现
./gradlew :user:dependencies --configuration debugCompileClasspath
# 输出中不应出现 mqttcomponent、commonmedia
```

手动回归：

- [ ] 冷启动 / 旋转屏后 Loading、Toast 正常  
- [ ] 底部 Tab 切到「我的」不崩溃，选图可用  
- [ ] 登录 / 反馈页媒体能力正常  
- [ ] MQTT 连接与收发（若启用）  

---

## 5. 已知未完成（升级后仍存在）

以下不影响「能跑」，但后续还会继续改：

| 项 | 说明 |
|----|------|
| `core-base` 二次削片 | widget / 重 utils 仍集中在此 |
| `core-ui` / `core-utils` | 预留模块，代码量少 |
| ToolbarDelegate | 已预埋，未全面改写 Base |
| 旧网络 API | 仅 Deprecated，未删除 |
| 库模块自身 R8 | library 仍 `minifyEnabled false`，靠宿主 + consumer-rules |

---

## 6. 版本号对照

| 组件 | 旧（约） | 现 |
|------|----------|-----|
| common | 4.4.x（单体） | **4.5.1**（facade） |
| core-\* | 无 | **1.0.1** |
| media | 3.2.x | 3.2.5（发布脚本已对齐） |
