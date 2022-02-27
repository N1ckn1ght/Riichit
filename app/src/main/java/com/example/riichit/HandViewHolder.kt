package com.example.riichit

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class HandViewHolder(itemView: View, private val context: Context, private var tile: Int) : RecyclerView.ViewHolder(itemView) {
    val iv = itemView.findViewById<ImageView>(R.id.tile)

    init {
        itemView.setOnClickListener{
            (context as SoloActivity).discard(tile)
        }
    }

    fun bindTo(drawable: Int, tile_: Int) {
        iv.setImageResource(drawable)
        tile = tile_
    }
}
