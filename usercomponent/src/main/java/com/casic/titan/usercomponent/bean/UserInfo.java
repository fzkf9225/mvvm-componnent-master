package com.casic.titan.usercomponent.bean;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.BaseObservable;

public class UserInfo extends BaseObservable implements Parcelable {
	private String id;
	private String name;
	private String realName;
	private String avatar;
	private String email;
	private String phone;
	private String roleName;
	private int sex;

	public UserInfo() {
	}

	protected UserInfo(Parcel in) {
		id = in.readString();
		name = in.readString();
		realName = in.readString();
		avatar = in.readString();
		email = in.readString();
		phone = in.readString();
		roleName = in.readString();
		sex = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(realName);
		dest.writeString(avatar);
		dest.writeString(email);
		dest.writeString(phone);
		dest.writeString(roleName);
		dest.writeInt(sex);
	}

	@Override
	public int describeContents() {
		return 0;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}
}

