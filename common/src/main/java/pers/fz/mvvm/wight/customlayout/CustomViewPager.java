package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.DrawableRes;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import pers.fz.mvvm.R;
import pers.fz.mvvm.adapter.PictureAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fz on 2017/12/10.
 * 重写RelativeLayout ，实现ViewPager轮播图
 */

public class CustomViewPager extends RelativeLayout{
    private ViewGroup.LayoutParams matchParams = new
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private List<ImageView> images = new ArrayList<>();
    private BannerViewPager viewPager;
    private LinearLayout dots_layout;
    private Context context;
    private static
    @DrawableRes
    int[] imgs = new int[]{};

    public void init(@DrawableRes int[] imgs, Context context){
        CustomViewPager.imgs = imgs;
        this.context = context;
        initImgs();
    }
    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initImgs() {
        if(imgs.length==1){
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(matchParams);
            Glide.with(context)
                    .load(imgs[0])
                    .apply(new RequestOptions().error(R.mipmap.icon_banner_default))
                    .into(imageView);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            this.addView(imageView);
        }else if(imgs.length>1){
            for (int img : imgs) {
                getImages(img);
            }
            addHeader();
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 记录总高度
        int mTotalHeight = 0;
        // 遍历所有子视图
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            // 获取在onMeasure中计算的视图尺寸
            int measureHeight = childView.getMeasuredHeight();
            int measuredWidth = childView.getMeasuredWidth();

            childView.layout(l, mTotalHeight, measuredWidth, mTotalHeight
                    + measureHeight);
            mTotalHeight += measureHeight;
        }
    }
    public BannerViewPager getViewPager(){
        return viewPager;
    }
    /**
     * 填充布局
     */
    private void addHeader() {
        View headerView = LayoutInflater.from(context).inflate(R.layout.viewpager_round_layout, null);
        LinearLayout images_layout = (LinearLayout) headerView.findViewById(R.id.carousel_image_layout);
        dots_layout = (LinearLayout) headerView.findViewById(R.id.image_round_layout);

        viewPager = new BannerViewPager(context,false);
        initImageRounds();
        viewPager.setImages(images);
        viewPager.setAdapter(new PictureAdapter(images));
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2);
        //一个childView只能被赋给一个parent。因此在添加前需要移除，再添加
        images_layout.removeAllViews();
        this.removeAllViews();

        images_layout.addView(viewPager);
        this.addView(headerView);
    }

    /**
     * 创建ViewPager的子item项
     *
     * @param picPath 图片路劲
     */
    private void getImages(Integer picPath) {
        ImageView img1 = new ImageView(context);
        img1.setLayoutParams(matchParams);
        Glide.with(context).load(picPath).into(img1);
        img1.setScaleType(ImageView.ScaleType.FIT_XY);
        images.add(img1);
    }

    /**
     * 计算viewPager小底部小圆点的大小
     */
    private void initImageRounds() {
        List<ImageView> dots = new ArrayList<>();
        dots_layout.removeAllViews();
        /*
         *当轮播图大于1张时小圆点显示
         */
        if (images.size() > 1) {
            dots_layout.setVisibility(View.VISIBLE);
        } else {
            dots_layout.setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i < images.size(); i++) {
            ImageView round = new ImageView(context);
            /*
             * 默认让第一张图片显示深颜色的圆点
             */
            if (i == 0) {
                round.setImageResource(R.mipmap.icon_point2);
            } else {
                round.setImageResource(R.mipmap.icon_point1);
            }
            dots.add(round);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, -2);
            params.leftMargin = 20;
            dots_layout.addView(round, params);
        }
        viewPager.setDots(dots);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = measureWidth(widthMeasureSpec);
        int measureHeight = measureHeight(heightMeasureSpec);
        // 计算自定义的ViewGroup中所有子控件的大小
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        // 设置自定义的控件ViewGroup的大小
        setMeasuredDimension(measureWidth, measureHeight);
    }

    private int measureWidth(int pWidthMeasureSpec) {
        int result = 0;
        int widthMode = MeasureSpec.getMode(pWidthMeasureSpec);// 得到模式
        int widthSize = MeasureSpec.getSize(pWidthMeasureSpec);// 得到尺寸

        switch (widthMode) {
            /**
             * mode共有三种情况，取值分别为MeasureSpec.UNSPECIFIED, MeasureSpec.EXACTLY,
             * MeasureSpec.AT_MOST。
             *
             *
             * MeasureSpec.EXACTLY是精确尺寸，
             * 当我们将控件的layout_width或layout_height指定为具体数值时如andorid
             * :layout_width="50dip"，或者为FILL_PARENT是，都是控件大小已经确定的情况，都是精确尺寸。
             *
             * MeasureSpec.AT_MOST是最大尺寸，
             * 当控件的layout_width或layout_height指定为WRAP_CONTENT时
             * ，控件大小一般随着控件的子空间或内容进行变化，此时控件尺寸只要不超过父控件允许的最大尺寸即可
             * 。因此，此时的mode是AT_MOST，size给出了父控件允许的最大尺寸。
             *
             * MeasureSpec.UNSPECIFIED是未指定尺寸，这种情况不多，一般都是父控件是AdapterView，
             * 通过measure方法传入的模式。
             */
            case MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> result = widthSize;
        }
        return result;
    }

    private int measureHeight(int pHeightMeasureSpec) {
        int result = 0;

        int heightMode = MeasureSpec.getMode(pHeightMeasureSpec);
        int heightSize = MeasureSpec.getSize(pHeightMeasureSpec);

        switch (heightMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = heightSize;
                break;
        }
        return result;
    }


}
