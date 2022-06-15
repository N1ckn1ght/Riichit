package com.example.riichit

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class HandViewHolder(
    itemView: View,
    discard: (toRemove: Int) -> Unit,
    private var tile: Int
) : RecyclerView.ViewHolder(itemView) {
    val iv: ImageView = itemView.findViewById(R.id.tile)

    init {
        itemView.setOnClickListener {
            discard(tile)
        }
    }

    fun bindTo(drawable: Int, tile_: Int) {
        iv.setImageResource(drawable)
        tile = tile_
    }
}