package com.example.amorient.util

import android.content.Context
import io.paperdb.Book
import io.paperdb.Paper

class AmorientPreferences(context: Context) {

    private val paper: Book

    init {
        Paper.init(context)
        paper = Paper.book()
    }

    private val sharedPref by lazy {
        context.getSharedPreferences("pref", Context.MODE_PRIVATE)
    }

    fun set(key: String, value: Any?) {
        val editor = sharedPref.edit()

        when (value) {
            is Boolean -> editor.putBoolean(key, value)
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is Double -> editor.putFloat(key, value.toFloat())
            else -> paper.write(key, value)
        }

        editor.commit()
    }

    fun <T> get(key: String): T? {
        return paper.read(key)
    }

    fun removeObject(key: String) {
        paper.delete(key)
    }

    fun getString(key: String, default: String?): String? {
        return sharedPref.getString(key, default)
    }

    fun getInt(key: String, default: Int): Int {
        return sharedPref.getInt(key, default)
    }

    fun getLong(key: String, default: Long): Long {
        return sharedPref.getLong(key, default)
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return sharedPref.getBoolean(key, default)
    }

    fun getFloat(key: String, default: Float): Float {
        return sharedPref.getFloat(key, default)
    }

    fun clean() {
        paper.destroy()
        sharedPref.edit().apply {
            clear()
            commit()
        }
    }

}