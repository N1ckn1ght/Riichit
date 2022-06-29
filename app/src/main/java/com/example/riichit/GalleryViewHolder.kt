package com.example.riichit

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class GalleryViewHolder(
    itemView: View,
    onClick: (id: Int) -> Unit,
    private var id: Int
) : RecyclerView.ViewHolder(itemView) {
    val iv: ImageView = itemView.findViewById(R.id.image)

    init {
        itemView.setOnClickListener {
            onClick(id)
        }
    }

    fun bindTo(drawable: Int, id_: Int) {
        iv.setImageResource(drawable)
        id = id_
    }
}