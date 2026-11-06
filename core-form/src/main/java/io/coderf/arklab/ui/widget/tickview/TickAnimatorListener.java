package io.coderf.arklab.ui.widget.tickview;

/**
 * TickAnimatorListener 接口。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2019/10/22
 */
public interface TickAnimatorListener {
    void onAnimationStart(TickView tickView);

    void onAnimationEnd(TickView tickView);

    abstract class TickAnimatorListenerAdapter implements TickAnimatorListener {
        @Override
        public void onAnimationStart(TickView tickView) {

        }

        @Override
        public void onAnimationEnd(TickView tickView) {

        }
    }
}
