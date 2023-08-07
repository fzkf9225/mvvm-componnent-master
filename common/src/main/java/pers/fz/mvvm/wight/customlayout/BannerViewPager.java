package pers.fz.mvvm.wight.customlayout;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import pers.fz.mvvm.R;

import java.util.Date;
import java.util.List;

/**
 * Created by fz on 2016/8/19.
 *
 */
public class BannerViewPager extends ViewPager {

    /**
     * 图片和小圆点
     */
    private List<ImageView> images;
    private List<ImageView> dots;

    /**
     * 停止轮播的标记
     */
    private boolean stopLoopTag = false;

    /**
     * lastPointPosition 上一个点的位置,初始化为0
     * lastPointIndex 上一个点在集合中的位置
     * currentPointIndex 当前的点在集合中的位置
     */
    private int lastPointPosition = 0;
    private int lastPointIndex;
    private int currentPointIndex;

    /**
     * 记录点击时的坐标
     */
    private int xDown;
    private int yDown;

    /**
     * 用于处理手势事件
     */
    private long touchTime;
    private boolean timeTag;
    private TimeThread timeThread;
    private boolean isOpen;
    private @DrawableRes
    int drawableResCurrent = R.mipmap.icon_point2;
    private @DrawableRes int drawableResNormal = R.mipmap.icon_point1;

    public BannerViewPager(Context context, boolean isOpen) {
        super(context);
        initLoop();
        this.isOpen = isOpen;
        if (!isOpen) {
            stopLoop();
        }
        addOnPageChangeListener(onPageChangeListener);
    }
    public BannerViewPager(Context context, boolean isOpen, @DrawableRes int drawableResCurrent, @DrawableRes int drawableResNormal) {
        super(context);
        this.drawableResCurrent = drawableResCurrent;
        this.drawableResNormal = drawableResNormal;
        initLoop();
        this.isOpen = isOpen;
        if (!isOpen) {
            stopLoop();
        }
        addOnPageChangeListener(onPageChangeListener);
    }

    public List<ImageView> getImages() {
        return images;
    }

    public void setImages(List<ImageView> images) {
        this.images = images;
    }

    public void setDots(List<ImageView> dots) {
        this.dots = dots;
    }

    /**
     * 处理轮播的Handler
     */
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 10 && !stopLoopTag) {
                setCurrentItem(msg.arg1);
            } else {
                mHandler.removeMessages(10);
            }
            return true;
        }
    });

    private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            /**
             * 改变小圆点的状态
             * 三种情况:
             *         1.在初始位置
             *         2.向左滑动
             *         3.向右滑动
             * 不要放在上一个方法中,会有明显的延迟现象出现
             * lastPointPosition 上一个点的位置,初始化为0
             * lastPointIndex 上一个点在集合中的位置
             * currentPointIndex 当前的点在集合中的位置
             * */
            if (lastPointPosition == 0) {

                currentPointIndex = 0;
                lastPointIndex = 0;

            } else if (lastPointPosition < position) {

                if (currentPointIndex == getImages().size() - 1) {
                    currentPointIndex = 0;
                } else {
                    currentPointIndex += 1;
                }

            } else if (lastPointPosition > position) {

                if (currentPointIndex == 0) {
                    currentPointIndex = getImages().size() - 1;
                } else {
                    currentPointIndex -= 1;
                }
            }

            dots.get(lastPointIndex).setBackground(ContextCompat.getDrawable(getContext(), drawableResNormal));
            dots.get(currentPointIndex).setBackground(ContextCompat.getDrawable(getContext(),drawableResCurrent));
            lastPointPosition = position;
            lastPointIndex = currentPointIndex;
            if (onImageItemClickListener != null) {
                onImageItemClickListener.getPosition(lastPointIndex);
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * 停止轮播
     */
    public void stopLoop() {

        if (!stopLoopTag) {
            stopLoopTag = true;
        }

    }

    /**
     * 开启轮播
     */
    public void openLoop() {

        if (stopLoopTag) {
            stopLoopTag = false;
        }

    }


    /**
     * 手势事件的重写
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                /**
                 * 按下的时候停止轮播
                 * */
                stopLoop();
                xDown = (int) event.getX();
                yDown = (int) event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                /**
                 * 这里不用做任何处理,移动的时候ViewPager内的图片会自动滑动
                 * */
                break;

            case MotionEvent.ACTION_UP:
                if (isOpen){
                    /**
                     * 记录按下时间
                     * */
                    if (timeThread == null) {
                        touchTime = new Date().getTime();
                        timeTag = true;
                        timeThread = new TimeThread();
                        timeThread.start();
                    } else {
                        touchTime = new Date().getTime();
                    }
                }
                /*
                 * 判断是否是点击事件
                 * */
                int xUp = (int) event.getX();
                int yUp = (int) event.getY();
                if (Math.abs(xDown - xUp) < 20 && Math.abs(yDown - yUp) < 20) {

                    if (onImageItemClickListener != null) {
                        onImageItemClickListener.onItemClick(currentPointIndex);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 初始化轮播
     */
    public void initLoop() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(3000);
                        if (!stopLoopTag) {
                            Message message = Message.obtain();
                            message.what = 10;
                            message.arg1 = getCurrentItem() + 1;
                            mHandler.sendMessage(message);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    /**
     * 时间线程,用于记录手指离开点击ViewPager的时间
     * 如果离开的时间 >= 2000毫秒,那么ViewPager继续轮播
     */
    class TimeThread extends Thread {

        @Override
        public void run() {
            while (timeTag) {
                long currentTime = new Date().getTime();
                if (currentTime - touchTime >= 2000) {
                    openLoop();
                    timeTag = false;
                    timeThread = null;
                }
            }
        }
    }

    /**
     * 对Activity暴露接口
     */
    private OnImageItemClickListener onImageItemClickListener;

    public interface OnImageItemClickListener {
        void onItemClick(int itemPosition);
        void getPosition(int itemPosition);
    }

    public void setOnImageItemClickListener(OnImageItemClickListener onImageItemClickListener) {
        this.onImageItemClickListener = onImageItemClickListener;
    }


}
