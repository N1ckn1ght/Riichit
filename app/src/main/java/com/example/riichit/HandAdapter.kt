package com.example.riichit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

internal class HandAdapter(private val inflater: LayoutInflater) : ListAdapter<Int, HandViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HandViewHolder {
        val row: View = inflater.inflate(R.layout.tile, parent, false)
        return HandViewHolder(row)
    }

    override fun onBindViewHolder(holder: HandViewHolder, position: Int) {
        holder.bindTo(getItem(position))
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
