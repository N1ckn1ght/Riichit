package com.example.riichit

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.riichit.Utility.not

class MainActivity : AppCompatActivity() {
    // TODO: add RU translation
    private var ema = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
    }

    private fun startGame(mode: Int) {
        val intent = Intent(this, SoloActivity::class.java)
        intent.putExtra("mode", mode)
        intent.putExtra("ema", ema)
        startActivity(intent)
    }

    fun onClickSoloMahjong(view: View) {
        startGame(0)
    }

    fun onClickSoloMan(view: View) {
        startGame(1)
    }

    fun onClickEma(view: View) {
        ema = !ema
    }
}