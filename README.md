# mvvm-componnent-master
# �ĵ�����

| �ĵ� | ˵�� |
|------|------|
| [MODULES.md](MODULES.md) | ��ģ��ְ��������ϵ��Maven ���ꡢGateway / ������ API �÷� |
| [UPGRADE.md](UPGRADE.md) | **��2��core-* 1.2.0 �޳� BaseView / MVP**����1��4.6.0 Material3����¼��4.4.x �� 4.5.1 ��� |
| [MIGRATION-2.0.0.md](MIGRATION-2.0.0.md) | core-base/network/db **2.0.0** breaking migration (CN) |
| [QuickStart.md](QuickStart.md) | ����ӽ��루������Manifest�����⡢���á�ҳ����ּܣ� |
| [core-base/README.md](core-base/README.md) | ����ģ�飻�ؼ������ⲹ�� |
| [core-db/README.md](core-db/README.md) | Room��**1.5.0** Э��ջ / Rx ���У����� processor |
| [room-processor/README.md](room-processor/README.md) | KSP Bridge��**1.2.0**���� suspend RawQuery�� |
| [core-form/README.md](core-form/README.md) | ��� / �����ȣ�ԭ commonui�� |
| [core-media/README.md](core-media/README.md) | ý��ѡͼ / ѹ���ȣ�ԭ commonmedia�� |
| [core-gnss/README.md](core-gnss/README.md) | GNSS / �켣��ԭ googlegps�� |

��ǰ���ɰ汾��**common `4.7.0`** �� **core-base `1.7.2`** �� **core-network `1.7.1`** �� **core-db `1.6.2`** �� **core-ui `1.3.1`** �� **core-utils `1.2.0`** �� **core-form `3.7.7`** �� **core-media `3.7.0`** �� **core-ws `1.0.1`** �� **core-gnss `4.0.0`**������Ϊ **Material3 DayNight**��Views + MDC 1.14����ҵ����Կ����� `common`���� Maven �밴 [UPGRADE.md ����](UPGRADE.md) **��ʽ��ס** core / ui / media / gps��������Ϊ `io.coderf.arklab.common.*`������˵���� [UPGRADE.md](UPGRADE.md)����������ͼ�� ��0.4.3������ģ�ͼ� ��0.4.2����

**1.2.0**��ɾ�� `BaseView` �� Repository/ViewModel �ϵ�ҳ�淺�ͣ����� UI �� `NetworkRequestUiHost` + **`RequestUi`**����ɾ���ɰ� `RequestUiCallback`����ҵ�񵼺��� LiveData/Flow��

**1.1.x / 3.6.x �ؼ�����**����ʷ�����ٷ�����Բ�ǡ���ߵİ�װ����ȥ������� `MaterialButton` / `ShapeableImageView` / `TextInputLayout`������� [core-base/README.md](core-base/README.md)��

# ��ܼ��
���ȫ�����`MVVM`�ܹ����JetPackȫ��Ͱ���з�װ���������Ҫ��װ�˳��ù��ܣ����������������ݿ⡢���ݴ洢�������ࡢUI�����ҵ���߼���װ�ȵ�,����90%Ϊ`Java`,10%Ϊ`Kotlin`����ͼ��ݵ�`Android 8` ��`targetSdk` ���� Demo����ǰ 35����
�Ӽܹ��ϸ����Ľ��в�⣬��ַ��ӽ����˼·����ԭʼ��`Activity`�������¼�����ֳɸ����ֿ���ģ�飬����`ViewModel`����ר��ʹ��`Repository`�������ݴ�����ӿ�ͨ��`Hilt`����ע��ķ�ʽ����һ��ע��ʡȥ������`New`������

**4.5.0 ��**��ԭ���� `common` ��Ϊ `core-utils` / `core-base` / `core-network` / `core-db` / `core-ui`��`common` ��Ϊ **facade**��`api` �ۺ� core����MQTT / ý���ʵ�ֿ⾭ **Gateway**��case ģ�� `:base`��ע�룬ҵ��ģ�鲻Ҫֱ������ `mqttcomponent` / `core-media`��ԭ `commonmedia`����

**4.6.0**��`AppBaseTheme` �� AppCompat Light ����Ϊ `Theme.Material3.DayNight.NoActionBar`����ɫ�� `values-night` tonal ɫ�壨�ر�ϵͳ `forceDarkAllowed`�����ؼ����� Views������ Compose������ API ��ɹ��̶��룬��� [UPGRADE.md](UPGRADE.md)��

**core-base 1.1.1 / core-form��ԭ commonui��3.6.1**���� 4.6.0 ����֮�����ؼ���ء������ùٷ� Material3 �ؼ��Ĳ��ٰ�һ�㣻��Ҫ XML `radius` / `bgColor` / `stroke*` ����������/���ֿؼ���Ϊ `MaterialShapeDrawable`������Ĭ�ϰ�ť `inset`/`minHeight` ���㣬`layout_height` ���Ӿ��߶ȡ�

��Ҫ��װ���ܣ�
1. ����`BaseActivity`��`BaseFragment`��Ҫ��ҳ��UI�Ļ����ࣨλ�� `core-base`��
2. �Զ�����`����ͷ��UI���䷽��`������ģ�� `:core-autosize`���� `core-base` ���ݣ�
3. ���������װ��`OkHttp`+`RxJava`+`Retrofit`���Լ�Э�� `Flow`��**�Ƽ�**�� API `BaseNetworkRepository`��`core` �������� Rx `RepositoryImpl` �Ա�������ɲ� `FlowRepositoryImpl` ��ɾ��
4. �������ݿ��װ��ʹ��`Room`+`RxJava` / `Flow`��`core-db`��
5. �������洢��ܣ�ʹ��`MMKV`���滻�Դ���`SharedPreferences`
6. ����`Paging3`��ҳ���������ܻ��ķ�ҳ�б��ͬʱҲ�ṩԭʼ�ķ�ҳ����
7. ���`Media`ģ��`core-media`��ԭ `commonmedia`����ר�Ŵ���ý�����ݣ�ҵ���ע�� `MediaGateway`
8. ��ֳ��ñ����������� module `core-form`��ԭ `commonui`����������У�飬�����֤������ύ�ȵ�
9. ��ֵ�����`GPS`������ģ��`core-gnss`���ṩGPS���񣬴Ӽ��GPS�Ƿ�򿪵���ȡȨ�޵���ȡ��γ��һ������
10. ʹ��`properties`�����ļ��������ļ����Զ���ֻ�������ֹƵ���л��������µ�һϵ������
11. �������`annotation`ģ�飬һ��ע��У���ֶα�������Ƿ�����Լ������
12. �ܹ���ͨ��ģ���� + Gateway ���ҵ��ֻ���� `*api`��ʵ���� app ��װ���
13. Material3 ������ Top App Bar / ��ť / Բ��ɫ��`AppBaseTheme`����̬ȡɫĬ�Ϲأ�

# ���̽���
    - **core-utils**���� R �������������ߣ�Ԥ��ֲ㣩
    - **core-base**��Base* / widget / helper / res��namespace ��Ϊ `io.coderf.arklab.common`���������� Material �ؼ�����ڴ�
    - **core-network**��Retrofit / Repository / `BaseNetworkRepository`
    - **core-db**��Room DAO / Database / RoomRepository
    - **core-ui**��Activity ί�У��� `InitDataPolicy`��
    - **common**���������棨facade����ҵ���Ƽ�ֻ������
    - annotation��ע���ܣ���Ҫ��������У���ע��
    - app����� case ʾ������ Gateway Hilt �󶨣�
    - core-media������ͷ����ᡢý����ط�װ��ԭ commonmedia������ Gateway��
    - core-form��UI �����װ����ҪΪ��������ԭ commonui��
    - core-gnss��GPS������
    - mqttcomponent��MQTT ��װ������ Gateway��
    - base��Demo/case ������Gateway��AppPropertiesConfig��BaseAppActivity �ȣ�**�ǿ��**������ core-base ������
    - userapi���û�ģ�����ӿڣ�UserService / UserInfo / Router��
    - user��ʾ��ҵ�� module������ common + base + userapi��
    - core-ws��WebSocket �����OkHttp������ core-mqtt��
    - room-processor��`@RoomObservedEntity` KSP ������

ȫ��ʹ��``ksp``

# ����ӿ����ʼ
[����ӿ����ʼ](QuickStart.md)�������Ѳ��� **���� UI��`NetworkRequestUiHost` / `RequestUi`��** ˵����ʾ������  
�Ӿɰ��������ȶ� [UPGRADE.md](UPGRADE.md)��

## MVVM�ܹ�ʾ�����룬�ع��汾

### ҵ���������ڣ������� `common`��4.7.0 facade��

#### �������ã�Maven��

�ֿ���ƾ֤ͬǰ��`ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`�����Ƽ����꣨��������� [UPGRADE.md](UPGRADE.md) / [MODULES.md](MODULES.md)����

```gradle
implementation 'io.coderf.arklab.common:common:4.7.0'
// һ����� common POM ���ݣ�Maven ����������ʽ����
// implementation 'io.coderf.arklab.core:base:2.0.0'
// implementation 'io.coderf.arklab.core:network:1.7.0'
// implementation 'io.coderf.arklab.core:db:2.0.0'
// implementation 'io.coderf.arklab.core:ui:1.3.1'
// implementation 'io.coderf.arklab.core:utils:1.1.1'
```

���ֿⷢ����`./gradlew :common:publish` / `:core-*:publish`����� [MODULES.md](MODULES.md)����

������������

```gradle
implementation project(':common')
implementation project(':base')      // Gateway / ���� / BaseApp*
implementation project(':userapi')   // ��Ҫ�û���Լʱ
```

### mqttcomponent / core-media / websocket

ʵ�ֿ��Կɵ����������� **ҵ�� module ��Ҫֱ������**��Ӧע�룺

- `MessageGateway`��MQTT��
- `MediaGateway`��ý�壩

�� **app** �� Hilt ��ʵ�֣��ο� `GatewayModule`��`MediaGatewayModule`�����ӿ��� **`:base`**��

### userapi

Ϊ **user ģ��** �Ĺ��� api���ӿڡ�ʵ�塢·�ɡ���Ҫ�� Gateway / ȫ�����á�

### base��case��

��Ŀ����ģ�����壺Gateway��`AppPropertiesConfig`��`PropertiesKeyEnum`��`FileTypeEnum`��`BasePage`��`BaseAppActivity` �ȡ��������ɰ� serm-app �� `:base` ˼·��չ�ֵ䡢�ļ�����ȣ�**��Ҫ��ҵ���ֵ�Ӳ�������**��

### user

�����ʾ�������� `common` + `base` + `userapi`������ҵ�� module ��Ҫ���� `user` ʵ��ģ�顣
## ��װ˼·���ģ�ע��ۿ�˳���

һ��https://blog.csdn.net/fzkf9225/article/details/105197996

����https://blog.csdn.net/fzkf9225/article/details/105200803

����https://blog.csdn.net/fzkf9225/article/details/132182449

## �鿴��������������־

### ��ʼ���ʹ���־

��app�е�Application�е��ó�ʼ������

```
        // ���¾����� init ֮ǰ����д����Ĭ��ֵ
        // Config.getInstance().setDynamicColorEnabled(true);          // Material You��Ĭ�Ϲ�
        // Config.getInstance().setAutoSizeEnabled(false);             // ��Ļ���䣬Ĭ�Ͽ�
        // Config.getInstance().setCrashHandlerEnabled(false);         // �Դ���������Ĭ�Ͽ�
        // Config.getInstance().setCrashLogRetainDays(7);              // ������־����������Ĭ�� 5��<=0 ������
        // Config.getInstance().setNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        // Config.getInstance().setEdgeToEdgeEnabled(false);           // BaseActivity ȫ��Ĭ�ϣ�Ĭ�Ͽ�
        // Config.getInstance().setDefaultPlaceholderRes(R.drawable.my_placeholder);
        // Config.getInstance().setDefaultErrorImageRes(R.drawable.my_error);
        Config.getInstance().init(this);
        if (BuildConfig.LOG_DEBUG) {
            Config.getInstance().enableDebug(true);
        }
```
### ����Api

#### ϵͳ����
1. �̳�`BaseApplication`��Ȼ���ʼ��`Config.getInstance().init(this)`����Ļ���䡢��������ҹ��ģʽ��Edge-to-Edge��ռλͼ��ȫ�ֿ������� `init` ǰ���á������Ҫ��ȡȫ�ֵ�`Application`������Ե���`Config`�ķ���
2. `ErrorService`���ṩȫ�ֵ�api��������������ص���һЩ�������ת��¼���ص����⣬����ͳһ��������ͷ�����õ�

#### UI���
1. ��ͨ`Activity`�̳�`BaseActivity`����
2. ��ͨ`Fragment`�̳�`BaseFragment`����
3. ��ͨ`Adapter`�̳�`BaseRecyclerViewAdapter`����
4. �����Ҫ`pinging`��ҳ��`Adapter`�Ļ����̳�`BasePagingAdapter`
5. 列表分页请继承 `BaseSmartPagingFragment`（原 SwipeRefreshLayout 版 `BasePagingFragment` 已移除）
6. ����`pinging`��ҳ���б�`Fragment`�̳�`BaseRecyclerViewFragment`
7. ��õķ�ҳ�б�`Fragment`��֧�ֶ���ʽˢ�£����ظ��࣬`paging3`��ҳ�̳�`BaseSmartPagingFragment`
8. ����� UI�����ؿ� / ���� / ҵ���룩�� `BaseActivity` / `BaseFragment` ʵ�� **`RequestUi`**���� `NetworkRequestUiBinder` ���� ViewModel �� `NetworkRequestUiHost`������ͳһ `showError(AppError)`��ҵ�񵼺��� LiveData/Flow�����¼ `PostLoginRoute`����ViewModel/Repository ������ҳ��
9. `adapter`�����Ҫʵ���Զ�����¼��ؼ��ȣ������Զ���`BaseViewHolder`��Ȼ����adapter����дcreateViewHold����
10. ��ͨ`ViewModel`����ֱ�Ӽ̳�`BaseViewModel`
11. ��ҳ�б��`ViewModel`�̳�`PagingViewModel`
12. ֧��tab��ǩ�໬���ܵ�`Fragment`�̳�`BaseViewPagerFragment`���������Ҫ`Fragment`�Ļ�ֱ��`Activity`�̳�`BaseViewPagerActivity`
13. �Զ����������ֱ�ӵ���`CameraActivity`����Ƶ���ſ���ֱ�ӵ���`VideoPlayerActivity`����ά��ɨ�����ֱ�ӵ���`CaptureActivity`��WebView����ֱ�ӵ���`WebViewActivity`

#### ���ݴ洢�������������
1. ���������´����Ƽ� `BaseNetworkRepository`��`core` ����`Flow<RequestResult<T>>`������ Rx `RepositoryImpl` �Ա�������ɲ� `FlowRepositoryImpl` ��ɾ����Demo �� `SampleCoreNetworkRepository`
2. ���������װ`ApiRetrofit`��ͨ��`Builder`ģʽ����������`Api`�ο�`ApiRetrofit.Builder`
3. ������ش洢���ݵĻ�����ʹ��`MMKVHelper`����`SharedPreferences`�����ݿ�Ļ������Լ̳�`BaseRoomDao`��**Rx ջ**�� `RoomRepositoryImpl`��**Э��ջ**�� `io.coderf.arklab.core.db.RoomDataRepository`����������� `BaseNetworkRepository`��Room ԭ�� `*Suspend`������ `Flow<RequestResult>`�����ײ��в��Žӣ�������ģ��� `AttachmentDao` / `AttachmentDatabase` / `AttachmentRepositoryImpl`��ҵ����� `mobileId`��������� `fileId`�������� `mainId`/`fieldName` ���顣DAO �� `@RoomObservedEntity` �� KSP ���� `XxxDaoRawQueryBridge`��Maven��`core-base:1.7.2` + `core-db:1.6.2` + `room-processor:1.2.0`����� [core-db/README.md](core-db/README.md)��[UPGRADE.md ��0.4.2](UPGRADE.md#042-����ģ�ͼ�base-171--db-162) / [��0.4.3](UPGRADE.md#043-����Сͼ��������ͼbase-172--ui-377)
4. ���е�`Retrofit`�������������ӿڶ���Ҫ����`BaseApiService`
5. ���������ҳ`PagingSource`�̳�`PagingSource`��Э��`flow`��ʽ�̳�`FlowPagingSource`������ `core-network`�������ݿ�洢��ҳ�̳�`RxRoomPagingSource`��`core-db`����ѯ������ `RoomPagingQuery`��
6. `RetryService`����װ�����������������Ի��ƣ�������`Repository`�����û���`ApiRetrofit`�����ã�`ApiRetrofit`����ΪĬ�ϵģ�`Repository`��Ϊ��ǰ�า��Ĭ�ϵ�����
7. `FlowRetryService`��Э�̷�ʽ�������������ԣ�������`Repository`�����û���`ApiRetrofit`�����ã�`ApiRetrofit`����ΪĬ�ϵģ�`Repository`��Ϊ��ǰ�า��Ĭ�ϵ�����
8. `RoomRepositoryImpl`�����ݿ��һЩ���÷���
9. `PagingRepositoryImpl`��`paging3`��ҳ��`Repository`���װ
10. Э�̷�ҳ��`NetworkPagingRepository` + `NetworkFlowPagingViewModel`��`core` ����
11. Rx ��ҳ��`PagingRepositoryImpl` + `PagingViewModel`��`common.repository`���Ա����
#### ���ù�����
1. `AppManager`����ȡ`Activity`ջ��Ϣ�ͻ�ȡApp�汾�š��汾���ơ�App�Ƿ���ǰ̨���е�API
2. `AppSettingHelper`���������û�����app�Ƿ��״����еȷ���
3. `ConstantsHelper`��������һЩ���þ�̬����
4. `MobileHelper`����ȡ�ֻ���һЩ��Ϣ�������ȡ�ֻ���IMEI��MAC��ַ���ֻ��ͺš��ֻ�ϵͳ�汾���ֻ��ֱ��ʡ��ֻ�Ψһ��ʶ����
5. `RegexUtils`��������ʽ��λ�� `core-utils`������ `io.coderf.arklab.core.utils`��
6. `AuthManager`����¼��ص�һЩ���ã�`Fragment`��`Activity`���еĶ��󣬿���ֱ�ӻ�ȡʹ��
7. `UIController`������`Fragment`��`Activity`һЩ`dialog`��`toast`��`loading`��
8. `PropertiesModule`������Properties������Ի�ȡ�����ļ���Ĳ���
9. `CacheUtil`�����湤��
10. `CollectionUtil`�����Ϲ�����
11. `DownloadManger`�����ع������
12. `PermissionManager`��ϵͳȨ�޹��������
13. `NetworkStateUtil`������״̬������
14. `StringUtil`: �ַ���������
15. `RxMenuView`��`Toolbar`�˵��ؼ��������ʱ�䣬�����ؿ��ٵ���¼�
16. `RxView`��`View`�������ʱ�䣬�����ؿ��ٵ���¼�
17. `QRCodeUtil`����ά�빤����
18. `MathUtil`����ѧ���㹤���࣬�Ӽ��˳���һЩ��ʽ��������С��λ����
19. `NumberUtil`�����ִ�Сдת�������ָ�ʽ����
20. `KeyBoardUtil`������̿��ơ����ѡ��رպͼ�����
21. `GeoMapUtil`������ת������
22. `FastBlur`����˹ģ��������
23. `ThreadExecutor`���̳߳ع�����
24. `ThreadExecutorBounded`���̳߳ع����࣬�߽����Ĭ��Ϊ128

#### �Զ���View���

�ٷ�����Բ�� / ��ߵĳ�����ֱ���� Material3 �ؼ���**1.1.1 ��ɾ����Ӧ��װ��**����

| ���� | ʹ�� |
|------|------|
| Բ�� / ��߰�ť | `MaterialButton`��`app:cornerRadius` / `app:backgroundTint` / `app:stroke*`�� |
| Բ�� / Բ��ͼƬ | `ShapeableImageView`��`app:shapeAppearanceOverlay`���� `CircleShapeAppearance` / `RoundedShapeAppearanceL`����� `app:strokeColor` / `app:strokeWidth`����**��ɫ�� `android:background`����Ҫ�� `app:backgroundTint`** |
| ��������� | `TextInputLayout` + `TextInputEditText`������ `Widget.App.TextInputLayout.Dense`�� |
| ����������� View ��Բ�ǵ� | `CornerShapeHelper.apply` / `applyFromAttributes`���� `ShapeView`��`radius` / `bgColor` / `stroke*`�� |

�ٷ� **û��** XML Բ�ǡ���䡢������ԣ�����Ա����װ������Ϊ `MaterialShapeDrawable`����

1. `AutoNextLineLinearlayout`���Զ����е�`ViewGroup`
2. `CircleTextView`��Բ�� `MaterialTextView`��XML `bgColor` / `stroke*`�����ְ�Բ�ľ���
3. `CirclePaddingImageView`��Բ��**����** + `android:padding` ����ͼ�꣨���� `AppCompatImageView`�����ǲ�Բͷ��ͷ���� `ShapeableImageView` + `CircleShapeAppearance`��
4. `CustomScrollView`��֧�������ص�Ч���� `ScrollView`
5. `Code`������ͼ����֤��Ĺ����࣬�ɻ�������ַ��͸�����
6. `DividerView`���ɻ��ƺ��߻����ߵ����߿ؼ���Kotlin ʵ�֣�
7. `CircleProgressBar`��Բ�ν�������֧��������ʾ���������ȸ���
8. `GradationRectTextView`��֧�ֽ��䱳��������������ʽ��ͬ�� `TextView`
9. `GridMenuView`����ҳ����˵����̳� `CornerConstraintLayout`��`Kotlin`��
10. `ClearableEditText` / `PasswordEditText`��Բ������ + ��� / �����л���compound drawable���ٷ� `TextInputLayout` �޷�ͬʱ�� + �ۣ�
11. `CornerLabelView`�������νǱ�ؼ�������ʾ���ĸ����䣬֧�����ֺͱ���ɫ
12. `CornerTextView`��Բ�� `MaterialTextView`��XML `radius` / `bgColor` / `stroke*`
13. `HorizontalProgressBar`��ˮƽ�����������֧��Բ�Ǳ�����������ʾ
14. `ConfigurableWebView`�������õ� `WebView`��֧�ּ��ر��� assets ������ URL
15. `AutoTextView`���� 3D ��ת�����������л��ؼ������� `TextSwitcher`��
16. `CustomSearchEditText`��������ͼ���������ܵ� `EditText`��֧��Բ�Ǳ���
17. `CornerConstraintLayout`��Բ�� `ConstraintLayout`��XML `radius` / `bgColor` / `stroke*`
18. `BannerView`���ֲ�ͼ�ؼ���֧��Բ��ָʾ�����Զ��ֲ���Բ�ǲü�
19. `StarBar`���Զ����������ֿؼ���֧��ʵ��/��������ǡ�����/С�����֡������������ֵȹ���
20. `IconDotTextView`��ͼ��+����+���ֽǱ����Ͽؼ���֧�ֶ��ֲ��ַ���ͼ������λ�ã��ͽǱ��Զ���
21. `SquareLabelView`�����������`TextView`������ģ�����Ƶȳ�����֧�ַ�����״�����Ρ���Բ��Բ�Ǿ��Σ���λ���Զ���
22. `IconLabelValueView`��ͼ��-��ǩ-ֵ-ͼ��Ĳ�����ͼ�������������������չʾ��֧������ͼ�꿪�ء��ײ��߿�ֵ���뷽ʽ������
23. `VerificationCodeInputView`����֤������ؼ���֧�ֶ���������򡢹����˸�������������ƣ����֡���ĸ�ȣ����߿���ʽ�Զ���
24. `ScalingTextView`����չ��/������ı��ؼ���Ĭ����ʾָ��������֧�֡��鿴ȫ��/����ȫ�ġ�����л�
25. `LoadingProgressDialog`�����ؽ��ȶԻ���֧�ֶ�̬ʡ�ԺŶ���Ч�����Զ�����ʾ���֡��������Ƿ��ȡ��
26. `DatePickDialog`��������ѡ��Ի���֧�ֶ�������ģʽ�������ա����¡���ݡ�ʱ��ȣ����Զ������ڷ�Χ�ͱ�ǩ
27. `BottomSheetDialog`���ײ�ѡ���Material `BottomSheetDialog`����֧���б�ѡ����ʾ���Զ���ָ�����ʽ��ȡ����ť
28. `MenuDialog`���ײ��˵��Ի��򣬹������ơ�BottomSheetDialog����ʹ�ô�ͳDialogʵ�֣�֧���Զ���λ�ú���ʽ
29. `ImageSaveDialog`��ͼƬ����ѡ��Ի����ṩ�����浽���ء���ѡ�����ͼƬ���泡��
30. `EmptyLayout`���հ�ռλ���ֿؼ�������ʾ�����С�����ʧ�ܡ������ݵ�״̬��֧���Զ���ͼ��͵������
31. `InputDialog`����������Ի���֧���ı����롢����������ơ������������ú���ʾ����
32. `EditAreaDialog`�������ı�����Ի��򣬹�������`InputDialog`��֧�ֶ����ı��༭
33. `UpdateMessageDialog`��Ӧ�ø�����ʾ�Ի�����ʾ�汾��Ϣ�͸������ݣ�֧������ʶ�����������
34. `ConfirmDialog`��ȷ�϶Ի����ṩȷ����ȡ����ť��֧�ָ��ı�������ʾ�Ͱ�ť�ɼ��Կ���
35. `ProgressBarDialog`���������Ի���֧��Բ�κ�ˮƽ���ֽ�������ʽ�����Զ����������ۺͰ�ť
36. `MessageDialog`����Ϣ��ʾ�Ի��򣬵���ť��ƣ����ڼ���Ϣ��ʾ����
37. `ProtectionGuidelinesDialog`�����ҳ���û�Ȩ����˽ָ��������Ȩ����ʾ����֧�ָ��ı����ݡ��Զ��尴ť���ֺ���ʽ������Ȩ�����볡��
38. `CascadeSinglePopupWindow`����ѡ�������
39. `PopupView`����ͨ����ѡ�񵯴�����ѡ��
40. `TextPopupView`���Զ����ı�+ͼ����Ͽؼ�
41. `MultiPopupView`����ѡ���Ǽ���������
42. `TreePopupView`����������ѡ�񵯴�
43. `CascadeMultiPopupWindow`����ѡ�������
44. `TxSlideRecyclerView`��֧�ֲ໬��`RecyclerView`
45. `SpeakButton`����΢�ŵĳ���˵��View
46. `VoiceView`������������ؼ�

#### Form�����ؿؼ�
��Ҫ���ڳ�����һЩ�������ͻ������
1. `FormDate`������ѡ��ؼ�
2. `FormDateRange`�����ڷ�Χѡ��ؼ�
3. `FormDateTime`������+ʱ��ѡ��ؼ�
4. `FormEditArea`���ı�������ؼ�
5. `FormEditText`�������ı�����ؼ�
6. `FormFile`���ļ�ѡ��ؼ�������ȫ����ʽ���ļ�ѡ��
7. `FormFilePreview`���ļ�Ԥ���ؼ�
8. `FormImage`��ͼƬѡ��ؼ�
9. `FormImageAndVideo`��ͼƬ+��Ƶѡ��ؼ�
10. `FormImageAndVideoPreview`��ͼƬ+��ƵԤ���ؼ�
11. `FormImagePreview`��ͼƬԤ���ؼ�
12. `FormRichText`�����ı��ؼ�
13. `FormSelection`������ѡ��ؼ�
14. `FormTextView`���ı�Ԥ���ؼ���֧�ֵ��ж�����ʾ����Ҫ��������ҳ�ı������
15. `FormTime`��ʱ��ѡ��ؼ�
16. `FormVideo`����Ƶѡ��ؼ�
17. `FormVideoPreview`����ƵԤ���ؼ�
18. `DateRangePickDialog`�����ڷ�Χѡ��`dialog`
19. `TickViewMessageDialog`���Թ���������
20. `CalendarView`���Զ�������

#### GPS���api
1. `GpsService`: GPS��̨����
2. `GpsLifecycleObserver`��GPS�������ڹ۲��ߣ�����ֱ���������۲��ߣ�����GPS�Ƿ����Ȩ���жϵȵ�
### ��������������־

����ApiRetrofit�µ�TAG
TAGֵΪ������`ApiRetrofit`

## ����ָ����

### ���빫��������

���ڼ��ɵ�aar���и���Ч�������Ҫ�ڼ̳�base��ʱ�������ǵİ�����Ȼ�ͻ��Ҳ������õĵ������⣬���������û����aar���ö���ֱ��module���õĻ�������ֱ�ӽ�
`implementation` �ĳ�`api`����
�ɲο�appģ���demo������

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
    implementation project(':core-form')
    implementation project(':core-gnss')
    // ��װ�������ʵ�ֿ⣻ҵ�� module ͨ�� Gateway ע��
    implementation project(':core-media')
    implementation project(':core-mqtt')
    implementation project(':user')
    implementation project(':core-ws')

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
    // ���ڲ���
    testImplementation libs.androidx.paging.common// [��ѡ] RxJava ֧��
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

### ��������

```
# ==================== MVVM Common / Core Library ====================
# common facade + core-*�������Զ�Ϊ io.coderf.arklab.common.*��
-keep class io.coderf.arklab.common.** { *; }
-dontwarn io.coderf.arklab.common.**
-keep class io.coderf.arklab.core.** { *; }
-dontwarn io.coderf.arklab.core.**
-keep class io.coderf.arklab.base.gateway.** { *; }

# �������ݰ������
-keep class io.coderf.arklab.common.bean.** { *; }
-keepclassmembers class io.coderf.arklab.common.bean.** {
    void set*(***);
    *** get*();
}

# ���� BaseResponse �� ViewModel
-keep class io.coderf.arklab.common.base.BaseResponse { *; }
-keep class io.coderf.arklab.common.viewmodel.** { *; }

# ����ע�⴦�������ɵ���
-keep class * extends io.coderf.arklab.common.base.** { *; }

# ������Դ������ࣨ������Զ����ɵİ��ࣩ
-keep class *Binding { *; }
-keep class *BindingImpl { *; }
```
```
# ==================== Media Common Library ====================
# ���� Media ��ܺ�����
-keep class io.coderf.arklab.media.** { *; }
-dontwarn io.coderf.arklab.media.**
-keep class io.coderf.arklab.media.gateway.** { *; }

# ��������ģ����
-keep class io.coderf.arklab.media.bean.** { *; }
-keepclassmembers class io.coderf.arklab.media.bean.** {
    void set*(***);
    *** get*();
}

# ������Ĺ�����͹�����
-keep class io.coderf.arklab.media.MediaBuilder { *; }
-keep class io.coderf.arklab.media.MediaHelper { *; }
-keep class io.coderf.arklab.media.utils.** { *; }

# ����ص��ӿ�
-keep class io.coderf.arklab.media.callback.** { *; }
-keep interface io.coderf.arklab.media.callback.** { *; }

# �����ý�崦�����
-keepclassmembers class io.coderf.arklab.media.** {
    public * compress*(...);
    public * process*(...);
    public * encode*(...);
    public * decode*(...);
}
```

```
# ==================== core-form Library��ԭ commonui��====================
# ���� core-form ��ܺ����ࣨ���� io.coderf.arklab.ui��
-keep class io.coderf.arklab.ui.** { *; }
-dontwarn io.coderf.arklab.ui.**

# ȷ������ģ�Ͳ�������
-keep class io.coderf.arklab.ui.bean.** { *; }
-keepclassmembers class io.coderf.arklab.ui.bean.** {
    void set*(***);
    *** get*();
}

# �����Զ��� Widget ���
-keep class io.coderf.arklab.ui.widget.** { *; }
-keepclassmembers class io.coderf.arklab.ui.widget.** {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
}

# ���� Fragment
-keep class io.coderf.arklab.ui.fragment.** { *; }

# ����������
-keep class io.coderf.arklab.ui.adapter.** { *; }

# ��������ع���
-keep class io.coderf.arklab.ui.form.** { *; }

# ����ӿںͻص�
-keep interface io.coderf.arklab.ui.inter.** { *; }
-keep class io.coderf.arklab.ui.impl.** { *; }

# �������ݰ������
-keep class *Binding { *; }
-keep class *BindingImpl { *; }
```

```
# ==================== Google GPS Library ====================
# ���¹����������� core-gnss �� consumer-rules.pro �У��� AAR �Զ���Ч�����������ظ�������
# ���ṹ��api(���� API) / service(����+Session) / permission(Ȩ������) / track(�켣�ļ�)
#          / utils(����)  ���� �������location / ui Ϊ�ڲ�ʵ�֣����������ԭ socket �ϴ����Ƴ���
-dontwarn io.coderf.arklab.gnss.**

# ���� API��GpsStarter / GpsCallback / GpsSettingConfig / LocationFilter
-keep class io.coderf.arklab.gnss.api.** { *; }
-keepnames class * implements io.coderf.arklab.gnss.api.LocationFilter

# ��λ���� GpsService / GpsBinder / Session
-keep class io.coderf.arklab.gnss.service.** { *; }

# Ȩ������ GpsLifecycleObserver / PermissionsChecker
-keep class io.coderf.arklab.gnss.permission.** { *; }

# �켣�ļ� IFileLogger / Files / GpsFileParser
-keep class io.coderf.arklab.gnss.track.** { *; }

# ������ DebugUtil / LogUtil / EsriUtil
-keep class io.coderf.arklab.gnss.utils.** { *; }
```
### hilt�������

����Ŀ��Ŀ¼��build.gradle���

```
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.hilt.android)
}
```

�ڸ���ģ����Ӳ�����ã��Ǹ�ģ���õ����ĸ�ģ����ӣ�ÿ���õ��ĶҪ���

```
plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.hilt.android)
}
```

�ڸ���ģ����Ӳ�����ã��Ǹ�ģ���õ����ĸ�ģ����ӣ�ÿ���õ��ĶҪ���

```
    implementation libs.google.hilt.android
    ksp libs.google.hilt.android.compiler
```

### �½�assetsĿ¼

�����½�dev.properties��release.properties��������һЩ�������ã���commonģ�����Ѿ������ˣ�����key����ο�demo�еĵ�properties�����ļ���Ҳ�����Զ���һЩ����

����Ĭ�����ã�ע�����key���ܴ��󣬲ο�demo�е�

```
    PropertiesUtil.getInstance().loadConfig(application).getBaseUrl()
```

�����Զ����ļ��е��Զ�������

```
    PropertiesUtil.getInstance().loadConfig(application).getPropertyValue(key,defaultValue)
```

����Ĭ�ϼ��ص���'prod.properties'�����ļ��е����������Ҫ���ر��ֱ����builde.gradle���������resValue���ü���

```
    buildTypes {
        release {
            resValue "string", "app_config_file", "prod.properties"//ָ���ļ�����app_config_file�����޸ģ������prod.properties"�����޸�
            minifyEnabled true// ����
            shrinkResources true // �Ƴ����õ�resource
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
        debug {
            resValue "string", "app_config_file", "dev.properties"//ָ���ļ�����app_config_file�����޸ģ������dev.properties"�����޸�
            minifyEnabled false// ����
            shrinkResources false // �Ƴ����õ�resource
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
```

### �½�Application�̳�BaseApplication

һ��Ҫ���ã���Ȼcommon�кܶ����ᱨ�����application�г�ʼ��һЩ�����������磺MMKV�������ܡ�WebSocket��mqtt��
�����Ҫ��ӡ��־�Ļ������Config�ӿ�
���ó�ʼ������

```
    // ����ȫ�ֿ��ؼ����ġ�ϵͳ���á������� init ǰ����
    Config.getInstance().init(this);
```

### CommonUI�и����������ͳһ���ϴ�����
```
        MediaUploadConfig.getInstance()
                .setFileApiService(fileApiService)
                .setUploadUrl("minioc/upload");
```

### �½�ErrorServiceImplʵ��ErrorService�ӿ�

�����кܶ��װ�õ�api��Ҫʵ�֣���Ȼ��Ҳ���Բ��ã�������ֱ���޸�common�������Ϊ��������ľ��ǽ����ʲô����common��ʵ��������Ա�Ȼ�ͻ�ܸߣ���ȻҲҪ�½�ErrorServiceModule������hilt�÷������������Լ��ٶ���

### ǿ��һ��

�ڽ���hiltʱÿ��module��Ҫ�������������������٣����˵Ļ�����Ա��뵫���޷����У������㻹�Ҳ���������
ÿ���õ���ģ�鶼Ҫ��������������ʹ��api

``` 
    implementation libs.google.hilt.android
    ksp libs.google.hilt.android.compiler
```

### ��Ŀ���Զ�Ϊ�������˽���ͷ�������䷽���������Ҫ���������ļ�����������

    <meta-data
        android:name="design_width_in_dp"
        android:value="360" />
    <meta-data
        android:name="design_height_in_dp"
        android:value="640" />

### ���������������

�ڸ�ʵ��`RetryService`�ӿڣ��ýӿڻ��Զ�ʵ��������û�ģ��ʵ�֣���Ϊ�ýӿ���Ҫ����ʵ�ֹ������¼������ڵ�¼ģ��ʵ����Ϊ�����ʾ����������ʾ�����˹��ܹ���hilt��ʹ�ã������Ҫ�˽�hilt����÷�,��Ҫ��ViewModel�����ע��@HiltViewModel,
��Ȼ��Ҳ���Գ����д����ָ���������Է��������Բ���д��Ҳ���Բ���ע�⣬ϵͳ��Ĭ��һ�������������벻Ҫ��дΪnull

### ����ͬ��ߴ������

��dimens��Դ�ļ���������ͳһ�ĳߴ��С������ά��

### ����һЩ��װ�����ĵ���

#### ͼƬ��Ƶѡ���

��ʱͼƬ��Ƶѡ��û�������������ƣ���ҿ�������ʵ�֣������һ����
ѡ��ͼƬ��

```
    new OpenImageDialog(requireActivity())
        .setMediaType(OpenImageDialog.CAMERA_ALBUM)
        .setOnOpenImageClickListener(mediaHelper)
        .builder()
        .show();
```

ѡ����Ƶ

```
    new OpenShootDialog(requireActivity())
        .setMediaType(OpenShootDialog.CAMERA_ALBUM)
        .setOnOpenVideoClickListener(mediaHelper)
        .builder()
        .show();
```

��ʼ������ȡ��ص���Ϣ

```
//��ͳ��ʼ����ʽ
    mediaHelper = new MediaBuilder(this, this)
        .setImageMaxSelectedCount(1)
        .builder();
    mediaHelper.getMutableLiveData().observe(this, mediaBean -> {
            
    });
//hilt��ʼ����ʽ��activity��
    @Inject
    @MediaModule.ActivityMediaHelper
    MediaHelper mediaHelper;
    
//hilt��ʼ����ʽ��fragment��
    @Inject
    @MediaModule.FragmentMediaHelper
    MediaHelper mediaHelper;
```

#### ��ӭҳ��˽Ȩ��dialog
```
        new ProtectionGuidelinesDialog(this)
                .setCanOutSide(false)
                .setPositiveBackgroundColor(ContextCompat.getColor(this, io.coderf.arklab.common.R.color.theme_green))
                .setSpannableContent(getSpannableContent())
                .setOnNegativeClickListener(dialog -> {

                    showToast("�ܾ����ܻᵼ�²��ֹ���ʹ���쳣");
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
### �Զ���ע��ʹ��

```java
import android.net.Uri;

import androidx.databinding.Bindable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.TypeConverters;

import java.util.List;

import io.coderf.arklab.annotation.annotation.VerifyEntity;
import io.coderf.arklab.annotation.annotation.VerifyField;
import io.coderf.arklab.annotation.annotation.VerifyParams;
import io.coderf.arklab.annotation.annotation.VerifySort;
import io.coderf.arklab.annotation.enums.VerifyType;
import io.coderf.arklab.annotation.inter.VerifyGroup;
import io.coderf.arklab.common.bean.BaseDaoBean;
import io.coderf.arklab.common.converter.RoomListStringConverter;

/**
 * Created by fz on 2023/9/5 18:32
 * describe :
 */
@Entity
@VerifyEntity(sort = true)
public class Person extends BaseDaoBean {
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "����Ϊ�գ�"),
            @VerifyParams(type = VerifyType.LENGTH_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, minLength = 2, maxLength = 10, errorMsg = "�����������"),
            @VerifyParams(type = VerifyType.EQUALS, group = {VerifyGroup.Default.class}, errorMsg = "��ֻ����������", equalStr = "����")
    })
    @VerifySort(1)
    @ColumnInfo
    private String name;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "��ѡ���Ա�"),
    })
    @VerifySort(2)
    @ColumnInfo
    private String sex;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "��ѡ�����գ�"),
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
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "����д�ֻ����룡"),
            @VerifyParams(type = VerifyType.MOBILE_PHONE, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "�ֻ������ʽ���벻��ȷ��")
    })
    @VerifySort(4)
    @ColumnInfo
    private String mobile;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "����д�̻����룡"),
            @VerifyParams(type = VerifyType.TEL_PHONE, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "�̻������ʽ���벻��ȷ��")
    })
    @VerifySort(5)
    @ColumnInfo
    private String tel;

    @VerifySort(6)
    @VerifyParams(type = VerifyType.NUMBER_RANGE, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, minNumber = 0, maxNumber = 120, errorMsg = "����������")
    @ColumnInfo
    private String age;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "����Ϊ��"),
            @VerifyParams(type = VerifyType.NUMBER_00, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "���������ʽ����ȷ"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, maxNumber = 200, errorMsg = "��ü����ˣ�����"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, minNumber = 40, errorMsg = "���Ѿ��ݳ�����ˣ�����")
    })
    @VerifySort(7)
    @ColumnInfo
    private String weight;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "���Ϊ��"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, maxNumber = 300, errorMsg = "Ҧ����û��ߣ�����"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, minNumber = 40, errorMsg = "�����������ƣ���ɹɹ̫�������")

    })
    @VerifySort(8)
    @ColumnInfo
    private String height;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Create.class}, errorMsg = "�����ַΪ�գ�"),
            @VerifyParams(type = VerifyType.EMAIL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "�����ַ����")
    })
    @VerifySort(9)
    @ColumnInfo
    private String email;

    @VerifySort(10)
    @VerifyParams(type = VerifyType.NOT_EMPTY, errorMsg = "������д���İ��ã�")
    @ColumnInfo
    @TypeConverters({RoomListStringConverter.class})
    private List<String> hobby;

    //    @VerifyFieldSort(11)
//    @VerifyParams(type = VerifyType.NOTNULL, notNull = true, errorMsg = "��ѡ�����ı�����Ƭ��")
    @Ignore
    private List<Uri> imageList;

    //    @VerifyFieldSort(12)
//    @Valid(notNull = true, errorMsg = "��ѡ�����ļ�ͥ��Ϣ��")
    @Ignore
    public Family family;

    //    @VerifyFieldSort(13)
//    @Valid(notNull = true, errorMsg = "��ѡ�����ļ�ͥ������Ϣ��")
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
        notifyPropertyChanged(io.coderf.arklab.demo.BR.name);
    }

    @Bindable
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.birthday);
    }

    @Bindable
    public String getEducationalExperienceDate() {
        return educationalExperienceDate;
    }

    public void setEducationalExperienceDate(String educationalExperienceDate) {
        this.educationalExperienceDate = educationalExperienceDate;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.educationalExperienceDate);
    }

    @Bindable
    public String getSchoolStartTime() {
        return schoolStartTime;
    }

    public void setSchoolStartTime(String schoolStartTime) {
        this.schoolStartTime = schoolStartTime;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.schoolStartTime);
    }

    @Bindable
    public String getClassStartTime() {
        return classStartTime;
    }

    public void setClassStartTime(String classStartTime) {
        this.classStartTime = classStartTime;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.classStartTime);
    }

    @Bindable
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.age);
    }

    @Bindable
    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.weight);
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.email);
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
        notifyPropertyChanged(io.coderf.arklab.demo.BR.mobile);
    }

    @Bindable
    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.tel);
    }

    @Bindable
    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.height);
    }

    @Bindable
    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
        notifyPropertyChanged(io.coderf.arklab.demo.BR.sex);
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
��ʼУ��
```
            VerifyResult verifyResult = EntityValidator.validate(binding.getData());
            showToast((verifyResult.isOk() ? "��֤�ɹ�" : "��֤ʧ�ܣ�") + StringUtil.filterNull(verifyResult.getErrorMsg()));
            if (!verifyResult.isOk()) {
                return;
            }
```

#### �Զ������
�ο�ʾ������`CustomCameraActivity`

#### Dialog������װʾ��
�ο�ʾ������`DialogActivity`

#### �Զ��������װʾ��
�ο�ʾ������`WightActivity`

#### ��ά������ʾ��
�ο�ʾ������`ScanQrCodeActivity`

#### GPS����ʾ��
�ο�ʾ������`GoogleGPSActivity`

#### �Զ��������
�ο�ʾ������`VerifyActivity`

#### �ϵ���������

��ͨ�ļ�����ʾ����

```
               Disposable disposable = DownloadManger.getInstance().download(this, binding.editUrl.getText().toString().trim())
                    .subscribe(file -> {
                        LogUtil.show(ApiRetrofit.TAG, "���سɹ���" + file.getAbsolutePath());
                        showToast("���سɹ���");
                    }, throwable -> {
                        if (throwable instanceof BaseException baseException) {
                            showToast(baseException.getErrorMsg());
                            return;
                        }
                        showToast(throwable.getMessage());
                    });
```

�汾����ʾ����

```
    UpdateManger.getInstance().checkUpdateInfo((Activity) view.getContext(),
                        "http://softfile.3g.qq.com:8080/msoft/179/24659/43549/qq_hd_mini_1.4.apk",
                        "1���޸���֪bug",
                        "1.0.1");
```

#### ��Ƶ����

��Ŀ�м�����github�ϵ�gsy��Ƶ���ſ⣬Ŀǰ���Ҿ��ÿ�Դ���м�������õ��˰�ֱ�ӵ���VideoPlayerActivity���Activity�����ˣ��������Ź��ܣ������ĳ����������ǿ�����Ҫ�Լ�����������

#### ��ͼԤ�����

```
    new PreviewPhotoDialog(this)
        .createImageInfo(imageUrl)
        .currentPosition(0)
        .show()
```

#### ����һЩ�����Ĺ�����

base��װ��һЩdialog���Զ���view��װ�ȵȿ���������Դ���в鿴�˽⣬��һһ������

