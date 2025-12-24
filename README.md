# mvvm-componnent-master

# 工程介绍
    - annotation：注解框架，主要用于数据校验的注解
    - app：框架case示例
    - common：共同模块，最基础的封装
    - commonmedia：摄像头、相册、媒体相关封装
    - commonui：UI组件封装，主要为表单组件
    - googlegps：GPS工具类
    - mqttcomponent：MQTT最基础的封装示例
    - userapi：示例module的对外接口，模块之间调用
    - usercomponent：示例module，也就是项目中模块化示例
    - wscomponent：WebSocket最基础的封装示例

全面使用``ksp``

## MVVM架构示例代码，重构版本

### 如果想用基础模块的话直接导入common就可以了

#### 在线引用方式

可以先直接打包成aar，然后执行gradle中的脚本即可，gradle中有注解，或者直接执行gradle得maven-publish，
`ALIYUN_USER_NAME`、`ALIYUN_PASSWORD`是window的环境变量值为阿里云的用户名密码，大家可以自己添加，或者直接不用
`System.getenv("ALIYUN_USER_NAME")`,直接用你的用户名密码

### mqttcomponent和websocketcomponent

为mqtt和websocket简单封装，没太多复杂逻辑，有需要的可以自己接

### userapi

为module的公共api模块，提供给别的module接入，建议此模块编写一些interface接口和实体类等等，其他的主要逻辑代码不要放在这里

### usercomponent

组件化代码示例，为单独一个组件，配合api模块一起使用，别的module不要接入此模块，不然组件化就失去了本身的意义

## 封装思路博文，注意观看顺序哈

一：https://blog.csdn.net/fzkf9225/article/details/105197996

二：https://blog.csdn.net/fzkf9225/article/details/105200803

三：https://blog.csdn.net/fzkf9225/article/details/132182449

## 查看网络请求等相关日志

### 初始化和打开日志

在app中的Application中调用初始化方法

```
        Config.getInstance().init(this);
        if (BuildConfig.LOG_DEBUG) {
            Config.getInstance().enableDebug(true);
        }
```
### 常用Api

#### 系统配置
1. 继承`BaseApplication`，然后初始化`Config.getInstance().init(this)`;如果需要获取全局的`Application`对象可以调用`Config`的方法
2. `ErrorService`：提供全局的api，处理网络请求回调的一些错误和跳转登录拦截等问题，还有统一网络请求头的配置等

#### UI相关
1. 普通`Activity`继承`BaseActivity`即可
2. 普通`Fragment`继承`BaseFragment`即可
3. 普通`Adapter`继承`BaseRecyclerViewAdapter`即可
4. 如果需要`pinging`分页的`Adapter`的话，继承`BasePagingAdapter`
5. 原始的列表自带的下拉刷新，`paging`分页可以继承`BasePagingFragment`
6. 不带`pinging`分页的列表`Fragment`继承`BaseRecyclerViewFragment`
7. 最常用的分页列表`Fragment`，支持多样式刷新，加载更多，`paging3`分页继承`BaseSmartPagingFragment`
8. `Fragment`、`Activity`自带的回调接口`BaseView`，如果需要自定义的话，可以直接继承`BaseView`，然后在`Fragment`、`Activity`实现即可
9. `adapter`如果需要实现自定义的事件控件等，可以自定义`BaseViewHolder`，然后在adapter中重写createViewHold方法
10. 普通`ViewModel`可以直接继承`BaseViewModel`
11. 分页列表的`ViewModel`继承`PagingViewModel`
12. 支持tab标签侧滑功能的`Fragment`继承`BaseViewPagerFragment`，如果不需要`Fragment`的话直接`Activity`继承`BaseViewPagerActivity`
13. 自定义相机可以直接调用`CameraActivity`，视频播放可以直接调用`VideoPlayerActivity`，二维码扫描可以直接调用`CaptureActivity`，WebView可以直接调用`WebViewActivity`

#### 数据存储和网络请求相关
1. 网络请求相关`Repository`常用的话直接继承`RepositoryImpl`，在`ViewModel1`中创建`Repository`对象，可以使用构造方法创建也可以使用`RepositoryFactory`工厂工具类创建
2. 网络请求封装`ApiRetrofit`，通过`Builder`模式创建，具体`Api`参考`ApiRetrofit.Builder`
3. 如果本地存储数据的话可以使用`MMKVHelper`代替`SharedPreferences`，数据库的话，可以继承`BaseRoomDao`和`RoomRepositoryImpl`，提供常见的一些Api方法，附件表已经提供了基础的模版`AttachmentDao`和`AttachmentDatabase`和`AttachmentRepositoryImpl`
4. 所有的`Retrofit`的网络请求代理接口都需要集成`BaseApiService`
5. 网络请求分页`PagingSource`继承`PagingSource`，协程`flow`方式继承`FlowPagingSource`，数据库存储分页继承`RxRoomPagingSource`
6. `RetryService`：封装了网络请求拦截重试机制，可以在`Repository`中设置或者`ApiRetrofit`中配置，`ApiRetrofit`配置为默认的，`Repository`中为当前类覆盖默认的配置
7. `FlowRetryService`：协程方式的网络请求重试，可以在`Repository`中设置或者`ApiRetrofit`中配置，`ApiRetrofit`配置为默认的，`Repository`中为当前类覆盖默认的配置
8. `RoomRepositoryImpl`：数据库的一些常用方法
9. `PagingRepositoryImpl`：`pinging3`分页的`Repository`层封装
10. `PagingFlowRepositoryImpl`：关于协程的`pinging3``Repository`层封装
11. `FlowRepositoryImpl`：协程方式`Repository`封装

#### 常用工具类
1. `AppManager`：获取`Activity`栈信息和获取App版本号、版本名称、App是否在前台运行等API
2. `AppSettingHelper`：可以设置基础的app是否首次运行等方法
3. `ConstantsHelper`：基础的一些常用静态常量
4. `MobileHelper`：获取手机的一些信息，比如获取手机的IMEI、MAC地址、手机型号、手机系统版本、手机分辨率、手机唯一标识符等
5. `RegexUtils`： 正则表达式
6. `AuthManager`：登录相关的一些调用，`Fragment`、`Activity`持有的对象，可以直接获取使用
7. `UIController`：控制`Fragment`、`Activity`一些`dialog`、`toast`、`loading`等
8. `PropertiesModule`：加载Properties对象可以获取配置文件里的参数
9. `CacheUtil`：缓存工具
10. `CollectionUtil`：集合工具类
11. `DownloadManger`：下载管理工具
12. `PermissionManager`：系统权限管理工具类
13. `NetworkStateUtil`：网络状态工具类
14. `StringUtil`: 字符串工具类
15. `RxMenuView`：`Toolbar`菜单控件点击拦截时间，可拦截快速点击事件
16. `RxView`：`View`点击拦截时间，可拦截快速点击事件
17. `QRCodeUtil`：二维码工具类
18. `MathUtil`：数学计算工具类，加减乘除和一些格式化，保留小数位数等
19. `NumberUtil`：数字大小写转化，数字格式化等
20. `KeyBoardUtil`：软键盘控制、唤醒、关闭和监听等
21. `GeoMapUtil`：坐标转化工具
22. `FastBlur`：高斯模糊工具类
23. `ThreadExecutor`：线程池工具类
24. `ThreadExecutorBounded`：线程池工具类，边界更大默认为128

#### 自定义View相关
1. `AutoNextLineLinearlayout`：自动换行的`ViewGroup`
2. `CircleTextView`：圆形背景的 `AppCompatTextView`，支持边框、背景色、文字居中显示
3. `CornerImageView`：圆角 `ImageView`，支持四个角独立设置圆角半径
4. `CirclePaddingImageView`：带内边距的圆形 `ImageView`，支持选中/按下样式切换
5. `CustomScrollView`：支持下拉回弹效果的 `ScrollView`
6. `Code`：生成图形验证码的工具类，可绘制随机字符和干扰线
7. `DividerView`：可绘制横线或竖线的虚线控件（Kotlin 实现）
8. `CircleProgressBar`：圆形进度条，支持文字显示、动画进度更新
9. `GradationRectTextView`：支持渐变背景、左右文字样式不同的 `TextView`
10. `HomeMenuView`：首页菜单视图，支持分页显示、圆点指示器（`Kotlin` 实现）
11. `CornerEditText`：圆角背景的 `EditText`，支持边框和背景色设置
12. `CornerLabelView`：三角形角标控件，可显示在四个角落，支持文字和背景色
13. `CornerTextView`：圆角背景的 `TextView`，支持边框和背景色
14. `CornerButton`：圆角背景的 `Button`，支持边框和背景色
15. `HorizontalProgressBar`：水平横向进度条，支持圆角背景和文字显示
16. `ConfigurableWebView`：可配置的 `WebView`，支持加载本地 assets 或网络 URL
17. `AutoTextView：带 3D 翻转动画的文字切换控件（基于 `TextSwitcher`）
18. `CustomSearchEditText：带搜索图标和清除功能的 `EditText`，支持圆角背景
19. `CornerConstraintLayout`：圆角背景的 `ConstraintLayout`
20. `BannerView`：轮播图控件，支持圆点指示器、自动轮播、圆角裁剪
21. `Code`：生成图形验证码的工具类，可绘制随机字符和干扰线
22. `StarBar`：自定义星星评分控件，支持实心/空心五角星、整数/小数评分、触摸滑动评分等功能
23. `IconDotTextView`：图标+文字+数字角标的组合控件，支持多种布局方向（图标上下位置）和角标自定义
24. `SquareLabelView`：左侧带方块的`TextView`，用于模块名称等场景，支持方块形状（矩形、椭圆、圆角矩形）和位置自定义
25. `IconLabelValueView`：图标-标签-值-图标的布局视图，常用于设置项或详情展示，支持左右图标开关、底部边框、值对齐方式等配置
26. `VerificationCodeInputView`：验证码输入控件，支持多格独立输入框、光标闪烁、输入类型限制（数字、字母等）、边框样式自定义
27. `RoundImageView`：圆形`ImageView`，用于头像等圆形图片显示，支持边框宽度和颜色设置
28. `ScalingTextView`：可展开/收起的文本控件，默认显示指定行数，支持“查看全文/收起全文”点击切换
29. `LoadingProgressDialog`：加载进度对话框，支持动态省略号动画效果、自定义提示文字、可设置是否可取消
30. `DatePickDialog`：年月日选择对话框，支持多种日期模式（年月日、年月、年份、时间等）、自定义日期范围和标签
31. `BottomSheetDialog`：底部选择框（基于Material Design），支持列表选项显示、自定义分割线样式和取消按钮
32. `MenuDialog`：底部菜单对话框，功能类似·BottomSheetDialog·但使用传统Dialog实现，支持自定义位置和样式
33. `ImageSaveDialog`：图片保存选择对话框，提供“保存到本地”等选项，用于图片保存场景
34. `EmptyLayout`：空白占位布局控件，可显示加载中、加载失败、无数据等状态，支持自定义图标和点击重试
35. `InputDialog`：单行输入对话框，支持文本输入、最大字数限制、输入类型设置和提示文字
36. `EditAreaDialog`：多行文本输入对话框，功能类似`InputDialog`但支持多行文本编辑
37. `UpdateMessageDialog`：应用更新提示对话框，显示版本信息和更新内容，支持链接识别和流量提醒
38. `ConfirmDialog`：确认对话框，提供确定和取消按钮，支持富文本内容显示和按钮可见性控制
39. `ProgressBarDialog`：进度条对话框，支持圆形和水平两种进度条样式，可自定义进度条外观和按钮
40. `MessageDialog`：信息提示对话框，单按钮设计，用于简单信息提示场景
41. `ProtectionGuidelinesDialog`：启动页的用户权限隐私指引，请求权限提示弹框，支持富文本内容、自定义按钮文字和样式，用于权限申请场景
42. `CascadeSinglePopupWindow`：单选项级联弹窗
43. `PopupView`：普通下拉选择弹窗（单选）
44. `TextPopupView`：自定义文本+图标组合控件
45. `MultiPopupView`：多选（非级联）弹窗
46. `TreePopupView`：二级树形选择弹窗
47. `CascadeMultiPopupWindow`：多选项级联弹窗
48. `TxSlideRecyclerView`：支持侧滑的`RecyclerView`
49. `SpeakButton`：仿微信的长按说话View
50. `VoiceView`：语音播放条控件

#### Form表单相关控件
主要用于常见的一些表单输入和回显情况
1. `FormDate`：日期选择控件
2. `FormDateRange`：日期范围选择控件
3. `FormDateTime`：日期+时间选择控件
4. `FormEditArea`：文本域输入控件
5. `FormEditText`：单行文本输入控件
6. `FormFile`：文件选择控件，包含全部格式的文件选择
7. `FormFilePreview`：文件预览控件
8. `FormImage`：图片选择控件
9. `FormImageAndVideo`：图片+视频选择控件
10. `FormImageAndVideoPreview`：图片+视频预览控件
11. `FormImagePreview`：图片预览控件
12. `FormRichText`：富文本控件
13. `FormSelection`：下拉选择控件
14. `FormTextView`：文本预览控件，支持单行多行显示，主要用于详情页的表单回显
15. `FormTime`：时间选择控件
16. `FormVideo`：视频选择控件
17. `FormVideoPreview`：视频预览控件
18. `DateRangePickDialog`：日期范围选择`dialog`
19. `TickViewMessageDialog`：对勾动画弹窗
20. `CalendarView`：自定义日历

#### GPS相关api
1. `GpsService`: GPS后台服务
2. `GpsLifecycleObserver`：GPS生命周期观察者，可以直接添加这个观察者，包括GPS是否开启、权限判断等等
### 过滤网络请求日志

调用ApiRetrofit下的TAG
TAG值为类名：`ApiRetrofit`

## 接入指引：

### 导入公共依赖包

由于集成的aar包有隔离效果因此需要在继承base类时导入他们的包，不然就会找不到引用的第三方库，但是如果你没有以aar引用而是直接module引用的话，可以直接将
`implementation` 改成`api`即可
可参考app模块的demo依赖库

```
    implementation fileTree(include: ['*.jar', '*.aar'], dir: 'libs')
    implementation libs.activity
    implementation libs.androidx.core.ktx
    implementation platform(libs.kotlin.bom)
    implementation libs.androidx.appcompat
    implementation libs.material
    testImplementation libs.junit
    androidTestImplementation libs.androidx.junit
    androidTestImplementation libs.androidx.espresso.core
    implementation libs.constraintlayout

    implementation project(':common')
    implementation project(':userapi')
    implementation project(':commonui')
    implementation project(':googlegps')
    implementation project(':commonmedia')
    implementation project(':usercomponent')
    implementation project(':mqttcomponent')
    implementation project(':wscomponent')

    implementation project(':annotation')
    annotationProcessor project(':annotation')

    implementation libs.github.glide

    implementation libs.logger

    implementation libs.google.hilt.android
    ksp libs.google.hilt.android.compiler

    implementation libs.androidx.core.splashscreen
    implementation libs.androidx.multidex
    implementation libs.androidx.navigation.fragment
    implementation libs.androidx.navigation.ui
    implementation libs.androidx.navigation.ui.ktx
    implementation libs.mmkv
    implementation libs.reactivex.rxandroid
    implementation libs.rxjava3.rxjava
    implementation libs.gson
    implementation libs.scwang90.refresh.layout.kernel
    implementation libs.refresh.header.classics
    implementation libs.refresh.footer.classics
    implementation libs.okhttp
    implementation libs.retrofit

    implementation libs.zxing.core
    implementation libs.zxing.android.embedded
    //paging3
    implementation libs.androidx.paging.runtime
    // 用于测试
    testImplementation libs.androidx.paging.common// [可选] RxJava 支持
    implementation libs.paging.rxjava3
    implementation libs.swiperefreshlayout

    implementation libs.androidx.recyclerview
    implementation libs.graphics
    implementation libs.net.res
    implementation libs.net.ui

    implementation libs.androidx.room.ktx
    implementation libs.androidx.room.runtime
    ksp libs.room.compiler
    // optional - RxJava3 support for Room
    implementation libs.room.rxjava3
    // optional - Paging 3 Integration
    implementation libs.room.paging
```

### 混淆配置

```
# ==================== MVVM Common Library ====================
# 保留 MVVM 框架核心类
-keep class com.casic.otitan.common.** { *; }
-dontwarn com.casic.otitan.common.**

# 保留数据绑定相关类
-keep class com.casic.otitan.common.bean.** { *; }
-keepclassmembers class com.casic.otitan.common.bean.** {
    void set*(***);
    *** get*();
}

# 保留 BaseResponse 和 ViewModel
-keep class com.casic.otitan.common.base.BaseResponse { *; }
-keep class com.casic.otitan.common.viewmodel.** { *; }

# 保留注解处理器生成的类
-keep class * extends com.casic.otitan.common.base.** { *; }

# 保留资源绑定相关类（如果有自动生成的绑定类）
-keep class *Binding { *; }
-keep class *BindingImpl { *; }
```
```
# ==================== Media Common Library ====================
# 保留 Media 框架核心类
-keep class com.casic.otitan.media.** { *; }
-dontwarn com.casic.otitan.media.**

# 保留数据模型类
-keep class com.casic.otitan.media.bean.** { *; }
-keepclassmembers class com.casic.otitan.media.bean.** {
    void set*(***);
    *** get*();
}

# 保留核心工具类和构建器
-keep class com.casic.otitan.media.MediaBuilder { *; }
-keep class com.casic.otitan.media.MediaHelper { *; }
-keep class com.casic.otitan.media.utils.** { *; }

# 保留回调接口
-keep class com.casic.otitan.media.callback.** { *; }
-keep interface com.casic.otitan.media.callback.** { *; }

# 保留多媒体处理方法
-keepclassmembers class com.casic.otitan.media.** {
    public * compress*(...);
    public * process*(...);
    public * encode*(...);
    public * decode*(...);
}
```

```
# ==================== CommonUI Library ====================
# 保留 CommonUI 框架核心类
-keep class com.casic.otitan.commonui.** { *; }
-dontwarn com.casic.otitan.commonui.**

# 确保数据模型不被混淆
-keep class com.casic.otitan.commonui.bean.** { *; }
-keepclassmembers class com.casic.otitan.commonui.bean.** {
    void set*(***);
    *** get*();
}

# 保留自定义 Widget 组件
-keep class com.casic.otitan.commonui.widget.** { *; }
-keepclassmembers class com.casic.otitan.commonui.widget.** {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
}

# 保留 Fragment
-keep class com.casic.otitan.commonui.fragment.** { *; }

# 保留适配器
-keep class com.casic.otitan.commonui.adapter.** { *; }

# 保留表单相关功能
-keep class com.casic.otitan.commonui.form.** { *; }

# 保留接口和回调
-keep interface com.casic.otitan.commonui.inter.** { *; }
-keep class com.casic.otitan.commonui.impl.** { *; }

# 保留数据绑定相关类
-keep class *Binding { *; }
-keep class *BindingImpl { *; }
```

```
# ==================== Google GPS Library ====================
# 保留 GPS 框架核心类
-keep class com.casic.otitan.googlegps.** { *; }
-dontwarn com.casic.otitan.googlegps.**

# 保留定位服务
-keep class com.casic.otitan.googlegps.service.** { *; }
-keepclassmembers class com.casic.otitan.googlegps.service.** {
    public * onStartCommand(...);
    public * onBind(...);
}

# 保留 Socket 通信
-keep class com.casic.otitan.googlegps.socket.** { *; }
-keepclassmembers class com.casic.otitan.googlegps.socket.** {
    public * connect*(...);
    public * send*(...);
}

# 保留工具类
-keep class com.casic.otitan.googlegps.utils.** { *; }

# 保留监听器接口
-keep interface com.casic.otitan.googlegps.listener.** { *; }

# 保留位置数据处理方法
-keepclassmembers class * {
    public * getLocation*(...);
    public * onLocation*(...);
    public * format*Gps*(...);
}
```
### hilt框架依赖

在项目根目录下build.gradle添加

```
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.hilt.android)
}
```

在各个模块添加插件引用，那个模块用到在哪个模块添加，每个用到的额都要添加

```
plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.hilt.android)
}
```

在各个模块添加插件引用，那个模块用到在哪个模块添加，每个用到的额都要添加

```
    implementation libs.google.hilt.android
    ksp libs.google.hilt.android.compiler
```

### 新建assets目录

下面新建dev.properties和release.properties，里面有一些基础配置，在common模块里已经配置了，参数key具体参考demo中的的properties配置文件，也可以自定义一些配置

调用默认配置，注意参数key不能错误，参考demo中的

```
    PropertiesUtil.getInstance().loadConfig(application).getBaseUrl()
```

调用自定义文件中的自定义配置

```
    PropertiesUtil.getInstance().loadConfig(application).getPropertyValue(key,defaultValue)
```

上面默认加载的是'prod.properties'配置文件中的配置如果需要加载别的直接再builde.gradle中添加如下resValue配置即可

```
    buildTypes {
        release {
            resValue "string", "app_config_file", "prod.properties"//指定文件名，app_config_file不能修改，后面的prod.properties"可以修改
            minifyEnabled true// 混淆
            shrinkResources true // 移除无用的resource
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
        debug {
            resValue "string", "app_config_file", "dev.properties"//指定文件名，app_config_file不能修改，后面的dev.properties"可以修改
            minifyEnabled false// 混淆
            shrinkResources false // 移除无用的resource
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
```

### 新建Application继承BaseApplication

一定要配置，不然common中很多代码会报错，在application中初始化一些基础配置例如：MMKV、网络框架、WebSocket、mqtt等
如果需要打印日志的话请调用Config接口
调用初始化方法

```
    Config.getInstance().init(this);
```

### CommonUI中附件组件配置统一的上传服务
```
        MediaUploadConfig.getInstance()
                .setFileApiService(fileApiService)
                .setUploadUrl("minioc/upload");
```

### 新建ErrorServiceImpl实现ErrorService接口

里面有很多封装好的api需要实现，当然你也可以不用，不建议直接修改common组件，因为组件化讲的就是解耦，你什么都在common离实现那耦合性必然就会很高，当然也要新建ErrorServiceModule，这是hilt用法，具体你们自己百度了

### 强调一下

在接入hilt时每个module都要导入这两个包，不能少，少了的话你可以编译但是无法运行，而且你还找不到错在哪
每个用到的模块都要加这两个，不能使用api

``` 
    implementation libs.google.hilt.android
    ksp libs.google.hilt.android.compiler
```

### 项目中自动为您集成了今日头条的适配方案，因此需要你在配置文件中新增属性

    <meta-data
        android:name="design_width_in_dp"
        android:value="360" />
    <meta-data
        android:name="design_height_in_dp"
        android:value="640" />

### 关于网络访问重试

在各实现`RetryService`接口，该接口会自动实现最好在用户模块实现，因为该接口主要用于实现过期免登录，因此在登录模块实现最为合理，示例代码里有示例，此功能关于hilt的使用，因此需要了解hilt框架用法,需要再ViewModel上添加注解@HiltViewModel,
当然你也可以冲过重写方法指定错误重试方法，可以不重写，也可以不加注解，系统会默认一个方法，但是请不要重写为null

### 关于同意尺寸的配置

在dimens资源文件下配置了统一的尺寸大小，方便维护

### 关于一些封装方法的调用

#### 图片视频选择库

暂时图片视频选择没有增加数量限制，大家可以自行实现，后期我会更新
选择图片：

```
    new OpenImageDialog(requireActivity())
        .setMediaType(OpenImageDialog.CAMERA_ALBUM)
        .setOnOpenImageClickListener(mediaHelper)
        .builder()
        .show();
```

选择视频

```
    new OpenShootDialog(requireActivity())
        .setMediaType(OpenShootDialog.CAMERA_ALBUM)
        .setOnOpenVideoClickListener(mediaHelper)
        .builder()
        .show();
```

初始化及获取其回调信息

```
//传统初始化方式
    mediaHelper = new MediaBuilder(this, this)
        .setImageMaxSelectedCount(1)
        .builder();
    mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            
    });
//hilt初始化方式，activity中
    @Inject
    @MediaModule.ActivityMediaHelper
    MediaHelper mediaHelper;
    
//hilt初始化方式，fragment中
    @Inject
    @MediaModule.FragmentMediaHelper
    MediaHelper mediaHelper;
```

#### 欢迎页隐私权限dialog
```
        new ProtectionGuidelinesDialog(this)
                .setCanOutSide(false)
                .setPositiveBackgroundColor(ContextCompat.getColor(this, com.casic.otitan.common.R.color.theme_green))
                .setSpannableContent(getSpannableContent())
                .setOnNegativeClickListener(dialog -> {

                    showToast("拒绝可能会导致部分功能使用异常");
                    keepOnScreenCondition.compareAndSet(false, true);
                    startCountDown();
                })
                .setOnPositiveClickListener(dialog -> {
                    UserAccountHelper.setAgreement(true);
                    if (!permissionManager.lacksPermissions(permissions())) {
                        keepOnScreenCondition.compareAndSet(false, true);
                        startCountDown();
                    } else {
                        permissionManager.request(permissions());
                    }
                })
                .builder()
                .show();
```
### 自定义注解使用
```java
import android.net.Uri;

import androidx.databinding.Bindable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.TypeConverters;

import java.util.List;

import com.casic.otitan.annotation.annotation.VerifyEntity;
import com.casic.otitan.annotation.annotation.VerifyField;
import com.casic.otitan.annotation.annotation.VerifyParams;
import com.casic.otitan.annotation.annotation.VerifySort;
import com.casic.otitan.annotation.enums.VerifyType;
import com.casic.otitan.annotation.inter.VerifyGroup;
import com.casic.otitan.common.bean.BaseDaoBean;
import com.casic.otitan.common.converter.RoomListStringConverter;

/**
 * Created by fz on 2023/9/5 18:32
 * describe :
 */
@Entity
@VerifyEntity(sort = true)
public class Person extends BaseDaoBean {
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "姓名为空！"),
            @VerifyParams(type = VerifyType.LENGTH_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, minLength = 2, maxLength = 10, errorMsg = "姓名输入错误！"),
            @VerifyParams(type = VerifyType.EQUALS, group = {VerifyGroup.Default.class}, errorMsg = "您只能填张三！", equalStr = "张三")
    })
    @VerifySort(1)
    @ColumnInfo
    private String name;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "请选择性别！"),
    })
    @VerifySort(2)
    @ColumnInfo
    private String sex;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "请选择生日！"),
    })
    @VerifySort(3)
    @ColumnInfo
    private String birthday;

    @Ignore
    private String educationalExperienceDate;

    @Ignore
    private String schoolStartTime;

    @Ignore
    private String classStartTime;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "请填写手机号码！"),
            @VerifyParams(type = VerifyType.MOBILE_PHONE,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "手机号码格式输入不正确！")
    })
    @VerifySort(4)
    @ColumnInfo
    private String mobile;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "请填写固话号码！"),
            @VerifyParams(type = VerifyType.TEL_PHONE,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "固话号码格式输入不正确！")
    })
    @VerifySort(5)
    @ColumnInfo
    private String tel;

    @VerifySort(6)
    @VerifyParams(type = VerifyType.NUMBER_RANGE,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, minNumber = 0, maxNumber = 120, errorMsg = "您是神仙吗？")
    @ColumnInfo
    private String age;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "体重为空"),
            @VerifyParams(type = VerifyType.NUMBER_00,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "体重输入格式不正确"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, maxNumber = 200, errorMsg = "你该减肥了！！！"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, minNumber = 40, errorMsg = "你已经瘦成竹竿了！！！")
    })
    @VerifySort(7)
    @ColumnInfo
    private String weight;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "身高为空"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, maxNumber = 300, errorMsg = "姚明都没你高！！！"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, minNumber = 40, errorMsg = "建议您补补钙，多晒晒太阳！！！")

    })
    @VerifySort(8)
    @ColumnInfo
    private String height;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY,group = {VerifyGroup.Create.class}, errorMsg = "邮箱地址为空！"),
            @VerifyParams(type = VerifyType.EMAIL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "邮箱地址错误！")
    })
    @VerifySort(9)
    @ColumnInfo
    private String email;

    @VerifySort(10)
    @VerifyParams(type = VerifyType.NOT_EMPTY, errorMsg = "您填填写您的爱好！")
    @ColumnInfo
    @TypeConverters({RoomListStringConverter.class})
    private List<String> hobby;

    //    @VerifyFieldSort(11)
//    @VerifyParams(type = VerifyType.NOTNULL, notNull = true, errorMsg = "您选择您的本人照片！")
    @Ignore
    private List<Uri> imageList;

    //    @VerifyFieldSort(12)
//    @Valid(notNull = true, errorMsg = "请选择您的家庭信息！")
    @Ignore
    public Family family;

    //    @VerifyFieldSort(13)
//    @Valid(notNull = true, errorMsg = "请选择您的家庭集合信息！")
    @ColumnInfo
    @Ignore
    public List<Family> familyList;


    public Person() {
    }

    @Ignore
    public Person(String name, String birthday, String mobile, String tel, String age, String weight, String height, String email, List<String> hobby) {
        this.name = name;
        this.birthday = birthday;
        this.mobile = mobile;
        this.tel = tel;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.email = email;
        this.hobby = hobby;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(com.casic.otitan.demo.BR.name);
    }

    @Bindable
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
        notifyPropertyChanged(com.casic.otitan.demo.BR.birthday);
    }

    @Bindable
    public String getEducationalExperienceDate() {
        return educationalExperienceDate;
    }

    public void setEducationalExperienceDate(String educationalExperienceDate) {
        this.educationalExperienceDate = educationalExperienceDate;
        notifyPropertyChanged(com.casic.otitan.demo.BR.educationalExperienceDate);
    }

    @Bindable
    public String getSchoolStartTime() {
        return schoolStartTime;
    }

    public void setSchoolStartTime(String schoolStartTime) {
        this.schoolStartTime = schoolStartTime;
        notifyPropertyChanged(com.casic.otitan.demo.BR.schoolStartTime);
    }

    @Bindable
    public String getClassStartTime() {
        return classStartTime;
    }

    public void setClassStartTime(String classStartTime) {
        this.classStartTime = classStartTime;
        notifyPropertyChanged(com.casic.otitan.demo.BR.classStartTime);
    }

    @Bindable
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
        notifyPropertyChanged(com.casic.otitan.demo.BR.age);
    }

    @Bindable
    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
        notifyPropertyChanged(com.casic.otitan.demo.BR.weight);
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(com.casic.otitan.demo.BR.email);
    }

    public List<String> getHobby() {
        return hobby;
    }

    public void setHobby(List<String> hobby) {
        this.hobby = hobby;
    }

    @Bindable
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
        notifyPropertyChanged(com.casic.otitan.demo.BR.mobile);
    }

    @Bindable
    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
        notifyPropertyChanged(com.casic.otitan.demo.BR.tel);
    }

    @Bindable
    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
        notifyPropertyChanged(com.casic.otitan.demo.BR.height);
    }

    @Bindable
    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
        notifyPropertyChanged(com.casic.otitan.demo.BR.sex);
    }


    public List<Uri> getImageList() {
        return imageList;
    }

    public void setImageList(List<Uri> imageList) {
        this.imageList = imageList;
    }

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family family) {
        this.family = family;
    }

    public List<Family> getFamilyList() {
        return familyList;
    }

    public void setFamilyList(List<Family> familyList) {
        this.familyList = familyList;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", mobile='" + mobile + '\'' +
                ", tel='" + tel + '\'' +
                ", age='" + age + '\'' +
                ", weight='" + weight + '\'' +
                ", height='" + height + '\'' +
                ", email='" + email + '\'' +
                ", hobby=" + hobby +
                ", imageList=" + imageList +
                ", family=" + family +
                ", familyList=" + familyList +
                '}';
    }
}
```
开始校验
```
            VerifyResult verifyResult = EntityValidator.validate(binding.getData());
            showToast((verifyResult.isOk() ? "验证成功" : "验证失败：") + StringUtil.filterNull(verifyResult.getErrorMsg()));
            if (!verifyResult.isOk()) {
                return;
            }
```

#### 自定义相机
参考示例代码`CustomCameraActivity`

#### Dialog常见封装示例
参考示例代码`DialogActivity`

#### 自定义组件封装示例
参考示例代码`WightActivity`

#### 二维码能力示例
参考示例代码`ScanQrCodeActivity`

#### GPS能力示例
参考示例代码`GoogleGPSActivity`

#### 自定义表单组件
参考示例代码`VerifyActivity`和`VerifyTopActivity`

#### 断点续传下载

普通文件下载示例：

```
               Disposable disposable = DownloadManger.getInstance().download(this, binding.editUrl.getText().toString().trim())
                    .subscribe(file -> {
                        LogUtil.show(ApiRetrofit.TAG, "下载成功：" + file.getAbsolutePath());
                        showToast("下载成功！");
                    }, throwable -> {
                        if (throwable instanceof BaseException baseException) {
                            showToast(baseException.getErrorMsg());
                            return;
                        }
                        showToast(throwable.getMessage());
                    });
```

版本更新示例：

```
    UpdateManger.getInstance().checkUpdateInfo((Activity) view.getContext(),
                        "http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk",
                        "1、修复已知bug",
                        "1.0.1");
```

#### 视频播放

项目中集成了github上的gsy视频播放库，目前是我觉得开源库中兼容性最好的了吧直接调用VideoPlayerActivity这个Activity就行了，基础播放功能，如果你的场景更负责那可能需要自己单独集成了

#### 大图预览框架

```
    new PreviewPhotoDialog(this)
        .createImageInfo(imageUrl)
        .currentPosition(0)
        .show()
```

#### 关于一些其他的公共库

base封装、一些dialog框、自定义view封装等等可以自行在源码中查看了解，不一一介绍了

