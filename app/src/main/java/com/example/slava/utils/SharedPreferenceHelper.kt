package com.example.slava.utils

import android.content.Context

class SharedPreferenceHelper(private val context: Context) {

    companion object{
        private const val MY_PREF_KEY = "myPref"
    }

    fun saveStringData(key: String,data: String?) {
        val sharedPreferences = context.getSharedPreferences(MY_PREF_KEY, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(key,data).apply()
    }

    fun saveBoolData(key: String,data: Boolean) {
        val sharedPreferences = context.getSharedPreferences(MY_PREF_KEY, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(key, data).apply()
    }

    fun getStringData(key: String): String? {
        val sharedPreferences = context.getSharedPreferences(MY_PREF_KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)
    }

    fun getBoolData(key: String): Boolean {
        val sharedPreferences = context.getSharedPreferences(MY_PREF_KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, false)
    }
}