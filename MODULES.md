# ģ��˵����ʹ��ָ��

> ��Ӧ�汾���Ը�ģ�� `build.gradle` Ϊ׼����**common `4.7.0`** �� **core-base `1.6.0`** �� **core-network `1.7.0`** �� **core-db `1.6.1`** �� **core-ui `1.3.1`** �� **core-utils `1.1.1`** �� **core-log `1.2.0`** �� **room-processor `1.2.0`** �� **core-ws `1.0.1`** �� **core-form `3.7.4`** �� **core-media `3.7.0`** �� **core-gnss `4.0.0`**  
> Options ���Ƽ� [UPGRADE.md ��0](./UPGRADE.md#0-160network--db����-options-����)���޳� `BaseView` �� [UPGRADE.md ��2](./UPGRADE.md#2-120�����޳�-baseview--mvp-ҳ���)��  
> ����˵����ģ��ְ��������ϵ���ճ��÷��������뿴 [UPGRADE.md](./UPGRADE.md)��

---

## 1. ����

```
app����װ��Hilt / Demo��
  ���� user��ҵ�񣩩���? userapi��UserService / UserInfo / Router��
  ��              ����? base��Gateway�����á�BaseApp* ����  �� case �������ǿ��
  ��                      ��
  ��                      �� ʵ�ְ��� app������ media / mqtt��
  ���� mqttcomponent �������������� MessageGateway
  ���� core-media ������������������ MediaGateway
  ���� core-form / core-gnss / core-ws / ��
  ���� common��facade������ api ����? core-network / core-db / core-ui
                                      ��
                                      ��
                               core-base �� core-utils / core-log / core-autosize
```

**ҵ����Ƽ���ڣ�**

```gradle
implementation project(':common')   // �� Maven: common:4.7.0��������ʽ�� network 1.7.0��db 1.6.1��base 1.6.0 �ȣ��� UPGRADE ���ף�
implementation project(':base')     // Gateway / AppPropertiesConfig / BaseAppActivity ��
implementation project(':userapi')  // ����Ҫ�û���Լʱ
```

`common` �Ǽ������棺����������ҵ����룬ͨ�� `api` �� `core-*` ͸����ȥ�����ھɹ��̼���ֻ����һ�����ꡣ

> **����ע�⣺** Gradle ģ�� `:base`��case���� Maven `io.coderf.arklab.core:base`����� `:core-base`����

---

## 2. ���ķֲ�ģ��

### `common`��facade��

| �� | ˵�� |
|----|------|
| ְ�� | �����ۺϣ�facade��ͳһ��ڣ� |
| ���� / namespace | `io.coderf.arklab.common.facade` |
| ��ʱ�� | ���� / ҵ��ģ��ͳһ��������ڼ��� |
| ע�� | Manifest ��Ҫ������������������Ȩ���� Activity �� `core-base`��`InitProvider` �� `core-autosize` |

### `core-base`

| �� | ˵�� |
|----|------|
| ְ�� | `BaseActivity` / `BaseFragment` / `BaseViewModel`����ʷ widget��helper��**ȫ�� common ��Դ�� DataBinding** |
| namespace | `io.coderf.arklab.common`�������� R / ������ҵ�� import �������øģ� |
| ��ʱ�� | һ��ͨ�� `common` �����������Ҫ��ҵ�����ٲ�ڶ���ͬ�� R |
| ��״ | ����Դ�widget / �ع��߶���Ǩ����δ��ɡ�**4.6.0** ������Ϊ Material3��`ActionToolbar` / `TitleBar` Ϊ `MaterialToolbar`�� |

### `core-network`

| �� | ˵�� |
|----|------|
| ְ�� | Retrofit / OkHttp ��װ���� `RepositoryImpl*`���� `DefaultNetworkRepository` |
| ���� | `api` �� `core-base`��**������** `core-db`�������� Room �ֲ������ |
| �� API | `RequestOptions` / `RequestResult` / `AppError` / `RequestUi` |
| �� API | `RepositoryImpl`��Rx���Ա��������ɲ� `FlowRepositoryImpl` **��ɾ��**��Э������ `DefaultNetworkRepository` |

**������ʾ����Kotlin����**

```kotlin
class XxxRepository(
    private val api: XxxApi,
    requestUi: RequestUi = NoOpRequestUi
) : DefaultNetworkRepository(requestUi) {

    fun load(): Flow<RequestResult<Xxx>> = request(
        RequestOptions(showLoading = true)
    ) { api.getXxx() }
}
```

Demo �ο���`app/.../SampleCoreNetworkRepository.kt`��

### `core-db`

| �� | ˵�� |
|----|------|
| ְ�� | Room DAO / Database / Entity / `RoomRepository*` |
| ��ʱ�� | ���س־û����� `common` ��ֱ�� `api project(':core-db')` |

### `core-ui`

| �� | ˵�� |
|----|------|
| ְ�� | Activity ί�У�`ToolbarDelegate`��`InitDataPolicy` �� |
| ��״ | �������٣��ؼ����� `core-base`���� R/DataBinding ���ƣ� |
| �÷� | `BaseActivity` �ѽ��� `InitDataPolicy`��Ĭ�� `AlwaysInitData`�� |

### `core-utils`

| �� | ˵�� |
|----|------|
| ְ�� | �� Android R ����������������ͨ����չ��`CoreLogger`������/��ֵ/�ܶ�/View �ȣ� |
| ���� | `io.coderf.arklab.core.utils`����չͳһ�� `io.coderf.arklab.core.utils.ext` |
| ��״ | �Ѵ� `core-base` �³�ԭ `Extensions.kt`��`DrawableUtil` �ȴ� R / �� UI �������� `core-base` |
| �´��� | `import io.coderf.arklab.core.utils.ext.*`���������� `common.utils.common` �µľ���չ�� |
| ���� | `core-base` �� `Extensions.kt` �Ա���ͬǩ�� `@Deprecated` ת������ import �ɱ��� |

#### util ˵�����ݻ�����Ǩ�� core-utils��

���⹤��Ǩ `core-utils` �ݲ����㣨`annotation` Ϊ�� Java��`core-utils` Ϊ Android Library��������ڸİ��ɱ�����  
��ǰԼ����

- �� Kotlin ��չ��ѡ�� `core-utils` `ext.*`����ʷ Java ���߶������� `core-base`
- `DensityUtil.setDensity` �ѷ���Ϊ�ղ�������й©������Ļ������ AutoSize
- `RegexUtils`��ҵ��ͨ��У���� `core-utils`��`io.coderf.arklab.core.utils.RegexUtils`����`annotation` �����б���У��ͬԴ������˫�������Ժ���˵

### `core-autosize`

| �� | ˵�� |
|----|------|
| ְ�� | ����ͷ���ܶ����䣨AndroidAutoSize����`AutoSize` / `AutoSizeConfig` / `InitProvider` |
| Maven | `io.coderf.arklab.core:autosize:1.0.0` |
| namespace | `io.coderf.arklab.core.autosize` |
| Java �� | `io.coderf.arklab.core.autosize`���� namespace / Maven ���룻�Ӱ� `strategy` / `lifecycle` / `external` / `unit` / `util`�� |
| ��ʱ�� | һ�㾭 `core-base` / `common` �����������������ʱ����ʽ������ģ�� |
| ���� | `Config.setAutoSizeEnabled`������ `Config.init` ǰ�������� meta��`design_width_in_dp` / `design_height_in_dp` |

---

## 3. ҵ��������ģ��

| ģ�� | ְ�� | ҵ��Ӧ������� |
|------|------|----------------|
| `base` | **Demo/case ����**���ǿ�ܣ���`MediaGateway` / `MessageGateway`��`AppPropertiesConfig`��`BaseAppActivity`��ͨ��ö�ٵ� | `api`/`implementation project(':base')` |
| `userapi` | �û�ģ�������Լ��`UserService` / `UserInfo` / Router�� | `implementation project(':userapi')` |
| `user` | �û�ҵ�� UI / �߼� | ���� `common` + `base` + `userapi`��**��ֹ**ֱ������ mqtt / media |
| `mqttcomponent` | MQTT ʵ�֣�`MqttClient` �ȣ� | �� app ������Gateway ����д�� app |
| `core-media` | ����/ѡͼ/ѹ����`MediaHelper` �ȣ� | �� app ������Gateway ����д�� app |
| `core-form` | ������ͨ�� UI ��� | ���裻`FormHost.validateAll` / `formStyle` �� core-form README |
| `core-gnss` | ��λ���� | ���裻���·��߽� |

#### core-gnss �����߽�

| ���� | ���� |
|------|------|
| LocationManager��GPS/NETWORK/��ѡϵͳ Fused��+ Ȩ�� + GPS ���ؼ�� | **Ĭ�Ϻ���**����������ֻ����Щ��ϵͳ Fused / getCurrentLocation ��Ϊ��ѡ���� |
| Session �켣���� / GPX��CSV | ��ѡ��`GpsSettingConfig.maxLocationHistorySize` Ĭ�� **1000**��`0`=�����ƣ� |
| ʵʱλ���ϱ� | **���ڱ���**���������н� `core-ws` / MQTT / HTTP��ԭ Netty `socket` ����ɾ���� |
| ���̼� `getInstance()` ���� | ע���Ự���ţ�ֹͣ��λʱ���� Session `clear` |

#### core-media �����߽�

| ���� | ���� |
|------|------|
| `MediaHelper` ѡͼ/����/Ԥ�� + app �� `MediaGateway` | **����**���������·���� |
| ���� `ImgCompressor` / `VideoCompress` / Crop | �����滻������� `core-media-compress`���� Demo ʹ������������ |
| ƽ�� `UIController` / `DebugUtil` / `LogUtil` | �ƻ�ɾ���������� core |
| `core-form` �� `implementation core-media` | ��֪��ϣ�����ý������Ҫʱ����������Ӧ��Ϊ Gateway/��ѡ���� |
| `core-ws` | WebSocket��OkHttp������ core-mqtt �ֲ㣩 | ���裻ҵ���ŷ���д���� |
| `annotation` / `room-processor` | У��ע���� Room ������ | ���� |
| `app` | Demo ��װ��Hilt Module��Gateway ������� | �������� |

---

## 4. Gateway �÷���������

ҵ��ģ��**ֻ�����ӿ�**��ʵ���� app ע�롣

### MessageGateway��MQTT��

```kotlin
@Inject lateinit var messageGateway: MessageGateway

messageGateway.connect()
messageGateway.subscribe("topic/x") { topic, payload -> /* �� */ }
```

��ʾ���� `app/.../di/GatewayModule.kt`��`app/.../mqtt/MqttMessageGateway`��

### MediaGateway��ý�壩

```java
@Inject MediaGateway mediaGateway;

mediaGateway.pickImages(1, uris -> { /* �ϴ�ͷ��� */ });
```

- �ӿڣ�`base/.../MediaGateway`��case���ǿ�ܣ�
- ������󶨣�`app/.../media/MediaHelperGateway`��`app/.../di/MediaGatewayModule`
- �ο���`MeFragment`��`FeedBackActivity`

**��ֹ��** �� `user` ��ҵ�� module �� `build.gradle` ��д `mqttcomponent` / `core-media`��  
**��ֹ��** `core-media` / `mqttcomponent` / `core-*` ���� `:base` / `userapi`��

�ֵ���ö�٣���ҵ�� `DicTypeEnum`������Ŀ���ڸ��Ե� case `:base`��Demo ������ҵ���ֵ��롣

---

## 5. Maven ��������

�����軷������ `ALIYUN_USER_NAME` / `ALIYUN_PASSWORD`�����磺

```bash
./gradlew :common:publish
./gradlew :core-base:publish :core-network:publish :core-db:publish :core-ui:publish :core-utils:publish :core-log:publish :core-autosize:publish
```

| ģ�� | ���� |
|------|------|
| common | `io.coderf.arklab.common:common:4.7.0` |
| core-base | `io.coderf.arklab.core:base:1.6.0` |
| core-network | `io.coderf.arklab.core:network:1.7.0`���� [core-network/README.md](./core-network/README.md)�� |
| core-db | `io.coderf.arklab.core:db:1.6.1`���� [core-db/README.md](./core-db/README.md)�� |
| core-ui | `io.coderf.arklab.core:ui:1.3.1` |
| core-utils | `io.coderf.arklab.core:utils:1.1.1` |
| core-log | `io.coderf.arklab.core:log:1.2.0` |
| core-autosize | `io.coderf.arklab.core:autosize:1.0.0` |
| room-processor | `io.coderf.arklab.room:room-processor:1.2.0`���� [room-processor/README.md](./room-processor/README.md)�� |
| core-mqtt | `io.coderf.arklab.mqtt:mqtt:1.6.2` |
| core-ws | `io.coderf.arklab.ws:ws:1.0.1` |
| core-media | `io.coderf.arklab.media:media:3.7.0` |
| core-form | `io.coderf.arklab.ui:ui:3.7.4`��`api` �� core-base / core-network�������� media�� |
| core-gnss | `io.coderf.arklab.gnss:core-gnss:4.0.0` |
| annotation | `io.coderf.arklab.annotation:annotation:3.3.1` |

������ֻ�� `common`����ͨ�� POM / `api` �������϶�Ӧ `core-*`����ʵ�ʷ��� POM Ϊ׼����Maven �����밴 [UPGRADE.md ����](./UPGRADE.md) **��ʽ���汾**�����⴫�������������� POM��

---

## 6. �´���Լ��

1. ҵ��ֱ������ mqtt / media ʵ��ģ�飬ֻ���� Gateway��
2. ������������� `DefaultNetworkRepository.request`�������� `sendRequest` ���ء�
3. Repository �ڲ�Ҫ����ҳ�棻Loading / ����ֻ�� `getRequestUi()` / `RequestUi`��`showError(AppError)`����
4. `core-*` ��ֹ���� `user` / `app`��
5. ��Ҫ�� `APP_SECRET` ���ķŽ� HTTP Header��������ǩ������
6. ����ʹ�� `AppBaseTheme`��Material3 DayNight����Ʒ��ɫ���ϵ����� `onPrimary`����Ҫд����ɫ��
7. `Config.setDynamicColorEnabled` Ĭ�Ϲأ��������� `Config.init()` ֮ǰ����Ļ���䡢��������ҹ��ģʽ��Edge-to-Edge��Ĭ��ռλͼͬ���� `Config`�������� `init()` ǰ���á�
