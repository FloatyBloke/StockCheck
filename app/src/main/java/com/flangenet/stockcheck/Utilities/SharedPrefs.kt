package com.flangenet.stockcheck.Utilities

import android.content.Context
import android.content.SharedPreferences

class SharedPrefs(context: Context) {

    val CONNECT_IP = "connect ip"
    val CONNECT_DB = "connect db"
    val CONNECT_USER = "connect user"
    val CONNECT_PASSWORD = "connect password"

    val PREFS_FILENAME = "prefsStockCheck"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME,0)

    var connectIP: String?
        get() = prefs.getString(CONNECT_IP,"192.168.1.151")
        set(value) = prefs.edit().putString(CONNECT_IP,value).apply()

    var connectDB: String?
        get() = prefs.getString(CONNECT_DB,"stockchecks")
        set(value) = prefs.edit().putString(CONNECT_IP,value).apply()

    var connectUser: String?
        get() = prefs.getString(CONNECT_USER,"god3")
        set(value) = prefs.edit().putString(CONNECT_USER,value).apply()

    var connectPassword: String?
        get() = prefs.getString(CONNECT_PASSWORD,"password")
        set(value) = prefs.edit().putString(CONNECT_PASSWORD,value).apply()

}
