package com.casic.titan.demo.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.casic.titan.commonui.bean.AttachmentBean;
import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.FormBean;
import com.casic.titan.demo.databinding.ActivityFormDetailBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.viewmodel.EmptyViewModel;

public class FormDetailActivity extends BaseActivity<EmptyViewModel, ActivityFormDetailBinding> {
   private final List<FormDemoFileBean> imageList = new ArrayList<>();
    private final List<FormDemoFileBean> videoList = new ArrayList<>();

    private String htmlText = "<div class=\"content\"> \n" +
            " <p style=\"margin: 0 0 0;text-align: center;font-family: Calibri;font-size: 14px;white-space: normal;line-height: 37px\"><span style=\"font-family: 方正小标宋简体;font-size: 29px\"></span></p> \n" +
            " <p style=\"margin: 0 0 0;text-align: center;font-family: Calibri;font-size: 14px;white-space: normal;line-height: 37px\"><span style=\"font-family: 方正小标宋简体;font-size: 29px\">信阳市人民政府办公室关于印发信阳市茶叶品牌创建和市场开拓奖励办法的通知</span></p> \n" +
            " <p style=\"margin: 0px; text-align: center; font-family: Calibri; font-size: 14px; white-space: normal; line-height: 37px;\"><span style=\"font-family: 宋体;font-size: 21px\">&nbsp;信政办〔2018〕77号</span></p> \n" +
            " <p style=\"margin: 0 0 0;text-align: justify;font-family: Calibri;font-size: 14px;white-space: normal;line-height: 37px\"><span style=\"font-family: 宋体;font-size: 21px\"><br></span></p> \n" +
            " <p style=\"margin: 0 0 0;text-align: justify;font-family: Calibri;font-size: 14px;white-space: normal;line-height: 37px\"><span style=\"font-family: 宋体;font-size: 21px\">各县、区人民政府，各管理区、开发区，市政府有关部门：</span></p> \n" +
            " <p style=\"margin: 0 0 0;text-align: justify;font-family: Calibri;font-size: 14px;white-space: normal;text-indent: 43px;line-height: 37px\"><span style=\"font-family: 宋体;font-size: 21px\">《信阳市茶叶品牌创建和市场开拓奖励办法》已经市政府同意，现印发给你们，请结合实际，认真组织实施。</span></p> \n" +
            " <p style=\"margin: 0 0 0;text-align: justify;font-family: Calibri;font-size: 14px;white-space: normal;line-height: 37px\"><span style=\"font-family: 宋体;font-size: 21px\">&nbsp;</span></p> \n" +
            " <p style=\"margin: 0 0 0;text-align: justify;font-family: Calibri;font-size: 14px;white-space: normal;line-height: 37px\"><span style=\"font-family: 宋体;font-size: 21px\">&nbsp;</span></p> \n" +
            " <p style=\"margin: 0 0 0;text-align: justify;font-family: Calibri;font-size: 14px;white-space: normal;line-height: 37px\"><span style=\"font-family: 宋体;font-size: 21px\">&nbsp;</span></p> \n" +
            " <p style=\"margin: 0 0 0;text-align: justify;font-family: Calibri;font-size: 14px;white-space: normal;line-height: 37px\"><span style=\"font-family: 宋体;font-size: 21px\">&nbsp;</span></p> \n" +
            " <p style=\"margin: 0px; text-align: right; font-family: Calibri; font-size: 14px; white-space: normal; text-indent: 320px; line-height: 37px;\"><span style=\"font-family: 宋体;font-size: 21px\">信阳市人民政府办公室<span style=\"font-family:宋体\"><br></span></span></p> \n" +
            " <p style=\"margin: 0px; text-align: right; font-family: Calibri; font-size: 14px; white-space: normal; text-indent: 320px; line-height: 37px;\"><span style=\"font-family: 宋体;font-size: 21px\"><span style=\"font-family:宋体\">&nbsp;2018年7月27日</span></span></p> \n" +
            " <p style=\"margin: 0 0 0;text-align: justify;font-family: Calibri;font-size: 14px;white-space: normal;line-height: 37px\"><span style=\"font-family: 宋体;font-size: 21px\">&nbsp;</span></p> \n" +
            " <p style=\"margin: 0 0 0;text-align: justify;font-family: Calibri;font-size: 14px;white-space: normal;text-indent: 43px;line-height: 37px\"><span style=\"font-family: 宋体;font-size: 21px\">附件：</span></p> \n" +
            " <p style=\"line-height: 16px;\"><img src=\"http://lycyj.xinyang.gov.cn/ueditor/php/upload/17921637631177.gif\"><a href=\"http://lycyj.xinyang.gov.cn/ueditor/php/../../upload/2021/16376310357163.pdf\">信阳市茶叶品牌创建和市场开拓奖励办法.pdf</a></p> \n" +
            " <p style=\"margin: 0 0 0;text-align: justify;font-family: Calibri;font-size: 14px;white-space: normal;text-indent: 43px;line-height: 37px\"><span style=\"font-family: 宋体;font-size: 21px\"></span><br></p> \n" +
            " <p style=\"margin: 0 0 0;text-align: center;font-family: Calibri;font-size: 14px;white-space: normal;line-height: 37px\"><span style=\"font-family: 宋体;font-size: 21px\"></span><br></p> \n" +
            " <p><br></p> \n" +
            "</div>";
    @Override
    protected int getLayoutId() {
        return R.layout.activity_form_detail;
    }

    @Override
    public String setTitleBar() {
        return "表单样式展示";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        FormDemoFileBean attachmentBean1 = new FormDemoFileBean();
        attachmentBean1.setUrl("https://img1.baidu.com/it/u=805676447,2282344960&fm=253&fmt=auto&app=120&f=JPEG?w=1280&h=800");
        attachmentBean1.setFileName("1.jpg");
        FormDemoFileBean attachmentBean2 = new FormDemoFileBean();
        attachmentBean2.setUrl("https://n.sinaimg.cn/translate/125/w690h1035/20180414/Rb2D-fzcyxmu4457695.jpg");
        attachmentBean2.setFileName("2.jpg");
        FormDemoFileBean attachmentBean3 = new FormDemoFileBean();
        attachmentBean3.setUrl("https://bkimg.cdn.bcebos.com/pic/21a4462309f7905298220197bda2c0ca7bcb0a467f42");
        attachmentBean3.setFileName("3.jpg");
        FormDemoFileBean attachmentBean4 = new FormDemoFileBean();
        attachmentBean4.setUrl("https://q8.itc.cn/images01/20240208/45d5ee19361f4f8fa824e93ebfc42a8a.jpeg");
        attachmentBean4.setFileName("4.jpg");
        FormDemoFileBean attachmentBean5 = new FormDemoFileBean();
        attachmentBean5.setUrl("https://ww1.sinaimg.cn/mw690/008vmhs1ly1hrhly2i2jtj30j616nq9v.jpg");
        attachmentBean5.setFileName("5.jpg");
        FormDemoFileBean attachmentBean6 = new FormDemoFileBean();
        attachmentBean6.setUrl("https://img2.baidu.com/it/u=2380808412,3135171519&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1199");
        attachmentBean6.setFileName("6.jpg");
        imageList.add(attachmentBean1);
        imageList.add(attachmentBean2);
        imageList.add(attachmentBean3);
        imageList.add(attachmentBean4);
        imageList.add(attachmentBean5);
        imageList.add(attachmentBean6);

        FormDemoFileBean formDemoFileBean = new FormDemoFileBean();
        formDemoFileBean.setUrl("http://alvideo.ippzone.com/zyvd/98/90/b753-55fe-11e9-b0d8-00163e0c0248");
        formDemoFileBean.setFileName("1.mp4");
        videoList.add(formDemoFileBean);
        binding.setData(new FormBean("1","我是测试标题","2024-11-12 14:53:07",
                "支付宝称，因系统消息库出现局部故障，导致部分用户的支付功能受到影响。该故障不会影响用户的资金安全，截止到上午10点50分故障已经修复。对于给用户带来的不便，支付宝深表歉意。",htmlText));
        binding.formScreenImage.setImages(imageList);
        binding.formScreenVideo.setImages(videoList);
    }

    @Override
    public void initData(Bundle bundle) {

    }

    public static class FormDemoFileBean extends AttachmentBean{

    }
}