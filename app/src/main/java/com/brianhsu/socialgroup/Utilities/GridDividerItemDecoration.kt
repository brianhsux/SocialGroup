package com.brianhsu.socialgroup.Utilities

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class GridDividerItemDecoration(private val span: Int, private val dividerWidth: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        val itemPosition = parent.getChildLayoutPosition(view)
        val col = itemPosition % span
        outRect.top = if (itemPosition < span) 0 else dividerWidth
        outRect.bottom = 0
        outRect.right = if (col < span - 1) dividerWidth / 2 else 0
        outRect.left = if (col > 0) dividerWidth / 2 else 0
    }
}
