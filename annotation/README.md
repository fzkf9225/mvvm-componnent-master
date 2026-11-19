# annotation

可选校验注解库（纯 Java Library，非 Android Library）：声明式字段 / Bean 校验与运行时校验器。

当前版本：**3.3.1**  
Maven：`io.coderf.arklab.annotation:annotation:3.3.1`

---

## 职责

- `@Verify*` / `@Valid` 等校验注解
- `EntityValidator`，用于表单 / 入参校验

---

## 依赖

- 无工程内 `project` 依赖

---

## 何时依赖

需要声明式实体 / 参数校验时：

```gradle
implementation 'io.coderf.arklab.annotation:annotation:3.3.1'
```

工程内 Demo 见 `app` / 表单相关用法；总览见 [MODULES.md](../MODULES.md)。

---

## 发布

```bash
./gradlew :annotation:publish
```
