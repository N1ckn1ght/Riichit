package com.example.riichit

import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.riichit.LocaleHelper.setLocale
import com.example.riichit.Ruleset.achievements
import java.lang.Integer.min


class AchievementsActivity : AppCompatActivity() {
    private val context = this
    private var toast: Toast? = null
    private var achievementsList = MutableList(achievements.size) { 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        setLocale(context)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)
        supportActionBar?.hide()

        val displayMetrics = DisplayMetrics()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = context.display
            @Suppress("DEPRECATION")
            display?.getMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = context.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(displayMetrics)
        }
        val padding = (displayMetrics.widthPixels * 0.016).toInt()
        val ivHeight = min(
            (displayMetrics.widthPixels * 1.248 / 3).toInt(),
            ((displayMetrics.heightPixels - 4 * padding) / 2)
        )
        val ivWidth = ivHeight * 4 / 3

        val rvGallery = findViewById<RecyclerView>(R.id.rvGallery)
        rvGallery.layoutManager = GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
        rvGallery.addItemDecoration(GridSpacingItemDecoration(3, padding, true, 0, false))
        val galleryAdapter = GalleryAdapter(
            LayoutInflater.from(context),
            this::onClick,
            ivWidth,
            ivHeight,
            padding
        )

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        for ((i, achievement) in achievements.withIndex()) {
            if (sharedPreferences.getBoolean(achievement, false)) {
                achievementsList[i] = i + 1
            }
        }

        galleryAdapter.submitList(achievementsList)
        rvGallery.adapter = galleryAdapter
    }

    override fun onPause() {
        toast?.cancel()
        super.onPause()
    }

    private fun onClick(id: Int) {
        showLongToast(
            context.getString(
                context.resources.getIdentifier(
                    "achievement_$id",
                    "string",
                    context.packageName
                )
            )
        )
    }

    private fun showLongToast(text: String) {
        toast?.cancel()

        val centeredText: Spannable = SpannableString(text)
        centeredText.setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
            0, text.length - 1,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )

        toast = Toast.makeText(
            baseContext,
            centeredText,
            Toast.LENGTH_LONG
        )
        toast?.show()
    }
}