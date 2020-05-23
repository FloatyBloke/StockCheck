package com.flangenet.stockcheck.Utilities

import com.flangenet.stockcheck.Controller.CheckList
import com.flangenet.stockcheck.Controller.DBHelper
import com.flangenet.stockcheck.Model.CheckItems
import java.sql.Connection
import java.sql.Statement
import java.util.*
import kotlin.collections.ArrayList

class getStockChecksList: Observable() {

    var jobSuccess = false
    var lstItems = ArrayList<CheckItems>()


    fun getStockChecksListObservable(selectedDate:Date)  {

        jobSuccess = false
        lstItems.clear()


        val db = DBHelper()
        var conn: Connection? = null
        try {
            conn = db.dbConnect()
            // ************lstItems = db.getListOfStockChecks(conn, selectedDate)

            val statement: Statement = conn!!.createStatement()
            var checkSQL = " SELECT type.id, type.name, checks.stock FROM type LEFT JOIN checks ON type.id = checks.typeid ORDER BY id;"
            checkSQL = "SELECT type.id, type.name, counts.date, counts.reccount " +
                    "FROM type LEFT JOIN(SELECT date,typeid, COUNT(*) AS reccount " +
                    "FROM checks WHERE date='${sqlDateFormat.format(selectedDate)}' GROUP BY typeid,date) counts " +
                    "ON type.id = counts.typeid;"

            var rs = statement.executeQuery(checkSQL)

            while(rs.next()) {
                val tList = CheckItems(0,"!",0)
                tList.checkID = rs.getInt(1)
                tList.description = rs.getString(2)
                tList.counter = rs.getInt(4)
                lstItems.add(tList)
                //println("${rs.getInt(1)} : ${rs.getString(2)} : ${rs.getInt(4)}")
            }
            //****************
            jobSuccess = true
        } catch (e:Exception){
            println(e.message)
        } finally {
            conn!!.close()
        }


        var outData = getStockChecksList()
        outData.lstItems = lstItems
        outData.jobSuccess = jobSuccess

        setChanged()
        notifyObservers(outData)
    }
}