# annotation

可选校验注解库（纯 Java Library，非 Android Library）：声明式字段 / Bean 校验与运行时校验器。

当前版本：**3.2.0**  
Maven：`io.coderf.arklab.annotation:annotation:3.2.0`

---

## 职责

- `@Verify*` / `@Valid` 等校验注解
- `EntityValidator` 及格式处理器，用于表单 / 入参校验

---

## 依赖

- 无工程内 `project` 依赖

---

## 何时依赖

需要声明式实体 / 参数校验时：

```gradle
implementation 'io.coderf.arklab.annotation:annotation:3.2.0'
// 若使用注解处理器：
annotationProcessor project(':annotation')   // 或对应 Maven classifier / 处理器配置
```

工程内 Demo 见 `app` / 表单相关用法；总览见 [MODULES.md](../MODULES.md)。

---

## 发布

```bash
./gradlew :annotation:publish
```
