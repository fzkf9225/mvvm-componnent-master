package com.casic.titan.usercomponent.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fz on 2023/5/6 17:20
 * describe :无人机工作空间
 */
public class WorkSpaceBean implements Parcelable {

    private String id;
    private String workspaceId;
    private String workspaceName;
    private String workspaceDesc;
    private String depId;

    protected WorkSpaceBean(Parcel in) {
        id = in.readString();
        workspaceId = in.readString();
        workspaceName = in.readString();
        workspaceDesc = in.readString();
        depId = in.readString();
    }

    public static final Creator<WorkSpaceBean> CREATOR = new Creator<WorkSpaceBean>() {
        @Override
        public WorkSpaceBean createFromParcel(Parcel in) {
            return new WorkSpaceBean(in);
        }

        @Override
        public WorkSpaceBean[] newArray(int size) {
            return new WorkSpaceBean[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getWorkspaceDesc() {
        return workspaceDesc;
    }

    public void setWorkspaceDesc(String workspaceDesc) {
        this.workspaceDesc = workspaceDesc;
    }

    public String getDepId() {
        return depId;
    }

    public void setDepId(String depId) {
        this.depId = depId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(workspaceId);
        dest.writeString(workspaceName);
        dest.writeString(workspaceDesc);
        dest.writeString(depId);
    }
}
