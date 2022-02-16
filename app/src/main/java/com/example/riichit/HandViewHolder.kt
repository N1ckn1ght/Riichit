package com.example.riichit

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class HandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val iv = itemView.findViewById<ImageView>(R.id.tile)

    init {
        itemView.setOnClickListener{
            // TODO: Change the tile on click
        }
    }

    fun bindTo(drawable: Int) {
        iv.setImageResource(drawable)
    }
}
