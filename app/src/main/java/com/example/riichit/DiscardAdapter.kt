package com.example.riichit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.riichit.Drawables.tiles
import com.example.riichit.Utility.logicIncrement

class DiscardAdapter(
    private val inflater: LayoutInflater,
    private var width: Int,
    private var height: Int,
    private val padding: Int
) : ListAdapter<Int, DiscardViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscardViewHolder {
        val row: View = inflater.inflate(R.layout.tile, parent, false)
        parent.setPadding(padding, padding, padding, padding)
        return DiscardViewHolder(row, 136)
    }

    override fun onBindViewHolder(holder: DiscardViewHolder, position: Int) {
        var x = getItem(position)
        holder.iv.layoutParams.width = width
        holder.iv.layoutParams.height = height
        if (x > logicIncrement - 1) {
            // TODO: find a way of rotating tile without changing its size or breaking the grid
            // holder.iv.rotation += 90F
            x -= logicIncrement
        }
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