package com.example.riichit

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.riichit.Drawables.tutorial

class TutorialActivity : AppCompatActivity() {
    private val context = this
    private lateinit var ivScreen: ImageView
    private lateinit var tvTutorial: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
        supportActionBar?.hide()

        var currentSlide = 0

        ivScreen = findViewById(R.id.ivScreen)
        tvTutorial = findViewById(R.id.tvTutorial)
        val buttonLeft = findViewById<Button>(R.id.btnLeft)
        val buttonRight = findViewById<Button>(R.id.btnRight)

        buttonLeft.setOnClickListener {
            currentSlide -= 1
            draw(currentSlide)
        }
        buttonRight.setOnClickListener {
            currentSlide += 1
            draw(currentSlide)
        }

        draw(currentSlide)
    }

    private fun draw(slide: Int) {
        // The point is to show text and image distinctively. while it's possible to make a better
        // slideshow reader, this may be unnecessary due to only two languages in-game; however,
        // this may backfire badly in case of future Riichit expansion. Also even the current
        // solution would imply that there need to be image with outlined/outshadowed text on top.
        // TODO: make a better reader
        val const = tutorial.size / 2
        val tvMap = arrayOf(
            1,
            2,
            0,
            3,
            0,
            4,
            5,
            0,
            6,
            7,
            0,
            8,
            9,
            0,
            10,
            11,
            12,
            13,
            14,
            0,
            15,
            16,
            17,
            18,
            0,
            19,
            20,
            21,
            22,
            23
        )
        val ivMap = arrayOf(
            0,
            1 + const,
            1,
            2 + const,
            2,
            2 + const,
            3 + const,
            3,
            3 + const,
            4 + const,
            4,
            5 + const,
            5 + const,
            5,
            5 + const,
            5 + const,
            0,
            0,
            6 + const,
            6,
            6 + const,
            6 + const,
            0,
            7 + const,
            7,
            7 + const,
            7 + const,
            7 + const,
            0,
            0
        )
        if (slide < 0 || slide > tvMap.size - 1) {
            finish()
        } else {
            tvTutorial.text = context.getString(
                context.resources.getIdentifier(
                    "tutorial_${tvMap[slide]}",
                    "string",
                    context.packageName
                )
            )
            ivScreen.setImageResource(tutorial[ivMap[slide]])
        }
    }
}