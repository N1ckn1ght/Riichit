package com.example.riichit

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.riichit.AppDatabase.Companion.instance
import com.example.riichit.Operations.getBalance
import com.example.riichit.Operations.getStreak
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RecordsActivity : AppCompatActivity() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleHelper.setLocale(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)
        supportActionBar?.hide()

        val db = instance(this)
        val profile = 1

        val tvProfile = findViewById<TextView>(R.id.row_0_head)
        val tvMahjongBalance = findViewById<TextView>(R.id.row_2_first)
        val tvManBalance = findViewById<TextView>(R.id.row_2_second)
        val tvMahjongStreak = findViewById<TextView>(R.id.row_3_first)
        val tvManStreak = findViewById<TextView>(R.id.row_3_second)

        GlobalScope.launch(Dispatchers.IO) {
            val profileName = getString(R.string.profile, profile)
            val mahjongBalance = getBalance(db, profile, 0)
            val manBalance = getBalance(db, profile, 1)
            val mahjongStreak = getStreak(db, profile, 0)
            val manStreak = getStreak(db, profile, 1)

            this@RecordsActivity.runOnUiThread {
                tvProfile.text = profileName
                tvMahjongBalance.text = mahjongBalance.toString()
                tvManBalance.text = manBalance.toString()
                tvMahjongStreak.text = mahjongStreak.toString()
                tvManStreak.text = manStreak.toString()
            }
        }

        val btnBack = findViewById<Button>(R.id.row_4_exit)
        btnBack.setOnClickListener {
            finish()
        }
    }
}