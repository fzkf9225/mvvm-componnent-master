package io.coderf.arklab.mqttcomponent.presence

/**
 * 设备在线（MQTT 心跳）管理接口。
 *
 * 定义「用户在线状态」在客户端侧的统一入口；具体实现放在宿主 App 模块，
 * 负责拉取 [PresenceConnectionInfo]、驱动 [PresenceMqttClient] 与 [HeartbeatScheduler]。
 *
 * ## 职责边界
 * - **本接口**：登录态下的长连接 + 应用层心跳 + LWT 离线感知
 * - **其它 MQTT 通道**（推送、轨迹等）：使用独立 [io.coderf.arklab.mqttcomponent.mqtt.MqttConnection] 实例
 *
 * ## 典型调用时机
 * | 场景                      | 方法                |
 * |---------------------------|---------------------|
 * | Application 启动          | [init] 一次；若已登录则 [start] |
 * | 登录成功                  | [start]             |
 * | 退出登录 / 跳转登录页     | [stop]              |
 * | Access Token 刷新成功     | [reconnectIfNeeded]（MQTT 密码为 JWT 时需重连） |
 */
interface DevicePresenceManager {

    /**
     * 注册应用级前后台生命周期监听。
     *
     * 内部通过 ProcessLifecycleOwner 感知整 App 进入前台/后台，
     * 用于切换「应用层心跳」与「仅协议层 keepAlive」策略。
     *
     * **必须在 [start] 之前调用一次**，建议在 Application.onCreate 中调用。
     * 重复调用应幂等。
     */
    fun init()

    /**
     * 启动设备在线会话：拉取 MQTT 连接信息并建立连接。
     *
     * 调用方应保证用户已登录。多次调用应为幂等：
     * 已启动且已连接时仅恢复前台心跳；未连接时重新拉配置并连接。
     */
    fun start()

    /**
     * 停止设备在线会话：取消进行中的配置拉取、停止心跳调度、断开 MQTT。
     *
     * 应在清除本地登录态**之前**调用，以便使用有效 Token 完成优雅断开。
     */
    fun stop()

    /**
     * 在会话仍活跃且用户仍登录时，重新拉取连接信息并重连 MQTT。
     *
     * 典型场景：HTTP Token 刷新后 MQTT JWT 密码失效，需重新获取连接信息再连接。
     * 若当前未 [start] 或已登出，实现类应直接返回。
     */
    fun reconnectIfNeeded()
}
