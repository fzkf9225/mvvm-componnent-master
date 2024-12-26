/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package pers.fz.mvvm.wight.gallery;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.View;
import android.widget.ImageView;


public interface IPhotoView {

    public static final float DEFAULT_MAX_SCALE = 3.0f;
    public static final float DEFAULT_MID_SCALE = 1.75f;
    public static final float DEFAULT_MIN_SCALE = 1.0f;
    public static final int DEFAULT_ZOOM_DURATION = 200;

    /**
     * 如果 PhotoView 设置为允许缩放图片，则返回 true。
     *
     * @return 如果 PhotoView 允许缩放，返回 true。
     */
    boolean canZoom();

    /**
     * 获取当前显示的 Drawable 的显示矩形。该矩形相对于此视图，并包括所有缩放和位移。
     *
     * @return 当前显示的 Drawable 的矩形区域
     */
    RectF getDisplayRect();

    /**
     * 设置当前显示的 Drawable 的显示矩阵。该矩形相对于此视图，并包括所有缩放和位移。
     *
     * @param finalMatrix 目标矩阵，用于设置 PhotoView
     * @return 如果矩形成功应用，则返回 true
     */
    boolean setDisplayMatrix(Matrix finalMatrix);

    /**
     * 获取当前显示的 Drawable 的显示矩阵。该矩形相对于此视图，并包括所有缩放和位移。
     *
     * @return 当前显示的矩阵
     */
    Matrix getDisplayMatrix();

    /**
     * @return 当前的最小缩放级别。该值的含义取决于当前 {@link ImageView.ScaleType}。
     */
    float getMinimumScale();

    /**
     * @return 当前的中等缩放级别。该值的含义取决于当前 {@link ImageView.ScaleType}。
     */
    float getMediumScale();

    /**
     * @return 当前的最大缩放级别。该值的含义取决于当前 {@link ImageView.ScaleType}。
     */
    float getMaximumScale();

    /**
     * 返回当前的缩放值
     *
     * @return float - 当前的缩放值
     */
    float getScale();

    /**
     * 返回当前 ImageView 使用的缩放类型。
     *
     * @return 当前的 ImageView.ScaleType
     */
    ImageView.ScaleType getScaleType();

    /**
     * 是否允许 ImageView 的父级在图片滚动到其水平边缘时拦截触摸事件。
     *
     * @param allow 是否允许父级拦截事件
     */
    void setAllowParentInterceptOnEdge(boolean allow);

    /**
     * 设置最小缩放级别。该值的含义取决于当前 {@link ImageView.ScaleType}。
     *
     * @param minimumScale 最小允许的缩放值
     */
    void setMinimumScale(float minimumScale);

    /*
     * 设置中等缩放级别。该值的含义取决于当前 {@link android.widget.ImageView.ScaleType}。
     *
     * @param mediumScale 中等缩放预设值
     */
    void setMediumScale(float mediumScale);

    /**
     * 设置最大缩放级别。该值的含义取决于当前 {@link ImageView.ScaleType}。
     *
     * @param maximumScale 最大允许的缩放值
     */
    void setMaximumScale(float maximumScale);

    /**
     * 注册一个回调函数，当此视图显示的照片被长按时调用。
     *
     * @param listener - 要注册的监听器。
     */
    void setOnLongClickListener(View.OnLongClickListener listener);

    /**
     * 注册一个回调函数，当此视图的矩阵发生变化时调用。一个示例是用户平移或缩放照片。
     *
     * @param listener - 要注册的监听器。
     */
    void setOnMatrixChangeListener(PhotoViewAttacher.OnMatrixChangedListener listener);

    /**
     * 注册一个回调函数，当此视图显示的照片被单击时调用。
     *
     * @param listener - 要注册的监听器。
     */
    void setOnPhotoTapListener(PhotoViewAttacher.OnPhotoTapListener listener);

    /**
     * 返回一个回调监听器，当此视图显示的照片被单击时调用。
     *
     * @return 当前设置的 PhotoViewAttacher.OnPhotoTapListener，可能为 null
     */
    PhotoViewAttacher.OnPhotoTapListener getOnPhotoTapListener();

    /**
     * 注册一个回调函数，当此视图被单击时调用。
     *
     * @param listener - 要注册的监听器。
     */
    void setOnViewTapListener(PhotoViewAttacher.OnViewTapListener listener);

    /**
     * 启用通过 PhotoView 内部函数进行旋转。
     *
     * @param rotationDegree - 旋转角度，范围应在 0 到 360 之间
     */
    void setRotationTo(float rotationDegree);

    /**
     * 启用通过 PhotoView 内部函数进行旋转。
     *
     * @param rotationDegree - 旋转角度，范围应在 0 到 360 之间
     */
    void setRotationBy(float rotationDegree);

    /**
     * 返回一个回调监听器，当此视图被单击时调用。
     *
     * @return 当前设置的 PhotoViewAttacher.OnViewTapListener，可能为 null
     */
    PhotoViewAttacher.OnViewTapListener getOnViewTapListener();

    /**
     * 将当前缩放值更改为指定的值。
     *
     * @param scale - 要缩放到的值
     */
    void setScale(float scale);

    /**
     * 将当前缩放值更改为指定的值。
     *
     * @param scale   - 要缩放到的值
     * @param animate - 是否启用缩放动画
     */
    void setScale(float scale, boolean animate);

    /**
     * 将当前缩放值更改为指定的值，并围绕给定的焦点进行缩放。
     *
     * @param scale   - 要缩放到的值
     * @param focalX  - X 轴焦点
     * @param focalY  - Y 轴焦点
     * @param animate - 是否启用缩放动画
     */
    void setScale(float scale, float focalX, float focalY, boolean animate);

    /**
     * 控制图像应如何调整大小或移动以匹配 ImageView 的大小。任何缩放或平移都会在此 {@link
     * ImageView.ScaleType} 的范围内发生。
     *
     * @param scaleType - 期望的缩放模式。
     */
    void setScaleType(ImageView.ScaleType scaleType);

    /**
     * 允许启用/禁用 ImageView 的缩放功能。当禁用时，ImageView 会恢复为使用 FIT_CENTER 矩阵。
     *
     * @param zoomable - 是否启用缩放功能。
     */
    void setZoomable(boolean zoomable);

    void setPhotoViewRotation(float rotationDegree);

    /**
     * 提取当前可见区域并转换为 Bitmap 对象，如果尚未加载图片或 ImageView 已被销毁，则返回 {@code null}。
     *
     * @return 当前可见区域的 Bitmap，或者 null
     */
    Bitmap getVisibleRectangleBitmap();

    /**
     * 允许更改缩放过渡速度，默认值为 200（PhotoViewAttacher.DEFAULT_ZOOM_DURATION）。
     * 如果提供负值，则默认为 200。
     *
     * @param milliseconds 缩放插值的持续时间
     */
    void setZoomTransitionDuration(int milliseconds);

    /**
     * 返回 IPhotoView 的实例（例如 PhotoViewAttacher），可以用于提供更好的集成。
     *
     * @return 如果可用，则返回 IPhotoView 实现实例，否则返回 null。
     */
    IPhotoView getIPhotoViewImplementation();

    /**
     * 设置自定义双击监听器，以拦截默认的功能。要将行为重置为默认值，可以传递 "null" 或 PhotoViewAttacher.defaultOnDoubleTapListener 公共字段。
     *
     * @param newOnDoubleTapListener 要设置的自定义 OnDoubleTapListener
     */
    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener newOnDoubleTapListener);
}
