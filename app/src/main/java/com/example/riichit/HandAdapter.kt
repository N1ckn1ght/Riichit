package com.example.riichit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class HandAdapter(private val inflater: LayoutInflater, private val context: Context,
                  private val width: Int, private val height: Int, private val padding: Int) : ListAdapter<Int, HandViewHolder>(DIFF_CALLBACK) {
    lateinit var tiles: MutableList<Int>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HandViewHolder {
        tiles = mutableListOf(
                R.drawable.man_1, R.drawable.man_2, R.drawable.man_3, R.drawable.man_4, R.drawable.man_5, R.drawable.man_6, R.drawable.man_7, R.drawable.man_8, R.drawable.man_9,
                R.drawable.pin_1, R.drawable.pin_2, R.drawable.pin_3, R.drawable.pin_4, R.drawable.pin_5, R.drawable.pin_6, R.drawable.pin_7, R.drawable.pin_8, R.drawable.pin_9,
                R.drawable.sou_1, R.drawable.sou_2, R.drawable.sou_3, R.drawable.sou_4, R.drawable.sou_5, R.drawable.sou_6, R.drawable.sou_7, R.drawable.sou_8, R.drawable.sou_9,
                R.drawable.wind_east, R.drawable.wind_south, R.drawable.wind_north, R.drawable.wind_west, R.drawable.dragon_red, R.drawable.dragon_white, R.drawable.dragon_green,
                R.drawable.debug)

        val row: View = inflater.inflate(R.layout.tile, parent, false)
        parent.setPadding(padding, 0,  padding / 2, padding)
        return HandViewHolder(row, context, 34)
    }

    override fun onBindViewHolder(holder: HandViewHolder, position: Int) {
        holder.iv.layoutParams.width = width
        holder.iv.layoutParams.height = height
        holder.bindTo(tiles[getItem(position)], getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Int> = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldTile: Int, newTile: Int): Boolean {
                return oldTile == newTile
            }
            override fun areContentsTheSame(oldTile: Int, newTile: Int): Boolean {
                return areItemsTheSame(oldTile, newTile)
            }
        }
    }
}