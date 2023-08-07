package pers.fz.mvvm.wight.recyclerview;


import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by fz on 2017/11/24.
 *
 */

public interface OnStartDragListener {
    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}
