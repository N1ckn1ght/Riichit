package com.example.riichit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.riichit.AppDatabase.Companion.instance
import com.example.riichit.Drawables.flag
import com.example.riichit.LocaleHelper.changeLocale
import com.example.riichit.LocaleHelper.getLocale
import com.example.riichit.LocaleHelper.setLocale
import com.example.riichit.Utility.toInt
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val context = this
    private lateinit var db: AppDatabase

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setLocale(context)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val buttonSoloMahjong = findViewById<Button>(R.id.btnSoloMahjong)
        val buttonSoloMan = findViewById<Button>(R.id.btnSoloMan)
        val buttonSettings = findViewById<Button>(R.id.btnSettings)
        val buttonRecords = findViewById<Button>(R.id.btnRecords)
        val buttonTutorial = findViewById<Button>(R.id.btnTutorial)
        val buttonAchievements = findViewById<Button>(R.id.btnAchievements)

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
            val intent = Intent(this, RecordsActivity::class.java)
            startActivity(intent)
        }
        buttonTutorial.setOnClickListener {
            val intent = Intent(this, TutorialActivity::class.java)
            startActivity(intent)
        }
        buttonAchievements.setOnClickListener{
            val intent = Intent(this, AchievementsActivity::class.java)
            startActivity(intent)
        }

        // language icon shall be changed upon a preferred language
        val imageViewLang = findViewById<ImageView>(R.id.ivLang)
        flag[getLocale(context)]?.let { imageViewLang.setImageResource(it) }
        imageViewLang.setOnClickListener {
            changeLocale(context)
            recreate()
        }

        // create profile #0 if not exists
        db = instance(this)
        val profile = 1

        GlobalScope.launch(Dispatchers.IO) {
            Operations.createProfile(db, profile)
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