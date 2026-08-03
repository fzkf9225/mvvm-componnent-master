# ArkLab BaseActivity / Fragment Templates

安装后出现在 Android Studio 原生 **New → Activity / Fragment**。

## 模板列表

| 菜单名 | 生成内容 |
|--------|----------|
| BaseActivity 空白页 | Activity + ViewModel + 空白布局 |
| BaseFragment 空白页 | Fragment（含 `newInstance`）+ ViewModel + 空白布局 |
| SmartPaging Fragment | Fragment + Adapter + item XML + Bean + ViewModel + Repository（LiveData） |
| SmartFlowPaging Fragment | 同上（Flow / Kotlin） |
| BaseRecyclerView Fragment | Fragment + Adapter + item XML + Bean + ViewModel + Repository |
| SmartPaging Activity+Fragment | 空白 Host Activity（FragmentContainerView + Navigation）+ SmartPaging 全套 |
| SmartFlowPaging Activity+Fragment | 同上（Flow） |
| BaseRecyclerView Activity+Fragment | 同上（RecyclerView） |
| 表单 Activity | Activity + ScrollView 骨架 + CornerButton「提交」+ ViewModel + Repository |

所有 Java/Kotlin 源文件均带统一类注释：

```java
/**
 *
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created yyyy/M/d HH:mm
 */
```

## 重要：不要加入主工程 settings.gradle

`as-plugin` 是 **Android Studio 插件工程**（依赖本机 AS + IntelliJ Platform），不是 Android library。  
不要 `include ':as-plugin'`：会与主工程 Kotlin `2.1.20` 冲突；若强行改成 `2.1.20`，又无法编译针对 Narwhal（Kotlin metadata 2.3）的模板 API。

请始终在 `as-plugin` 目录内单独构建。

## 构建 / 安装

```bat
cd as-plugin
build.bat
```


产物：`build/distributions/arklab-base-activity-templates-1.1.1.zip`

1. Settings → Plugins → 卸掉旧版（如有）
2. ⚙ → Install Plugin from Disk… → 选 zip
3. 重启 Android Studio

## 使用提示

- Package name 填模块根包，如 `io.coderf.arklab.demo`
- Feature Name 不含 `Activity` / `Fragment` 后缀
- Paging 模板依赖模块内 `api.ApiServiceHelper`（与当前 app 模块一致）
