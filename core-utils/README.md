# core-utils

轻量工具层：尽量不绑定 Android `R` / DataBinding，供 `core-base` 等上层复用（如日志等）。

当前版本：**1.0.1**  
Maven：`io.coderf.arklab.core:utils:1.0.1`  
namespace：`io.coderf.arklab.core.utils`

---

## 职责

- 无资源耦合的通用工具（拆分预留；大量历史 util 仍在 `core-base`）
- 作为核心栈最底层之一，被 `core-base` `api` 引用

---

## 依赖

- 无其它工程 `project` 依赖

---

## 何时依赖

一般经 `core-base` / `common` 间接使用；需要纯工具、避免 UI/R 耦合时可显式：

```gradle
implementation 'io.coderf.arklab.core:utils:1.0.1'
// 或
implementation project(':core-utils')
```

---

## 发布

```bash
./gradlew :core-utils:publish
```
