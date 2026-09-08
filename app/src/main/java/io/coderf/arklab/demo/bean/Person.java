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
import io.coderf.arklab.annotation.annotation.VerifyArray;
import io.coderf.arklab.annotation.annotation.VerifyCrossField;
import io.coderf.arklab.annotation.annotation.VerifyCrossFields;
import io.coderf.arklab.annotation.annotation.VerifyEntity;
import io.coderf.arklab.annotation.annotation.VerifyField;
import io.coderf.arklab.annotation.annotation.VerifyParams;
import io.coderf.arklab.annotation.annotation.VerifySort;
import io.coderf.arklab.annotation.annotation.VerifyWhen;
import io.coderf.arklab.annotation.annotation.VerifyWhenAll;
import io.coderf.arklab.annotation.enums.ConditionOperator;
import io.coderf.arklab.annotation.enums.CrossFieldOperator;
import io.coderf.arklab.annotation.enums.VerifyType;
import io.coderf.arklab.annotation.inter.VerifyGroup;
import io.coderf.arklab.common.bean.BaseDaoBean;
import io.coderf.arklab.common.converter.RoomListStringConverter;
import io.coderf.arklab.demo.BR;

/**
 * Person 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/9/5 18:32
 */
@Entity
@VerifyEntity(sort = true)
public class Person extends BaseDaoBean {
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "姓名为空！"),
            @VerifyParams(type = VerifyType.LENGTH_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, minLength = 2, maxLength = 10, errorMsg = "姓名输入错误！"),
            @VerifyParams(type = VerifyType.EQUALS, group = {VerifyGroup.Default.class}, errorMsg = "您只能填张三！", equalStr = "张三")
    })
    @VerifySort(1)
    @ColumnInfo
    private String name;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "请选择性别！"),
            @VerifyParams(type = VerifyType.IN, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class},
                    values = {"男", "女", "未知"}, errorMsg = "性别不在可选范围内！")
    })
    @VerifySort(2)
    @ColumnInfo
    private String sex;

    /** 非 Room：FormSpinner 学历下拉演示 */
    @Ignore
    @VerifySort(16)
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "请选择学历！"),
            @VerifyParams(type = VerifyType.IN, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class},
                    values = {"本科", "硕士", "博士"}, errorMsg = "学历必须是本科/硕士/博士！")
    })
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
    @VerifyParams(type = VerifyType.IN, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class},
            values = {"电话", "邮件", "微信"}, errorMsg = "联系偏好不在可选范围内！")
    private String contactPreference;

    /** 非 Room：FormStepper 步数演示 */
    @Ignore
    private int dailySteps;

    /** 非 Room：FormRating 评分演示 */
    @Ignore
    private float serviceRating;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "请选择生日！"),
            @VerifyParams(type = VerifyType.DATE, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class},
                    dateFormat = "yyyy-MM-dd", errorMsg = "生日格式不正确！")
    })
    @VerifySort(3)
    @ColumnInfo
    private String birthday;

    /** 非 Room：Default 分组演示日期区间必填 */
    @Ignore
    @VerifySort(4)
    @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Default.class, errorMsg = "请填写教育经历！")
    private String educationalExperienceDate;

    /** 非 Room：跨字段日期比较 + DATETIME */
    @Ignore
    @VerifySort(5)
    @VerifyCrossFields({
            @VerifyCrossField(refField = "birthday", operator = CrossFieldOperator.GREATER_THAN_OR_EQUAL,
                    dateFormat = "yyyy-MM-dd", group = VerifyGroup.Default.class,
                    errorMsg = "开学时间不能早于生日！")
    })
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Default.class, errorMsg = "请选择开学时间！"),
            @VerifyParams(type = VerifyType.DATETIME, group = VerifyGroup.Default.class,
                    dateFormat = "yyyy-MM-dd HH:mm:ss", errorMsg = "开学时间格式不正确！")
    })
    private String schoolStartTime;

    /** 非 Room：TIME 类型 */
    @Ignore
    @VerifySort(6)
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Default.class, errorMsg = "请选择上课时间！"),
            @VerifyParams(type = VerifyType.TIME, group = VerifyGroup.Default.class, errorMsg = "上课时间格式不正确！")
    })
    private String classStartTime;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "请填写手机号码！"),
            @VerifyParams(type = VerifyType.MOBILE_PHONE, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "手机号码格式输入不正确！")
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
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "请填写年龄！"),
            @VerifyParams(type = VerifyType.NUMBER_INTEGER, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "年龄必须是整数！"),
            @VerifyParams(type = VerifyType.AGE, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "年龄须在 0-120 之间！")
    })
    @ColumnInfo
    private String age;

    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "体重为空"),
            @VerifyParams(type = VerifyType.NUMBER_00, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "体重输入格式不正确"),
            @VerifyParams(type = VerifyType.NUMBER_SCALE, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, scale = 2, errorMsg = "体重最多两位小数"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, maxNumber = 200, errorMsg = "你该减肥了！！！"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, minNumber = 40, errorMsg = "你已经瘦成竹竿了！！！")
    })
    @VerifyCrossField(refField = "height", operator = CrossFieldOperator.LESS_THAN,
            group = {VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "体重数值应小于身高（跨字段数值比较）")
    @VerifySort(10)
    @ColumnInfo
    private String weight;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOTNULL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "身高为空"),
            @VerifyParams(type = VerifyType.NUMBER, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "身高必须是数字"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, maxNumber = 300, errorMsg = "姚明都没你高！！！"),
            @VerifyParams(type = VerifyType.NUMBER_RANGE_EQUAL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, minNumber = 40, errorMsg = "建议您补补钙，多晒晒太阳！！！")
    })
    @VerifySort(11)
    @ColumnInfo
    private String height;
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "邮箱地址为空！"),
            @VerifyParams(type = VerifyType.EMAIL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "邮箱地址错误！")
    })
    @VerifySort(12)
    @ColumnInfo
    private String email;

    /** 非 Room：字段级 @VerifyWhen，年龄≥18 时紧急联系人必填 */
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

    /** 非 Room：身份证 ID_CARD */
    @Ignore
    @VerifySort(17)
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Editor.class, errorMsg = "请填写身份证号！"),
            @VerifyParams(type = VerifyType.ID_CARD, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "身份证号格式不正确！")
    })
    private String idCard;

    /** 非 Room：URL */
    @Ignore
    @VerifySort(18)
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Editor.class, errorMsg = "请填写个人主页！"),
            @VerifyParams(type = VerifyType.URL, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "个人主页不是合法 URL！")
    })
    private String homepage;

    /** 非 Room：邮政编码 */
    @Ignore
    @VerifySort(19)
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Editor.class, errorMsg = "请填写邮政编码！"),
            @VerifyParams(type = VerifyType.POSTAL_CODE, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "邮政编码格式不正确！")
    })
    private String postalCode;

    /** 非 Room：NOT_EQUALS / NOT_IN / REGEX */
    @Ignore
    @VerifySort(20)
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class}, errorMsg = "请填写昵称！"),
            @VerifyParams(type = VerifyType.NOT_EQUALS, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class},
                    equalStr = "admin", errorMsg = "昵称不能是 admin！"),
            @VerifyParams(type = VerifyType.NOT_IN, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class},
                    values = {"root", "system"}, errorMsg = "昵称不能使用保留字！"),
            @VerifyParams(type = VerifyType.REGEX, group = {VerifyGroup.Default.class, VerifyGroup.Create.class, VerifyGroup.Editor.class},
                    regex = "^[\\u4e00-\\u9fa5A-Za-z0-9_]{2,16}$", errorMsg = "昵称仅支持 2-16 位中文、字母、数字或下划线！")
    })
    private String nickname;

    /** 非 Room：@VerifyWhenAll，订阅资讯的女性需填配偶姓名 */
    @Ignore
    @VerifySort(21)
    @VerifyWhenAll({
            @VerifyWhen(refField = "sex", operator = ConditionOperator.EQUALS, value = "女",
                    group = VerifyGroup.Create.class),
            @VerifyWhen(refField = "acceptNewsletter", operator = ConditionOperator.EQUALS, value = "true",
                    group = VerifyGroup.Create.class)
    })
    @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Create.class, errorMsg = "订阅资讯的女性请填写配偶姓名！")
    private String spouseName;

    /** 非 Room：Editor 分组 LENGTH_RANGE */
    @Ignore
    @VerifySort(22)
    @VerifyField({
            @VerifyParams(type = VerifyType.NOT_EMPTY, group = VerifyGroup.Editor.class, errorMsg = "请填写备注！"),
            @VerifyParams(type = VerifyType.LENGTH_RANGE, group = VerifyGroup.Editor.class,
                    minLength = 4, maxLength = 80, errorMsg = "备注长度需大于 4 且小于 80！")
    })
    private String remark;

    @Ignore
    private List<Uri> imageList;

    @VerifySort(15)
    @Valid(notNull = true, group = VerifyGroup.Create.class, errorMsg = "请填写家庭信息！")
    @Ignore
    public Family family;

    @VerifySort(23)
    @VerifyArray({
            @Valid(notNull = true, notEmpty = true, group = VerifyGroup.Editor.class, errorMsg = "请至少添加一个家庭成员！")
    })
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

    @Bindable
    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
        notifyPropertyChanged(BR.idCard);
    }

    @Bindable
    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
        notifyPropertyChanged(BR.homepage);
    }

    @Bindable
    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        notifyPropertyChanged(BR.postalCode);
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        notifyPropertyChanged(BR.nickname);
    }

    @Bindable
    public String getSpouseName() {
        return spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
        notifyPropertyChanged(BR.spouseName);
    }

    @Bindable
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
        notifyPropertyChanged(BR.remark);
    }

    @Bindable
    public String getWife() {
        return family == null ? null : family.getWife();
    }

    public void setWife(String wife) {
        if (family == null) {
            family = new Family();
        }
        family.setWife(wife);
        notifyPropertyChanged(BR.wife);
    }

    @Bindable
    public String getHusband() {
        return family == null ? null : family.getHusband();
    }

    public void setHusband(String husband) {
        if (family == null) {
            family = new Family();
        }
        family.setHusband(husband);
        notifyPropertyChanged(BR.husband);
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
                ", idCard='" + idCard + '\'' +
                ", homepage='" + homepage + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", nickname='" + nickname + '\'' +
                ", spouseName='" + spouseName + '\'' +
                ", remark='" + remark + '\'' +
                ", hobby=" + hobby +
                ", imageList=" + imageList +
                ", family=" + family +
                ", familyList=" + familyList +
                '}';
    }
}
