package com.example.riichit

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class DiscardViewHolder(itemView: View, private var tile: Int) : RecyclerView.ViewHolder(itemView) {
    val iv: ImageView = itemView.findViewById(R.id.tile)

    fun bindTo(drawable: Int, tile_: Int) {
        iv.setImageResource(drawable)
        tile = tile_
    }
}