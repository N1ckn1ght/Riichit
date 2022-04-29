package com.example.riichit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
    }

    fun onClickSoloMahjong(view: View) { startGame(0) }
    fun onClickSoloMan(view: View) { startGame(1) }
    private fun startGame(mode: Int) {
        val intent = Intent(this, SoloActivity::class.java)
        intent.putExtra("mode", mode)
        startActivity(intent)
    }
    // TODO: add RU translation
}