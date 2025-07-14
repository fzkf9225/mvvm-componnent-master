package pers.fz.mvvm.bean;

public class Gps {
    //纬度
    private Double longitude;
    //经度
    private Double latitude;

    public Gps(double longitude, double latitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
