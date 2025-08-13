package com.casic.titan.demo.bean;

import android.net.Uri;

import androidx.databinding.Bindable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.TypeConverters;

import java.util.List;

import pers.fz.annotation.annotation.VerifyEntity;
import pers.fz.annotation.annotation.VerifyField;
import pers.fz.annotation.annotation.VerifySort;
import pers.fz.annotation.annotation.VerifyParams;
import pers.fz.annotation.enums.VerifyType;
import pers.fz.annotation.inter.VerifyGroup;
import pers.fz.mvvm.bean.BaseDaoBean;
import pers.fz.mvvm.converter.RoomListStringConverter;

/**
 * Created by fz on 2023/9/5 18:32
 * describe :
 */
@Entity
@VerifyEntity(sort = true)
public class Person extends BaseDaoBean {
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "姓名为空！"),
            @VerifyParams(type = VerifyType.LENGTH_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, minLength = 2, maxLength = 10, errorMsg = "姓名输入错误！"),
            @VerifyParams(type = VerifyType.EQUALS, group = {VerifyGroup.Default.class}, errorMsg = "您只能填张三！", equalStr = "张三")
    })
    @VerifySort(1)
    @ColumnInfo
    private String name;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "请选择性别！"),
    })
    @VerifySort(2)
    @ColumnInfo
    private String sex;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "请选择生日！"),
    })
    @VerifySort(3)
    @ColumnInfo
    private String birthday;

    @Ignore
    private String educationalExperienceDate;

    @Ignore
    private String schoolStartTime;

    @Ignore
    private String classStartTime;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "请填写手机号码！"),
            @VerifyParams(type = VerifyType.MOBILE_PHONE,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "手机号码格式输入不正确！")
    })
    @VerifySort(4)
    @ColumnInfo
    private String mobile;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "请填写固话号码！"),
            @VerifyParams(type = VerifyType.TEL_PHONE,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "固话号码格式输入不正确！")
    })
    @VerifySort(5)
    @ColumnInfo
    private String tel;

    @VerifySort(6)
    @VerifyParams(type = VerifyType.NUMBER_RANGE,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, minNumber = 0, maxNumber = 120, errorMsg = "您是神仙吗？")
    @ColumnInfo
    private String age;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "体重为空"),
            @VerifyParams(type = VerifyType.NUMBER_00,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "体重输入格式不正确"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, maxNumber = 200, errorMsg = "你该减肥了！！！"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, minNumber = 40, errorMsg = "你已经瘦成竹竿了！！！")
    })
    @VerifySort(7)
    @ColumnInfo
    private String weight;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "身高为空"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, maxNumber = 300, errorMsg = "姚明都没你高！！！"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, minNumber = 40, errorMsg = "建议您补补钙，多晒晒太阳！！！")

    })
    @VerifySort(8)
    @ColumnInfo
    private String height;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY,group = {VerifyGroup.Create.class}, errorMsg = "邮箱地址为空！"),
            @VerifyParams(type = VerifyType.EMAIL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "邮箱地址错误！")
    })
    @VerifySort(9)
    @ColumnInfo
    private String email;

    @VerifySort(10)
    @VerifyParams(type = VerifyType.NOT_EMPTY, errorMsg = "您填填写您的爱好！")
    @ColumnInfo
    @TypeConverters({RoomListStringConverter.class})
    private List<String> hobby;

    //    @VerifyFieldSort(11)
//    @VerifyParams(type = VerifyType.NOTNULL, notNull = true, errorMsg = "您选择您的本人照片！")
    @Ignore
    private List<Uri> imageList;

    //    @VerifyFieldSort(12)
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
    public Person(String name, String birthday, String mobile, String tel, String age, String weight, String height, String email, List<String> hobby) {
        this.name = name;
        this.birthday = birthday;
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
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
        notifyPropertyChanged(com.casic.titan.demo.BR.birthday);
    }

    @Bindable
    public String getEducationalExperienceDate() {
        return educationalExperienceDate;
    }

    public void setEducationalExperienceDate(String educationalExperienceDate) {
        this.educationalExperienceDate = educationalExperienceDate;
        notifyPropertyChanged(com.casic.titan.demo.BR.educationalExperienceDate);
    }

    @Bindable
    public String getSchoolStartTime() {
        return schoolStartTime;
    }

    public void setSchoolStartTime(String schoolStartTime) {
        this.schoolStartTime = schoolStartTime;
        notifyPropertyChanged(com.casic.titan.demo.BR.schoolStartTime);
    }

    @Bindable
    public String getClassStartTime() {
        return classStartTime;
    }

    public void setClassStartTime(String classStartTime) {
        this.classStartTime = classStartTime;
        notifyPropertyChanged(com.casic.titan.demo.BR.classStartTime);
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
