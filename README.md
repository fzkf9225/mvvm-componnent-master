# mvvm-componnent-master
## MVVM架构示例代码，重构版本
### 如果想用基础模块的话直接导入common就可以了
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
### 过滤网络请求日志
调用ApiRetrofit下的TAG
TAG值为类名：`ApiRetrofit`
## 接入指引：
### 导入公共依赖包
由于集成的aar包有隔离效果因此需要在继承base类时导入他们的包，不然就会找不到引用的第三方库，但是如果你没有以aar引用而是直接module引用的话，可以直接将`implementation` 改成`api`即可
```
//沉浸式通知栏框架
    implementation "com.geyifeng.immersionbar:immersionbar:3.2.2"
//RxJava、RxAndroid和Retrofit网络框架
    implementation "com.squareup.okhttp3:okhttp:5.0.0-alpha.11"
    implementation "com.squareup.retrofit2:retrofit:2.9.0"
    implementation "io.reactivex.rxjava3:rxandroid:3.0.2"
    implementation "io.reactivex.rxjava3:rxjava:3.1.6"
    //ConverterFactory的String依赖包
    implementation "com.squareup.retrofit2:converter-scalars:2.9.0"
    //ConverterFactory的Gson依赖包
    implementation "com.squareup.retrofit2:converter-gson:2.9.0"
    //CallAdapterFactory的Rx依赖包
    implementation "com.squareup.retrofit2:adapter-rxjava3:2.9.0"
//腾讯的mmkv框架
    implementation "com.tencent:mmkv:1.2.11"
//刷新加载更多框架
//下拉刷新
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
    implementation "io.github.scwang90:refresh-layout-kernel:2.0.6"
    implementation "io.github.scwang90:refresh-header-classics:2.0.6"
```
### hilt框架依赖
在项目根目录下build.gradle添加
```
plugins {
    id 'com.android.application' version '8.0.0' apply false
    id 'com.android.library' version '8.0.0' apply false
    id 'com.google.dagger.hilt.android' version '2.46.1' apply false
    id 'org.jetbrains.kotlin.android' version '1.8.20' apply false
}
```
在各个模块添加插件引用，那个模块用到在哪个模块添加，每个用到的额都要添加
```
plugins {
    id 'com.android.application'
    id 'com.google.dagger.hilt.android'
}
```
在各个模块添加插件引用，那个模块用到在哪个模块添加，每个用到的额都要添加

```
    implementation 'com.google.dagger:hilt-android:2.46.1'
    annotationProcessor 'com.google.dagger:hilt-android-compiler:2.46.1'
```
### 新建assets目录
下面新建dev.properties和release.properties，里面有一些基础配置，在common模块里已经配置了，参数key具体参考demo中的的properties配置文件，也可以自定义一些配置

//调用默认配置，注意参数key不能错误，参考demo中的
```
    PropertiesUtil.getInstance().getProperties(mContext).getBaseUrl()
```
    
//调用自定义文件中的自定义配置
```
    PropertiesUtil.getInstance().getProperties(mContext，fileName).getPropertyValue(key,defaultValue)
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
    Config.init(this);
```
### 新建ErrorServiceImpl实现ErrorService接口
    里面有很多封装好的api需要实现，当然你也可以不用，不建议直接修改common组件，因为组件化讲的就是解耦，你什么都在common离实现那耦合性必然就会很高，当然也要新建ErrorServiceModule，这是hilt用法，具体你们自己百度了
### 强调一下
在接入hilt时每个module都要导入这两个包，不能少，少了的话你可以编译但是无法运行，而且你还找不到错在哪
//每个用到的模块都要加这两个，不能使用api
``` 
    implementation "com.google.dagger:hilt-android:2.46.1"
    annotationProcessor 'com.google.dagger:hilt-android-compiler:2.46.1'
```
### 项目中自动为您集成了今日头条的适配方案，因此需要你在配置文件中新增属性
    <meta-data
        android:name="design_width_in_dp"
        android:value="360" />
    <meta-data
        android:name="design_height_in_dp"
        android:value="640" />
### 关于网络访问重试
在各实现RetryService接口，该接口会自动实现最好在用户模块实现，因为该接口主要用于实现过期免登录，因此在登录模块实现最为合理，示例代码里有示例，此功能关于hilt的使用，因此需要了解hilt框架用法,需要再ViewModel上添加注解@HiltViewModel,
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
    mediaHelper = new MediaBuilder(this, this)
        .setImageMaxSelectedCount(1)
        .builder();
        mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            
        });
```
#### 断点续传下载
普通文件下载示例：
```
    DownloadManger.getInstance().download(mContext,"下载文件url");
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
    new PicShowDialog(mContext,PicShowDialog.createImageInfo(mList),pos).show();
```
#### 关于一些其他的公共库
base封装、一些dialog框、自定义view封装等等可以自行在源码中查看了解，不一一介绍了

