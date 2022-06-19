package com.example.riichit

import android.annotation.SuppressLint
import android.content.Context
import androidx.preference.PreferenceManager
import java.util.*

object LocaleHelper {
    @SuppressLint("ApplySharedPref")
    fun changeLocale(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString("lang", getNextLocale(getLocale(context))).commit()
    }

    fun setLocale(context: Context) {
        val res = context.resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.setLocale(Locale(getLocale(context).lowercase()))
        @Suppress("DEPRECATION")
        res.updateConfiguration(conf, dm)
    }

    fun getLocale(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("lang", "en")
            .toString()
    }

    private fun getNextLocale(locale: String): String {
        val nextLocale = when (locale) {
            "en" -> "ru"
            "ru" -> "en"
            else -> "en"
        }
        return nextLocale
    }
}