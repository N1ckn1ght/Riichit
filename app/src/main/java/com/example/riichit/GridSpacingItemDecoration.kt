package com.example.riichit

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// source: https://gist.github.com/liangzhitao/e57df3c3232ee446d464
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean,
    private val headerNum: Int,
    private val isReverse: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) - headerNum

        if (position >= 0) {
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount

                if (position < spanCount) {
                    if (isReverse)
                        outRect.bottom = spacing else
                        outRect.top = spacing
                }

                if (isReverse)
                    outRect.top = spacing else
                    outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount

                if (position >= spanCount) {
                    if (isReverse)
                        outRect.bottom = spacing else
                        outRect.top = spacing
                }
            }
        } else {
            outRect.left = 0
            outRect.right = 0
            outRect.top = 0
            outRect.bottom = 0
        }
    }
}