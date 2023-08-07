package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import pers.fz.mvvm.R;
import pers.fz.mvvm.activity.WebViewActivity;
import pers.fz.mvvm.adapter.PictureAdapter;
import pers.fz.mvvm.bean.BannerBean;
import pers.fz.mvvm.util.apiUtil.StringUtil;
import pers.fz.mvvm.wight.picDialog.PicShowDialog;
import pers.fz.mvvm.wight.picDialog.bean.ImageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fz on 2018/3/15.
 * 自定义banner轮播图
 */

public class CustomBannerPicture extends RelativeLayout implements View.OnClickListener, BannerViewPager.OnImageItemClickListener {
    private Context mContext;
    private ViewGroup.LayoutParams matchParams = new
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private List<ImageView> images = new ArrayList<>();
    private BannerViewPager viewPager;
    private LinearLayout dotsLayout;
    private boolean isBrowse = false, isAutoBanner = true;
    private List<ImageInfo> imageInfosList = new ArrayList<>();
    private int mCurrentPosition = 0;
    private List<BannerBean> imgPic;
    float width, height;
    private final static int radius = 8;

    public CustomBannerPicture(Context context) {
        super(context);
    }

    public CustomBannerPicture(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Topbar);
        isBrowse = ta.getBoolean(R.styleable.Topbar_isBrowse, false);
        isAutoBanner = ta.getBoolean(R.styleable.Topbar_isAutoBanner, false);
        this.mContext = context;
        setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, R.dimen.x392));
    }

    public void initView(List<BannerBean> imgPic) {
        this.imgPic = imgPic;
        if (!imgPic.isEmpty()) {
            imgPic.size();
            if (imgPic.size() == 1) {
                ImageView img1 = LayoutInflater.from(mContext).inflate(R.layout.banner_corner_image_view, null).findViewById(R.id.corner_image);
                img1.setLayoutParams(matchParams);
                img1.setOnClickListener(this);
                Object imgPath = imgPic.get(0).getPath() == null ? imgPic.get(0).getLocalPath() : imgPic.get(0).getPath();
                Glide.with(mContext)
                        .load(imgPath)
                        .apply(new RequestOptions().placeholder(R.mipmap.ic_default_image).error(R.mipmap.ic_default_image))
                        .into(img1);
                imageInfosList.add(new ImageInfo(imgPath, 200, 200));

                addView(img1);
                img1.setOnClickListener(this);
            } else {
                images.clear();
                for (int k = 0; k < imgPic.size(); k++) {
                    Object imgPath = imgPic.get(k).getPath() == null ? imgPic.get(k).getLocalPath() : imgPic.get(k).getPath();
                    imageInfosList.add(new ImageInfo(imgPath, 200, 200));
                    getImages(imgPath);
                }
                addHeader();
            }
        }
    }

    /**
     * 创建ViewPager的子item项
     */
    private void getImages(Object oldPath) {
        ImageView img1 = LayoutInflater.from(mContext).inflate(R.layout.banner_corner_image_view, null).findViewById(R.id.corner_image);
        img1.setLayoutParams(matchParams);
        img1.setOnClickListener(this);
        Glide.with(mContext)
                .load(oldPath)
                .apply(new RequestOptions().placeholder(R.mipmap.ic_default_image).error(R.mipmap.ic_default_image))
                .into(img1);
        images.add(img1);
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    /**
     * 填充布局
     */
    private void addHeader() {
        View headerView = LayoutInflater.from(mContext).inflate(R.layout.viewpager_round_layout_index, null);
        LinearLayout imagesLayout = (LinearLayout) headerView.findViewById(R.id.carousel_image_layout);
        dotsLayout = (LinearLayout) headerView.findViewById(R.id.image_round_layout);
        viewPager = new BannerViewPager(mContext, isAutoBanner, drawableResCurrent, drawableResNormal);
        initImageRounds();
        viewPager.setImages(images);
        viewPager.setOnImageItemClickListener(this);
        viewPager.setAdapter(new PictureAdapter(images));
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2);
        //一个childView只能被赋给一个parent。因此在添加前需要移除，再添加
        imagesLayout.removeAllViews();
        removeAllViews();

        imagesLayout.addView(viewPager);
        addView(headerView);
    }

    private @DrawableRes
    int drawableResCurrent = R.mipmap.icon_point2;
    private @DrawableRes
    int drawableResNormal = R.mipmap.icon_point1;

    public void setRoundDots(@DrawableRes int drawableResCurrent, @DrawableRes int drawableResNormal) {
        this.drawableResCurrent = drawableResCurrent;
        this.drawableResNormal = drawableResNormal;
    }

    /**
     * 计算viewPager小底部小圆点的大小
     */
    private void initImageRounds() {
        List<ImageView> dots = new ArrayList<>();
        dotsLayout.removeAllViews();
        /*
         *当轮播图大于1张时小圆点显示
         */
        if (images.size() > 1) {
            dotsLayout.setVisibility(View.VISIBLE);
        } else {
            dotsLayout.setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i < images.size(); i++) {
            ImageView round = new ImageView(mContext);
            /*
             * 默认让第一张图片显示深颜色的圆点
             */
            if (i == 0) {
                round.setBackground(ContextCompat.getDrawable(getContext(), drawableResCurrent));
            } else {
                round.setBackground(ContextCompat.getDrawable(getContext(), drawableResNormal));
            }
            dots.add(round);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, -2);
            params.leftMargin = 20;
            dotsLayout.addView(round, params);
        }
        viewPager.setDots(dots);
    }

    @Override
    public void onClick(View v) {
        if (imageInfosList == null || imageInfosList.isEmpty()) {
            return;
        }
        if (isBrowse) {
            new PicShowDialog(mContext, imageInfosList, mCurrentPosition).show();
        } else if (!StringUtil.isEmpty(imgPic.get(mCurrentPosition).getLinkPath())) {
            //排除默认的“#”号空链接
            if ("#".equals(imgPic.get(mCurrentPosition).getLinkPath())) {
                return;
            }
            WebViewActivity.show(mContext, imgPic.get(mCurrentPosition).getLinkPath(), null);
        }
    }

    @Override
    public void onItemClick(int itemPosition) {

    }

    @Override
    public void getPosition(int itemPosition) {
        dotsLayout.requestLayout();
        mCurrentPosition = itemPosition;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //这里做下判断，只有图片的宽高大于设置的圆角距离的时候才进行裁剪
        int maxLeft = radius;
        int maxRight = radius;
        int minWidth = radius;
        int maxTop = radius;
        int maxBottom = radius;
        int minHeight = maxTop + maxBottom;
        if (width >= minWidth && height > minHeight) {
            Path path = new Path();
            //四个角：右上，右下，左下，左上
            path.moveTo(radius, 0);
            path.lineTo(width - radius, 0);
            path.quadTo(width, 0, width, radius);

            path.lineTo(width, height - radius);
            path.quadTo(width, height, width - radius, height);

            path.lineTo(radius, height);
            path.quadTo(0, height, 0, height - radius);

            path.lineTo(0, radius);
            path.quadTo(0, 0, radius, 0);

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            canvas.clipPath(path);
//            } else {
//                canvas.clipPath(path, Region.Op.XOR);// REPLACE、UNION 等
//            }
        }

        super.onDraw(canvas);
    }
}
