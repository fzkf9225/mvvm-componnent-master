package pers.fz.mvvm.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by fz on 2024/11/22
 *
 */
public class PictureAdapter extends PagerAdapter {


    private final List<ImageView> images;
    
    /**
     * initPosition -1为初始化的位置，后面是当前的图片索引位置
     * topPosition 记录上一次初始化的索引位，用于计算上次的position和本次position的偏移量
     * 
     */
    private int initPosition = -1;
    private int topPosition = -1;

    public PictureAdapter(List<ImageView> images) {
        this.images = images;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(@NotNull View view, @NotNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
    }

    /**
     * 实例化Item
     */
    @NotNull
    @Override
    public Object instantiateItem(@NotNull ViewGroup container, int position) {

        if(images.size() <=1){
            ImageView imageView = images.get(topPosition);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            container.addView(imageView);
            return imageView;
        }else{
            /*
             * 初始化状态
             * 向左滑动
             * 向右滑动
             * 由于ViewPager有预加载机制,默认加载一页,因此在第一次初始化的时候,会调用三次这个方法.
             * (第一次： position=1073741823 第二次： position=1073741822 第三次： position=1073741824)
             *
             * 而后续,这个方法仅被执行一次,并且执行的是预加载下一页的请求.
             * */
            if (initPosition == -1) {
                /*
                 * 初始化状态
                 * topPosition 记录第一次初始化的索引位.用于后续作比较判断下次是向右滑动还是向左滑动
                 * initPosition 初始化图片集合的索引值
                 */
                topPosition = position;
                initPosition = 0;
            } else if (topPosition < position) {
                /*
                 * 向左滑动
                 * 得出偏移量后比较是否超过图片集合的大小
                 */
                int value = position - topPosition;
                initPosition += value;
                if (initPosition == images.size()) {
                    /*
                     * 滑动到了最后一页
                     */
                    initPosition = 0;
                }
                if (initPosition > images.size()) {
                    /*
                     * 如果超出了图片集合的大小,则 initPosition = 超过的数值
                     */
                    initPosition = (initPosition - images.size());
                }
                topPosition = position;
            } else if (topPosition > position) {
                int value = topPosition - position;
                initPosition -= value;
                if (initPosition == -1) {
                    /*
                     * 滑动到了第一页
                     */
                    initPosition = images.size() - 1;
                }
                if (initPosition < -1) {
                    /*
                     * 当计算后的值小于了集合大小，则用集合大小减去小于的这部分
                     */
                    initPosition = (images.size() - (Math.abs(initPosition)));
                }
                topPosition = position;
            }
            if(initPosition>(images.size()-1)){
                initPosition = 0;
            }
            /*
             * 只用这句话应该会出现问题
             */
//        position %= images.size();
//        if (position < 0) {
//            position = position + images.size();
//        }
            ImageView imageView = null;
            try{
                imageView = images.get(initPosition);
                ViewParent parent = imageView.getParent();
                if (parent != null) {
                    ViewGroup viewGroup = (ViewGroup) parent;
                    viewGroup.removeView(imageView);
                }
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                container.addView(imageView);
            }catch (Exception e){
                e.printStackTrace();
            }
            return imageView;
        }
    }
}
