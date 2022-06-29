package com.example.riichit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.riichit.Drawables.cards
import com.example.riichit.Utility.logicIncrement
import com.example.riichit.Utility.setMargin
import com.example.riichit.Utility.toInt

class GalleryAdapter(
    private val inflater: LayoutInflater,
    private val onClick: (id: Int) -> Unit,
    private val width: Int,
    private val height: Int,
    private val padding: Int
) : ListAdapter<Int, GalleryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val row: View = inflater.inflate(R.layout.image, parent, false)
        return GalleryViewHolder(row, onClick, 0)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val x = getItem(position)
        holder.iv.layoutParams.width = width
        holder.iv.layoutParams.height = height
        holder.iv.setMargin(0, 0, 0, 0)
        // the structure of strings in strings.xml will be like this:
        // [0] - debug message
        // [1, size + 1] - locked achievements
        // [1 + logicIncrement, size + 1 + logicIncrement] - unlocked achievements
        // where size = achievements.size, which also equals cards.size - 1
        // holder.bindTo(cards[x], position + 1 + logicIncrement * (x > 0).toInt())
        holder.bindTo(cards[x], position + 1)
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Int> =
            object : DiffUtil.ItemCallback<Int>() {
                override fun areItemsTheSame(oldImage: Int, newImage: Int): Boolean {
                    return oldImage == newImage
                }

                override fun areContentsTheSame(oldImage: Int, newImage: Int): Boolean {
                    return areItemsTheSame(oldImage, newImage)
                }
            }
    }
}