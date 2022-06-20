package com.example.riichit

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

object Utility {
    const val logicIncrement = 256

    fun View.setMargin(left: Int, top: Int, right: Int, bottom: Int) {
        val p = this.layoutParams as ConstraintLayout.LayoutParams
        p.setMargins(left, top, right, bottom)
        this.layoutParams = p
    }

    fun Boolean.toInt(): Int {
        if (this) return 1
        return 0
    }

    operator fun Int.not(): Int {
        if (this > 0) return 0
        return 1
    }

    fun <T> isEqual(first: List<T>, second: List<T>): Boolean {
        if (first.size != second.size) {
            return false
        }
        return first.zip(second).all { (x, y) -> x == y }
    }
}