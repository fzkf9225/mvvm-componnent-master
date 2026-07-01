package io.coderf.arklab.common.widget.gallery.scrollerproxy;

import android.content.Context;

public abstract class ScrollerProxy {

    public static ScrollerProxy getScroller(Context context) {
        return new GingerScroller(context);
    }

    public abstract boolean computeScrollOffset();

    public abstract void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY,
                               int maxY, int overX, int overY);

    public abstract void forceFinished(boolean finished);

    public abstract boolean isFinished();

    public abstract int getCurrX();

    public abstract int getCurrY();


}
