package pers.fz.mvvm.wight.picdialog.bean;

import java.io.Serializable;

public class ImageBean implements Serializable {

	public String parentName;
	public long size;
	public String displayName;
	public String path;
	public boolean isChecked;
    public String imageUrl;
	public ImageBean() {
		super();
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ImageBean(String path) {
		super();
		this.path = path;
	}

	public ImageBean(String parentName, long size, String displayName,
                     String path, boolean isChecked) {
		super();
		this.parentName = parentName;
		this.size = size;
		this.displayName = displayName;
		this.path = path;
		this.isChecked = isChecked;
	}

	@Override
	public String toString() {
		return "ImageBean [parentName=" + parentName + ", size=" + size
				+ ", displayName=" + displayName + ", path=" + path
				+ ", isChecked=" + isChecked + "]";
	}

}
