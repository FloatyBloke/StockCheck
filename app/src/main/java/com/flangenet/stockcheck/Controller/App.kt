package com.flangenet.stockcheck.Controller

import android.app.Application
import com.flangenet.stockcheck.Utilities.SharedPrefs

class App : Application() {

    companion object{
        lateinit var prefs: SharedPrefs
    }

    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}