package com.example.riichit

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

internal fun View.setMargin(left: Int, top: Int, right: Int, bottom: Int) {
    val p = this.layoutParams as ConstraintLayout.LayoutParams
    p.setMargins(left, top, right, bottom)
    this.layoutParams = p
}

internal fun min(a: Int, b: Int) : Int {
    if (a > b) return b
    return a
}