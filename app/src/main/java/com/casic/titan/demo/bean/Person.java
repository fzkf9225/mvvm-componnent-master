package com.casic.titan.demo.bean;

import android.net.Uri;

import androidx.databinding.Bindable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import pers.fz.annotation.verify.Valid;
import pers.fz.annotation.verify.VerifyEntity;
import pers.fz.annotation.verify.VerifyField;
import pers.fz.annotation.verify.VerifyFieldSort;
import pers.fz.annotation.verify.VerifyParams;
import pers.fz.annotation.verify.VerifyType;
import pers.fz.mvvm.bean.BaseDaoBean;
import pers.fz.mvvm.database.RoomConverter;

/**
 * Created by fz on 2023/9/5 18:32
 * describe :
 */

@Entity
@VerifyEntity(sort = true)
public class Person extends BaseDaoBean {
    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL, notEmpty = true, errorMsg = "姓名为空！"),
            @VerifyParams(type = VerifyType.LENGTH_RANGE_EQUAL, minLength = 2, maxLength = 10, errorMsg = "姓名输入错误！"),
//            @VerifyParams(type = VerifyType.EQUALS, errorMsg = "您只能填张三！", equalStr = "张三")
    })
    @VerifyFieldSort(1)
    @ColumnInfo
    private String name;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL, notEmpty = true, errorMsg = "请选择性别！"),
    })
    @VerifyFieldSort(2)
    @ColumnInfo
    private String sex;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL, errorMsg = "请填写手机号码！"),
            @VerifyParams(type = VerifyType.MOBILE_PHONE, errorMsg = "手机号码格式输入不正确！")
    })
    @VerifyFieldSort(3)
    @ColumnInfo
    private String mobile;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL, errorMsg = "请填写固话号码！"),
            @VerifyParams(type = VerifyType.TEL_PHONE, errorMsg = "固话号码格式输入不正确！")
    })
    @VerifyFieldSort(4)
    @ColumnInfo
    private String tel;

    @VerifyFieldSort(5)
    @VerifyParams(type = VerifyType.NUMBER_RANGE, minNumber = 0, maxNumber = 120, errorMsg = "您是神仙吗？")
    @ColumnInfo
    private String age;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL, notEmpty = true, errorMsg = "体重为空"),
            @VerifyParams(type = VerifyType.NUMBER_00, errorMsg = "体重输入格式不正确"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, maxNumber = 200, errorMsg = "你该减肥了！！！"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, minNumber = 40, errorMsg = "你已经瘦成竹竿了！！！")
    })
    @VerifyFieldSort(6)
    @ColumnInfo
    private String weight;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL, errorMsg = "身高为空"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, maxNumber = 300, errorMsg = "姚明都没你高！！！"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, minNumber = 40, errorMsg = "建议您补补钙，多晒晒太阳！！！")

    })
    @VerifyFieldSort(7)
    @ColumnInfo
    private String height;
    @VerifyField({
//            @VerifyParams(type = VerifyType.NOTNULL, errorMsg = "邮箱地址为空！"),
            @VerifyParams(type = VerifyType.EMAIL, notNull = false, errorMsg = "邮箱地址错误！")
    })
    @VerifyFieldSort(8)
    @ColumnInfo
    private String email;

    @VerifyFieldSort(8)
    @VerifyParams(type = VerifyType.NOTNULL, notNull = true, errorMsg = "您填填写您的爱好！")
    @ColumnInfo
    @TypeConverters({RoomConverter.class})
    private List<String> hobby;

//    @VerifyFieldSort(9)
//    @VerifyParams(type = VerifyType.NOTNULL, notNull = true, errorMsg = "您选择您的本人照片！")
    @Ignore
    private List<Uri> imageList;

//    @VerifyFieldSort(10)
//    @Valid(notNull = true, errorMsg = "请选择您的家庭信息！")
    @Ignore
    public Family family;

//    @VerifyFieldSort(13)
//    @Valid(notNull = true, errorMsg = "请选择您的家庭集合信息！")
    @ColumnInfo
    @Ignore
    public List<Family> familyList;


    public Person() {
    }

    @Ignore
    public Person(String name, String mobile, String tel, String age, String weight, String height, String email, List<String> hobby) {
        this.name = name;
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
        notifyPropertyChanged(com.casic.titan.demo.BR.name);
    }

    @Bindable
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
        notifyPropertyChanged(com.casic.titan.demo.BR.age);
    }

    @Bindable
    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
        notifyPropertyChanged(com.casic.titan.demo.BR.weight);
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(com.casic.titan.demo.BR.email);
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
        notifyPropertyChanged(com.casic.titan.demo.BR.mobile);
    }

    @Bindable
    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
        notifyPropertyChanged(com.casic.titan.demo.BR.tel);
    }

    @Bindable
    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
        notifyPropertyChanged(com.casic.titan.demo.BR.height);
    }

    @Bindable
    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
        notifyPropertyChanged(com.casic.titan.demo.BR.sex);
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
