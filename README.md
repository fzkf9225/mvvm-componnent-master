# mvvm-componnent-master
## MVVM架构示例代码，重构版本
### 如果想用基础模块的话直接导入common就可以了
### mqttcomponent和websocketcomponent为mqtt和websocket简单封装，没太多复杂逻辑，有需要的可以自己接
### userapi------为module的公共api模块，提供给别的module接入，建议此模块编写一些interface接口和实体类等等，其他的主要逻辑代码不要放在这里
### usercomponent------组件化代码示例，为单独一个组件，配合api模块一起使用，别的module不要接入此模块，不然组件化就失去了本身的意义

## 接入指引：
### 新建assets目录，下面新建dev.properties和release.properties，里面有一些基础配置，在common模块里已经配置了，也可以自定义一些配置，调用
    PropertiesUtil.getProperties(mContext).getBaseUrl()调用默认配置。
    PropertiesUtil.getProperties(mContext，fileName).getPropertyValue(key,defaultValue)调用自定义文件中的自定义配置
### 新建Application继承BaseApplication，一定要配置，不然common中很多代码会报错
    在application中初始化一些基础配置例如：MMKV、网络框架、WebSocket、mqtt等
### 新建ErrorServiceImpl实现ErrorService接口，里面有很多封装好的api需要实现，当然你也可以不用，不建议直接修改common组件，因为组件化讲的就是解耦，你什么都在common离实现那耦合性必然就会很高，当然也要新建ErrorServiceModule，这是hilt用法，具体你们自己百度了
### 强调一下啊，在接入hilt时每个module都要导入这两个包，不能少，少了的话你可以编译但是无法运行，而且你还找不到错在哪
    //每个用到的模块都要加这两个，不能使用api
    implementation "com.google.dagger:hilt-android:2.46.1"
    annotationProcessor 'com.google.dagger:hilt-android-compiler:2.46.1'
### 项目中自动为您集成了今日头条的适配方案，因此需要你在配置文件中新增属性
<meta-data
    android:name="design_width_in_dp"
    android:value="360" />
<meta-data
    android:name="design_height_in_dp"
    android:value="640" />
### 关于网络访问重试
在各实现RetryService接口，该接口会自动实现最好在用户模块实现，因为该接口主要用于实现过期免登录，因此在登录模块实现最为合理，示例代码里有示例，此功能关于hilt的使用，因此需要了解hilt框架用法
### 关于同意尺寸的配置
在dimens资源文件下配置了统一的尺寸大小，方便维护


    
