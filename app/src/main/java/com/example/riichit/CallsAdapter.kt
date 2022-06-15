package com.example.riichit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.riichit.Utility.setMargin

class CallsAdapter(
    private val inflater: LayoutInflater,
    private val context: Context,
    private val width: Int,
    private val height: Int,
    private val padding: Int
) : ListAdapter<Int, DiscardViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscardViewHolder {
        val row: View = inflater.inflate(R.layout.tile, parent, false)
        parent.setPadding(0, padding, 0, padding)
        return DiscardViewHolder(row, 136)
    }

    override fun onBindViewHolder(holder: DiscardViewHolder, position: Int) {
        context as SoloActivity
        holder.iv.layoutParams.width = width
        holder.iv.layoutParams.height = height
        if ((position + 1) % 4 == 0) {
            holder.iv.setMargin(0, 0, padding, 0)
        }

        val x = getItem(position)
        holder.bindTo(context.tiles[x / 4], x)
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Int> =
            object : DiffUtil.ItemCallback<Int>() {
                override fun areItemsTheSame(oldTile: Int, newTile: Int): Boolean {
                    return oldTile == newTile
                }

                override fun areContentsTheSame(oldTile: Int, newTile: Int): Boolean {
                    return areItemsTheSame(oldTile, newTile)
                }
            }
    }
}