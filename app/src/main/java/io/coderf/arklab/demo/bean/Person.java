package io.coderf.arklab.demo.bean;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.TypeConverters;

import java.util.List;

import io.coderf.arklab.annotation.annotation.Valid;
import io.coderf.arklab.annotation.annotation.VerifyCrossField;
import io.coderf.arklab.annotation.annotation.VerifyEntity;
import io.coderf.arklab.annotation.annotation.VerifyField;
import io.coderf.arklab.annotation.annotation.VerifyParams;
import io.coderf.arklab.annotation.annotation.VerifySort;
import io.coderf.arklab.annotation.annotation.VerifyWhen;
import io.coderf.arklab.annotation.enums.ConditionOperator;
import io.coderf.arklab.annotation.enums.CrossFieldOperator;
import io.coderf.arklab.annotation.enums.VerifyType;
import io.coderf.arklab.annotation.inter.VerifyGroup;
import io.coderf.arklab.common.bean.BaseDaoBean;
import io.coderf.arklab.common.converter.RoomListStringConverter;
import io.coderf.arklab.demo.BR;

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

    /** 非 Room：FormSpinner 学历下拉演示 */
    @Ignore
    @VerifySort(16)
    @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class}, errorMsg = "请选择学历！")
    private String educationLevel;

    /** 非 Room：FormSwitch 订阅演示 */
    @Ignore
    private Boolean acceptNewsletter;

    /** 非 Room：FormCheckbox 协议演示 */
    @Ignore
    @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Default.class, errorMsg = "请勾选用户协议！")
    private Boolean agreeProtocol;

    /** 非 Room：FormRadio 联系方式偏好演示 */
    @Ignore
    private String contactPreference;

    /** 非 Room：FormStepper 步数演示 */
    @Ignore
    private int dailySteps;

    /** 非 Room：FormRating 评分演示 */
    @Ignore
    private float serviceRating;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "请选择生日！"),
    })
    @VerifySort(3)
    @ColumnInfo
    private String birthday;

    /** 非 Room 字段：VerifyActivity（Default 分组）演示条件/日期类校验 */
    @Ignore
    @VerifySort(4)
    @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Default.class, errorMsg = "请填写教育经历！")
    private String educationalExperienceDate;

    /** 非 Room 字段：跨字段校验开学时间不能早于生日 */
    @Ignore
    @VerifySort(5)
    @VerifyCrossField(refField = "birthday", operator = CrossFieldOperator.GREATER_THAN_OR_EQUAL,
            dateFormat = "yyyy-MM-dd", group = VerifyGroup.Default.class,
            errorMsg = "开学时间不能早于生日！")
    @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Default.class, errorMsg = "请选择开学时间！")
    private String schoolStartTime;

    /** 非 Room 字段：演示 TIME 类型校验 */
    @Ignore
    @VerifySort(6)
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Default.class, errorMsg = "请选择上课时间！"),
            @VerifyParams(type = VerifyType.TIME, group = VerifyGroup.Default.class, errorMsg = "上课时间格式不正确！")
    })
    private String classStartTime;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "请填写手机号码！"),
            @VerifyParams(type = VerifyType.MOBILE_PHONE,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "手机号码格式输入不正确！")
    })
    @VerifySort(7)
    @ColumnInfo
    private String mobile;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Default.class, errorMsg = "请填写固话号码！"),
            @VerifyParams(type = VerifyType.TEL_PHONE, group = VerifyGroup.Default.class, errorMsg = "固话号码格式输入不正确！"),
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Create.class, errorMsg = "女性用户请填写座机号码！",
                    when = @VerifyWhen(refField = "sex", operator = ConditionOperator.EQUALS, value = "女")),
            @VerifyParams(type = VerifyType.TEL_PHONE, group = VerifyGroup.Create.class, errorMsg = "座机号码格式输入不正确！",
                    when = @VerifyWhen(refField = "sex", operator = ConditionOperator.EQUALS, value = "女"))
    })
    @VerifySort(8)
    @ColumnInfo
    private String tel;

    @VerifySort(9)
    @VerifyParams(type = VerifyType.NUMBER_RANGE,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, minNumber = 0, maxNumber = 120, errorMsg = "您是神仙吗？")
    @ColumnInfo
    private String age;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "体重为空"),
            @VerifyParams(type = VerifyType.NUMBER_00,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "体重输入格式不正确"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, maxNumber = 200, errorMsg = "你该减肥了！！！"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, minNumber = 40, errorMsg = "你已经瘦成竹竿了！！！")
    })
    @VerifyCrossField(refField = "height", operator = CrossFieldOperator.LESS_THAN,
            group = VerifyGroup.Create.class, errorMsg = "体重数值应小于身高（Create 分组跨字段数值比较演示）")
    @VerifySort(10)
    @ColumnInfo
    private String weight;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "身高为空"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, maxNumber = 300, errorMsg = "姚明都没你高！！！"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, minNumber = 40, errorMsg = "建议您补补钙，多晒晒太阳！！！")

    })
    @VerifySort(11)
    @ColumnInfo
    private String height;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY,group = {VerifyGroup.Create.class}, errorMsg = "邮箱地址为空！"),
            @VerifyParams(type = VerifyType.EMAIL,group = {VerifyGroup.Default.class,VerifyGroup.Create.class}, errorMsg = "邮箱地址错误！")
    })
    @VerifySort(12)
    @ColumnInfo
    private String email;

    /** 非 Room 字段：VerifyTopActivity（Create 分组）演示 @VerifyWhen 条件校验 */
    @Ignore
    @VerifySort(13)
    @VerifyWhen(refField = "age", operator = ConditionOperator.GREATER_THAN_OR_EQUAL, value = "18",
            group = VerifyGroup.Create.class)
    @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Create.class, errorMsg = "成年人请填写紧急联系人！")
    private String emergencyContact;

    @VerifySort(14)
    @VerifyParams(type = VerifyType.NOT_EMPTY, errorMsg = "您填填写您的爱好！")
    @ColumnInfo
    @TypeConverters({RoomListStringConverter.class})
    private List<String> hobby;

    //    @VerifyFieldSort(11)
//    @VerifyParams(type = VerifyType.NOTNULL, notNull = true, errorMsg = "您选择您的本人照片！")
    @Ignore
    private List<Uri> imageList;

    @VerifySort(15)
    @Valid(notNull = true, group = VerifyGroup.Create.class, errorMsg = "请选择您的家庭信息！")
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
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
        notifyPropertyChanged(BR.birthday);
    }

    @Bindable
    public String getEducationalExperienceDate() {
        return educationalExperienceDate;
    }

    public void setEducationalExperienceDate(String educationalExperienceDate) {
        this.educationalExperienceDate = educationalExperienceDate;
        notifyPropertyChanged(BR.educationalExperienceDate);
    }

    @Bindable
    public String getSchoolStartTime() {
        return schoolStartTime;
    }

    public void setSchoolStartTime(String schoolStartTime) {
        this.schoolStartTime = schoolStartTime;
        notifyPropertyChanged(BR.schoolStartTime);
    }

    @Bindable
    public String getClassStartTime() {
        return classStartTime;
    }

    public void setClassStartTime(String classStartTime) {
        this.classStartTime = classStartTime;
        notifyPropertyChanged(BR.classStartTime);
    }

    @Bindable
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
        notifyPropertyChanged(BR.age);
    }

    @Bindable
    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
        notifyPropertyChanged(BR.weight);
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(BR.email);
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
        notifyPropertyChanged(BR.mobile);
    }

    @Bindable
    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
        notifyPropertyChanged(BR.tel);
    }

    @Bindable
    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
        notifyPropertyChanged(BR.height);
    }

    @Bindable
    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
        notifyPropertyChanged(BR.sex);
    }

    @Bindable
    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
        notifyPropertyChanged(BR.educationLevel);
    }

    @Bindable
    public Boolean getAcceptNewsletter() {
        return acceptNewsletter;
    }

    public void setAcceptNewsletter(Boolean acceptNewsletter) {
        this.acceptNewsletter = acceptNewsletter;
        notifyPropertyChanged(BR.acceptNewsletter);
    }

    @Bindable
    public Boolean getAgreeProtocol() {
        return agreeProtocol;
    }

    public void setAgreeProtocol(Boolean agreeProtocol) {
        this.agreeProtocol = agreeProtocol;
        notifyPropertyChanged(BR.agreeProtocol);
    }

    @Bindable
    public String getContactPreference() {
        return contactPreference;
    }

    public void setContactPreference(String contactPreference) {
        this.contactPreference = contactPreference;
        notifyPropertyChanged(BR.contactPreference);
    }

    @Bindable
    public int getDailySteps() {
        return dailySteps;
    }

    public void setDailySteps(int dailySteps) {
        this.dailySteps = dailySteps;
        notifyPropertyChanged(BR.dailySteps);
    }

    @Bindable
    public float getServiceRating() {
        return serviceRating;
    }

    public void setServiceRating(float serviceRating) {
        this.serviceRating = serviceRating;
        notifyPropertyChanged(BR.serviceRating);
    }

    @Bindable
    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
        notifyPropertyChanged(BR.emergencyContact);
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

    @NonNull
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
                ", emergencyContact='" + emergencyContact + '\'' +
                ", hobby=" + hobby +
                ", imageList=" + imageList +
                ", family=" + family +
                ", familyList=" + familyList +
                '}';
    }
}
