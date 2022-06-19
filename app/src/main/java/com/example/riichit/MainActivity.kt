package com.example.riichit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.riichit.Utility.toInt

class MainActivity : AppCompatActivity() {
    // TODO: make RU translation for strings!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val buttonSoloMahjong = findViewById<Button>(R.id.btnSoloMahjong)
        val buttonSoloMan = findViewById<Button>(R.id.btnSoloMan)
        val buttonSettings = findViewById<Button>(R.id.btnSettings)
        val buttonRecords = findViewById<Button>(R.id.btnRecords)
        val buttonTutorial = findViewById<Button>(R.id.btnTutorial)

        buttonSoloMahjong.setOnClickListener {
            startGame(0)
        }
        buttonSoloMan.setOnClickListener {
            startGame(1)
        }
        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        buttonRecords.setOnClickListener {
            // TODO: show records (use ROOM database)
        }
        buttonTutorial.setOnClickListener {
            // TODO: move to tutorial activity
        }
    }

    private fun startGame(mode: Int) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val intent = Intent(this, SoloActivity::class.java)
        intent.putExtra("mode", mode)
        intent.putExtra("ema", sharedPreferences.getBoolean("ema_names", false).toInt())
        startActivity(intent)
    }
}