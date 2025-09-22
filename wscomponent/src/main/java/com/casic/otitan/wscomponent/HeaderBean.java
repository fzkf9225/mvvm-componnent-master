package com.casic.otitan.wscomponent;

/**
 * Created by fz on 2023/5/5 10:05
 * describe :
 */
public class HeaderBean {
    /**
     * 流程id，可以为空
     */
    private String flowId;
    /**
     * 租户id
     */
    private String tenantId;
    /**
     * 用户id
     */
    private String userId;
    private String msgId;

    public HeaderBean() {
        this.flowId = java.util.UUID.randomUUID().toString();
        this.tenantId = "000000";
        this.msgId = MsgIdEnum.DRONE_DEVICE_WS.getValue();
    }

    public HeaderBean(String flowId, String tenantId, String userId, String msgId) {
        this.flowId = flowId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.msgId = msgId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
