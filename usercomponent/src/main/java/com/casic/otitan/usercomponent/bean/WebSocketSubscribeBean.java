package com.casic.otitan.usercomponent.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by fz on 2023/5/10 8:42
 * describe :WebSocket订阅信息
 */
public class WebSocketSubscribeBean implements Parcelable {
    @SerializedName("device_sn")
    private String deviceSn;
    @SerializedName("device_name")
    private String deviceName;
    @SerializedName("device_type_id")
    private Integer deviceTypeId;
    @SerializedName("tenant_id")
    private String tenantId;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("user_name")
    private String userName;
    @SerializedName("nickname")
    private String nickname;
    @SerializedName("workspace_id")
    private String workspaceId;
    @SerializedName("workspace_name")
    private String workspaceName;
    @SerializedName("domain")
    private Integer domain;
    @SerializedName("device_type")
    private Integer deviceType;
    @SerializedName("sub_type")
    private Integer subType;
    @SerializedName("firmware_version")
    private String firmwareVersion;
    @SerializedName("compatible_status")
    private Boolean compatibleStatus;
    @SerializedName("version")
    private String version;
    @SerializedName("device_index")
    private String deviceIndex;
    @SerializedName("child_sn")
    private String childSn;
    @SerializedName("bound_time")
    private String boundTime;
    @SerializedName("bound_status")
    private Integer boundStatus;
    @SerializedName("login_time")
    private String loginTime;
    @SerializedName("last_online_time")
    private String lastOnlineTime;
    @SerializedName("device_desc")
    private String deviceDesc;
    @SerializedName("device_status")
    private Integer deviceStatus;
    @SerializedName("register_num")
    private String registerNum;
    @SerializedName("firmware_status")
    private Integer firmwareStatus;
    @SerializedName("firmware_progress")
    private Integer firmwareProgress;
    @SerializedName("status")
    private Boolean status;
    @SerializedName("payloads_list")
    private List<?> payloadsList;
    @SerializedName("gb_agent_id")
    private String gbAgentId;
    @SerializedName("gb_channel_id")
    private String gbChannelId;
    @SerializedName("rtmp_id")
    private String rtmpId;
    @SerializedName("video_on_live_status")
    private Boolean videoOnLiveStatus;
    @SerializedName("selected_stream_type")
    private Integer selectedStreamType;
    @SerializedName("phone")
    private String phone;
    @SerializedName("dep_id")
    private String depId;
    @SerializedName("children")
    private WebSocketSubscribeBean children;

    protected WebSocketSubscribeBean(Parcel in) {
        deviceSn = in.readString();
        deviceName = in.readString();
        if (in.readByte() == 0) {
            deviceTypeId = null;
        } else {
            deviceTypeId = in.readInt();
        }
        tenantId = in.readString();
        userId = in.readString();
        userName = in.readString();
        nickname = in.readString();
        workspaceId = in.readString();
        workspaceName = in.readString();
        if (in.readByte() == 0) {
            domain = null;
        } else {
            domain = in.readInt();
        }
        if (in.readByte() == 0) {
            deviceType = null;
        } else {
            deviceType = in.readInt();
        }
        if (in.readByte() == 0) {
            subType = null;
        } else {
            subType = in.readInt();
        }
        firmwareVersion = in.readString();
        byte tmpCompatibleStatus = in.readByte();
        compatibleStatus = tmpCompatibleStatus == 0 ? null : tmpCompatibleStatus == 1;
        version = in.readString();
        deviceIndex = in.readString();
        childSn = in.readString();
        boundTime = in.readString();
        if (in.readByte() == 0) {
            boundStatus = null;
        } else {
            boundStatus = in.readInt();
        }
        loginTime = in.readString();
        lastOnlineTime = in.readString();
        deviceDesc = in.readString();
        if (in.readByte() == 0) {
            deviceStatus = null;
        } else {
            deviceStatus = in.readInt();
        }
        registerNum = in.readString();
        if (in.readByte() == 0) {
            firmwareStatus = null;
        } else {
            firmwareStatus = in.readInt();
        }
        if (in.readByte() == 0) {
            firmwareProgress = null;
        } else {
            firmwareProgress = in.readInt();
        }
        byte tmpStatus = in.readByte();
        status = tmpStatus == 0 ? null : tmpStatus == 1;
        gbAgentId = in.readString();
        gbChannelId = in.readString();
        rtmpId = in.readString();
        byte tmpVideoOnLiveStatus = in.readByte();
        videoOnLiveStatus = tmpVideoOnLiveStatus == 0 ? null : tmpVideoOnLiveStatus == 1;
        if (in.readByte() == 0) {
            selectedStreamType = null;
        } else {
            selectedStreamType = in.readInt();
        }
        phone = in.readString();
        depId = in.readString();
        children = in.readParcelable(WebSocketSubscribeBean.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceSn);
        dest.writeString(deviceName);
        if (deviceTypeId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(deviceTypeId);
        }
        dest.writeString(tenantId);
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(nickname);
        dest.writeString(workspaceId);
        dest.writeString(workspaceName);
        if (domain == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(domain);
        }
        if (deviceType == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(deviceType);
        }
        if (subType == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(subType);
        }
        dest.writeString(firmwareVersion);
        dest.writeByte((byte) (compatibleStatus == null ? 0 : compatibleStatus ? 1 : 2));
        dest.writeString(version);
        dest.writeString(deviceIndex);
        dest.writeString(childSn);
        dest.writeString(boundTime);
        if (boundStatus == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(boundStatus);
        }
        dest.writeString(loginTime);
        dest.writeString(lastOnlineTime);
        dest.writeString(deviceDesc);
        if (deviceStatus == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(deviceStatus);
        }
        dest.writeString(registerNum);
        if (firmwareStatus == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(firmwareStatus);
        }
        if (firmwareProgress == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(firmwareProgress);
        }
        dest.writeByte((byte) (status == null ? 0 : status ? 1 : 2));
        dest.writeString(gbAgentId);
        dest.writeString(gbChannelId);
        dest.writeString(rtmpId);
        dest.writeByte((byte) (videoOnLiveStatus == null ? 0 : videoOnLiveStatus ? 1 : 2));
        if (selectedStreamType == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(selectedStreamType);
        }
        dest.writeString(phone);
        dest.writeString(depId);
        dest.writeParcelable(children, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WebSocketSubscribeBean> CREATOR = new Creator<WebSocketSubscribeBean>() {
        @Override
        public WebSocketSubscribeBean createFromParcel(Parcel in) {
            return new WebSocketSubscribeBean(in);
        }

        @Override
        public WebSocketSubscribeBean[] newArray(int size) {
            return new WebSocketSubscribeBean[size];
        }
    };

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Integer getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(Integer deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public Integer getDomain() {
        return domain;
    }

    public void setDomain(Integer domain) {
        this.domain = domain;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Boolean getCompatibleStatus() {
        return compatibleStatus;
    }

    public void setCompatibleStatus(Boolean compatibleStatus) {
        this.compatibleStatus = compatibleStatus;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDeviceIndex() {
        return deviceIndex;
    }

    public void setDeviceIndex(String deviceIndex) {
        this.deviceIndex = deviceIndex;
    }

    public String getChildSn() {
        return childSn;
    }

    public void setChildSn(String childSn) {
        this.childSn = childSn;
    }

    public String getBoundTime() {
        return boundTime;
    }

    public void setBoundTime(String boundTime) {
        this.boundTime = boundTime;
    }

    public Integer getBoundStatus() {
        return boundStatus;
    }

    public void setBoundStatus(Integer boundStatus) {
        this.boundStatus = boundStatus;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(String lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
    }

    public String getDeviceDesc() {
        return deviceDesc;
    }

    public void setDeviceDesc(String deviceDesc) {
        this.deviceDesc = deviceDesc;
    }

    public Integer getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(Integer deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getRegisterNum() {
        return registerNum;
    }

    public void setRegisterNum(String registerNum) {
        this.registerNum = registerNum;
    }

    public Integer getFirmwareStatus() {
        return firmwareStatus;
    }

    public void setFirmwareStatus(Integer firmwareStatus) {
        this.firmwareStatus = firmwareStatus;
    }

    public Integer getFirmwareProgress() {
        return firmwareProgress;
    }

    public void setFirmwareProgress(Integer firmwareProgress) {
        this.firmwareProgress = firmwareProgress;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public List<?> getPayloadsList() {
        return payloadsList;
    }

    public void setPayloadsList(List<?> payloadsList) {
        this.payloadsList = payloadsList;
    }

    public String getGbAgentId() {
        return gbAgentId;
    }

    public void setGbAgentId(String gbAgentId) {
        this.gbAgentId = gbAgentId;
    }

    public String getGbChannelId() {
        return gbChannelId;
    }

    public void setGbChannelId(String gbChannelId) {
        this.gbChannelId = gbChannelId;
    }

    public String getRtmpId() {
        return rtmpId;
    }

    public void setRtmpId(String rtmpId) {
        this.rtmpId = rtmpId;
    }

    public Boolean getVideoOnLiveStatus() {
        return videoOnLiveStatus;
    }

    public void setVideoOnLiveStatus(Boolean videoOnLiveStatus) {
        this.videoOnLiveStatus = videoOnLiveStatus;
    }

    public Integer getSelectedStreamType() {
        return selectedStreamType;
    }

    public void setSelectedStreamType(Integer selectedStreamType) {
        this.selectedStreamType = selectedStreamType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepId() {
        return depId;
    }

    public void setDepId(String depId) {
        this.depId = depId;
    }

    public WebSocketSubscribeBean getChildren() {
        return children;
    }

    public void setChildren(WebSocketSubscribeBean children) {
        this.children = children;
    }
}
