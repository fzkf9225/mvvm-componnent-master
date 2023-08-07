package com.casic.titan.usercomponent.bean;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.BaseObservable;

public class UserInfo extends BaseObservable implements Parcelable {

	private String access_token;
	private String token_type;
	private String refresh_token;
	private Integer expires_in;
	private String scope;
	private String tenant_id;
	private String user_name;
	private String real_name;
	private String avatar;
	private String client_id;
	private String workspace_id;
	private String role_name;
	private String user_type;
	private String user_id;
	private String role_id;
	private String nick_name;
	private String oauth_id;

	public UserInfo() {
	}

	protected UserInfo(Parcel in) {
		access_token = in.readString();
		token_type = in.readString();
		refresh_token = in.readString();
		if (in.readByte() == 0) {
			expires_in = null;
		} else {
			expires_in = in.readInt();
		}
		scope = in.readString();
		tenant_id = in.readString();
		user_name = in.readString();
		real_name = in.readString();
		avatar = in.readString();
		client_id = in.readString();
		workspace_id = in.readString();
		role_name = in.readString();
		user_type = in.readString();
		user_id = in.readString();
		role_id = in.readString();
		nick_name = in.readString();
		oauth_id = in.readString();
	}

	public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
		@Override
		public UserInfo createFromParcel(Parcel in) {
			return new UserInfo(in);
		}

		@Override
		public UserInfo[] newArray(int size) {
			return new UserInfo[size];
		}
	};

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String accessToken) {
		this.access_token = accessToken;
	}

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}

	public Integer getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(Integer expires_in) {
		this.expires_in = expires_in;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getReal_name() {
		return real_name;
	}

	public void setReal_name(String real_name) {
		this.real_name = real_name;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getClient_id() {
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public String getWorkspace_id() {
		return workspace_id;
	}

	public void setWorkspace_id(String workspace_id) {
		this.workspace_id = workspace_id;
	}

	public String getRole_name() {
		return role_name;
	}

	public void setRole_name(String role_name) {
		this.role_name = role_name;
	}

	public String getUser_type() {
		return user_type;
	}

	public void setUser_type(String user_type) {
		this.user_type = user_type;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getRole_id() {
		return role_id;
	}

	public void setRole_id(String role_id) {
		this.role_id = role_id;
	}

	public String getNick_name() {
		return nick_name;
	}

	public void setNick_name(String nick_name) {
		this.nick_name = nick_name;
	}

	public String getOauth_id() {
		return oauth_id;
	}

	public void setOauth_id(String oauth_id) {
		this.oauth_id = oauth_id;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(access_token);
		dest.writeString(token_type);
		dest.writeString(refresh_token);
		if (expires_in == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(expires_in);
		}
		dest.writeString(scope);
		dest.writeString(tenant_id);
		dest.writeString(user_name);
		dest.writeString(real_name);
		dest.writeString(avatar);
		dest.writeString(client_id);
		dest.writeString(workspace_id);
		dest.writeString(role_name);
		dest.writeString(user_type);
		dest.writeString(user_id);
		dest.writeString(role_id);
		dest.writeString(nick_name);
		dest.writeString(oauth_id);
	}
}

