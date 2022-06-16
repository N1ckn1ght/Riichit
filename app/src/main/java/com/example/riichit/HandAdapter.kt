package com.example.riichit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.riichit.Drawables.tiles

class HandAdapter(
    private val inflater: LayoutInflater,
    private val discard: (toDiscard: Int) -> Unit,
    private val width: Int,
    private val height: Int,
    private val padding: Int
) : ListAdapter<Int, HandViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HandViewHolder {
        val row: View = inflater.inflate(R.layout.tile, parent, false)
        parent.setPadding(padding, 0, padding, padding)
        return HandViewHolder(row, discard, 136)
    }

    override fun onBindViewHolder(holder: HandViewHolder, position: Int) {
        val x = getItem(position)
        holder.iv.layoutParams.width = width
        holder.iv.layoutParams.height = height
        holder.bindTo(tiles[x / 4], x)
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