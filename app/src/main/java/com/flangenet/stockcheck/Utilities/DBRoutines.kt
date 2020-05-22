package com.flangenet.stockcheck.Utilities

import com.flangenet.stockcheck.Controller.DBHelper
import com.flangenet.stockcheck.Model.CheckItems
import java.sql.Connection
import java.util.*

suspend fun getChecks3(selectedDate:Date) : ArrayList<CheckItems> {
    lateinit var lstItems: ArrayList<CheckItems>
    var conn: Connection? = null
    val db = DBHelper()

    conn = db.dbConnect()
    lstItems = db.getChecks(conn,selectedDate)
    conn!!.close()

    return lstItems
}