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
package pers.fz.mvvm.wight.gallery.gestures;


import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import pers.fz.mvvm.util.log.LogUtil;
import pers.fz.mvvm.wight.gallery.PreviewPhotoDialog;


public class EclairGestureDetector extends CupcakeGestureDetector {

    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mActivePointerIndex = 0;

    protected final ScaleGestureDetector mDetector;

    public EclairGestureDetector(Context context) {
        super(context);
        ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();

                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                    return false;
                }

                mListener.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                // NO-OP
            }
        };
        mDetector = new ScaleGestureDetector(context, mScaleListener);
    }

    @Override
    float getActiveX(MotionEvent ev) {
        try {
            return ev.getX(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getX();
        }
    }

    @Override
    float getActiveY(MotionEvent ev) {
        try {
            return ev.getY(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getY();
        }
    }

    @Override
    public boolean isScaling() {
        return mDetector.isInProgress();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mDetector.onTouchEvent(ev);
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN -> mActivePointerId = ev.getPointerId(0);
            case MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP ->
                    mActivePointerId = INVALID_POINTER_ID;
            case MotionEvent.ACTION_POINTER_UP -> {
                // Ignore deprecation, ACTION_POINTER_ID_MASK and
                // ACTION_POINTER_ID_SHIFT has same value and are deprecated
                // You can have either deprecation or lint target api warning
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                }
            }
            default -> {}
        }

        mActivePointerIndex = ev
                .findPointerIndex(mActivePointerId != INVALID_POINTER_ID ? mActivePointerId
                        : 0);
        return super.onTouchEvent(ev);
    }


}
