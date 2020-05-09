package com.flangenet.stockcheck.Controller

import android.os.StrictMode
import android.util.Log
import com.flangenet.stockcheck.Model.CheckItems
import com.flangenet.stockcheck.Model.StockCheck
import com.flangenet.stockcheck.Utilities.sqlDateFormat
import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import kotlin.collections.ArrayList

class ConnectionClass {
    private val ip = "192.168.1.151"
    private val db = "stockchecks"
    private val username = "god3"
    private val password = "password"


    fun dbConnect () : Connection? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var conn : Connection? = null
        var connString : String? = null
        try {
            Log.e("ASK", "Connection Setup")
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val connURL = "jdbc:mysql://$ip:3306/$db"
            Class.forName("com.mysql.jdbc.Driver").newInstance()
            conn = DriverManager.getConnection(connURL,username,password)
            Log.e("ASK", "Connection Called")

        } catch (ex : SQLException) {
            Log.e("SQL Error : ", ex.message)
        } catch (ex1 : ClassNotFoundException) {
            Log.e("Class Error : ", ex1.message)
        }  catch (ex2 : Exception) {
            Log.e("Error : ", ex2.message)
        }
        return conn


    }

    fun getStockCheck(conn: Connection?, checkType:Int, selectedDateText: String): ArrayList<StockCheck>{
        val listStockCheck = ArrayList<StockCheck>()

        val statement: Statement = conn!!.createStatement()
        var checkSQL = "SELECT checkitems.displayorder, product.id, product.name " +
                "FROM checkitems INNER JOIN product ON checkitems.itemid=product.id " +
                "WHERE checkitems.type=$checkType ORDER BY checkitems.displayorder;"

        checkSQL = "SELECT checkitems.type, checkitems.displayorder, checkitems.itemid, selchecks.stock " +
                "FROM checkitems " +
                "LEFT JOIN" +
                "(SELECT  date, typeid, itemid, stock FROM checks WHERE typeid=$checkType AND date='$selectedDateText') selchecks " +
                "ON checkitems.id = selchecks.itemid " +
                "WHERE checkitems.type=$checkType"

        checkSQL = "SELECT checkitems.type, checkitems.displayorder, checkitems.itemid, selchecks.name,selchecks.stock " +
                "FROM checkitems " +
                "LEFT JOIN" +
                "(SELECT date, typeid, itemid,product.name AS name, stock FROM checks INNER JOIN product ON checks.itemid=product.id WHERE typeid=$checkType AND date='$selectedDateText') selchecks " +
                "ON checkitems.itemid = selchecks.itemid " +
                "WHERE checkitems.type=$checkType"


        //var rs = statement.executeQuery("select * from product")
        var rs = statement.executeQuery(checkSQL)
        println(checkSQL)
        var displayOrder : Int = 0
        while(rs.next()) {
            val tList = StockCheck(0,0,"t",0F,false)

            tList.displayOrder = rs.getInt(2)
            tList.productId = rs.getInt(3)
            if (rs.getString(4) == null){
                tList.description = "?????"
            } else {
                tList.description = rs.getString(4)
            }

            tList.stock = rs.getFloat(5)
            //tList.stock=0f
            tList.selected = false
            listStockCheck.add(tList)
            //println("${displayOrder} : ${rs.getInt(1)} : ${rs.getString(2)}")
            displayOrder += 1
        }
        return listStockCheck

    }

    fun getChecks(conn: Connection?, selectedDate:Date): ArrayList<CheckItems>{
        val listChecks = ArrayList<CheckItems>()

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
            listChecks.add(tList)
            //println("${rs.getInt(1)} : ${rs.getString(2)}")

        }

        return listChecks

    }

}