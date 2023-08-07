package pers.fz.mvvm.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Created by fz on 2017/11/27.
 * 描述: 身份证工具类
 */

public class IDCardUtil {
    private static Properties props = new Properties();
    //当前目录下的IDCardAddressCode.properties
    private static String filePath = "IDCardAddressCode.properties";
    static {
        InputStream in = IDCardUtil.class.getResourceAsStream(filePath);
        try {
            props.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法名：parseGender
     * 详述：根据所传身份证号解析其性别
     * 开发人员：souvc
     * 创建时间：2015-9-7 下午1:55:44
     * @return 说明返回值含义
     * @throw 说明发生此异常的条件
     */
    public static String parseGender(String cid) {
        String gender = null;
        if(cid==null||cid.toCharArray().length<2||(cid.toCharArray().length!=15&&cid.toCharArray().length!=18))return "未知";
        char c = cid.charAt(cid.length()-2);
        int sex = Integer.parseInt(String.valueOf(c));
        if(sex%2==0){
            gender = "女士";
        }else{
            gender = "先生";
        }
        return gender;
    }
    /*
     * 校验规则：
     *1、将前面的身份证号码17位数分别乘以不同的系数。第i位对应的数为[2^(18-i)]mod11。从第一位到第十七位的系数分别为：7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2 ；
     *2、将这17位数字和系数相乘的结果相加；
     *3、用加出来和除以11，看余数是多少？；
     *4、余数只可能有0 1 2 3 4 5 6 7 8 9 10这11个数字。其分别对应的最后一位身份证的号码为1 0 X 9 8 7 6 5 4 3 2；
     * @return 返回false说明，身份证号码不符合规则，返回true说明身份证号码符合规则
     */


    /**
     * 方法名：checkCardId
     * 详述：检测身份证号是否符合规则
     * 开发人员：十年半山
     * 创建时间：2015-9-8
     * @param cid
     * @return 说明参数含义
     * @return boolean说明返回值含义
     */
    public static boolean checkCardId(String cid) {
        boolean flag = false;
        int len = cid.length();
        int kx = 0;
        //身份证号第一位到第十七位的系数装入到一个整型数组
        int Weight[]={7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2};
        //需要进行运算的是身份证前17位
        for(int i=0;i<len-1;i++) {
            //把身份证的数字分拆成一个个数字
            int x = Integer.parseInt(String.valueOf(cid.charAt(i)));
            //然后相加起来
            kx += Weight[i]*x;
        }
        //用加出来和模以11，看余数是多少？
        int mod = kx%11;
        //最后一位身份证的号码的对应号码,一一对应
        //(0,1,2,3,4,5,6,7,8,9,10)
        //(1,0,X,9,8,7,6,5,4,3,2)
        Character[] checkMods = {'1','0','X','9','8','7','6','5','4','3','2'};
        //获取身份证最后的一个验证码
        Character lastCode = cid.charAt(len-1);
        //判断是否对应
        String idNumber=lastCode.toString().toLowerCase();
        String checkMods2=checkMods[mod].toString().toLowerCase();
        if(checkMods2.equals(idNumber)){
            flag = true;
        }
        return flag;
    }

    /**
     * 方法名：parseAge
     * 详述：根据身份证号码，返回年龄
     * 开发人员：souvc
     * 创建时间：2015-9-7 下午1:56:15
     * @return 说明返回值含义
     * @throw 说明发生此异常的条件
     */
    public static int parseAge(String cid) {
        int age = 0;
        String birthDayStr = cid.substring(6,14);
        Date birthDay = null;
        try {
            birthDay = new SimpleDateFormat("yyyyMMdd").parse(birthDayStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        if(cal.before(birthDay)){
            throw new IllegalArgumentException("您还没有出生么？");
        }
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH)+1;
        int dayNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH)+1;
        int dayBirth = cal.get(Calendar.DAY_OF_MONTH);
        age = yearNow - yearBirth;
        if(monthNow<=monthBirth){
            if(monthNow==monthBirth&&dayNow<dayBirth){
                age--;
            }
        }else{
            age--;
        }
        return age;
    }

    /**
     * 方法名：parseAddress
     * 详述：通过身份证号码解析其出生所在地
     * 开发人员：souvc
     * 创建时间：2015-9-7 下午1:56:49
     * @return 说明返回值含义
     * @throw 说明发生此异常的条件
     */
    public static String parseAddress(String cid) {
        String address = null;
        String addressCode = cid.substring(0,6);
        try {
            address = new String(props.get(addressCode).toString().getBytes("iso-8859-1"),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return address;
    }

    /**
     * 方法名：parseBirthday
     * 详述：根据身份证号截取出生日期
     * 开发人员：liuhf
     * 创建时间：2015-9-7 下午2:08:20
     * @return 说明返回值含义
     * @throw 说明发生此异常的条件
     */
    public static String parseBirthday(String cid) {
        //通过身份证号来读取出生日期
        String birthday="";
        //如果没有身份证，那么不进行字符串截取工作。
        if(checkCardId(cid)){
            String year=cid.substring(6, 10);
            String month=cid.substring(10,12);
            String day=cid.substring(12, 14);
            birthday=year+"-"+month+"-"+day;
        }
        return birthday;
    }
}
