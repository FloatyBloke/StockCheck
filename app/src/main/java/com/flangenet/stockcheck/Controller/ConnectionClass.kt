package com.flangenet.stockcheck.Controller

import android.os.StrictMode
import android.util.Log
import com.flangenet.stockcheck.Model.CheckItems
import com.flangenet.stockcheck.Model.StockCheck
import com.flangenet.stockcheck.Model.StockItem
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

    fun getStockCheck(conn: Connection?, checkType:Int): ArrayList<StockCheck>{
        val listStockCheck = ArrayList<StockCheck>()

        val statement: Statement = conn!!.createStatement()
        val checkSQL = "SELECT checkitems.displayorder, product.id, product.name " +
                "FROM checkitems INNER JOIN product ON checkitems.itemid=product.id " +
                "WHERE checkitems.type=$checkType ORDER BY checkitems.displayorder;"

        //var rs = statement.executeQuery("select * from product")
        var rs = statement.executeQuery(checkSQL)
        var displayOrder : Int = 0
        while(rs.next()) {
            val tList = StockCheck(0,0,"t",0F,false)


            tList.productId = rs.getInt(2)
            tList.displayOrder = rs.getInt(1)
            tList.description = rs.getString(3)
            //tList.stock = rs.getFloat(3)
            tList.stock=0f
            tList.selected = false
            listStockCheck.add(tList)
            println("${displayOrder} : ${rs.getInt(1)} : ${rs.getString(2)}")
            displayOrder += 1
        }
        return listStockCheck

    }

    fun getChecks(conn: Connection?, checkDate:Date): ArrayList<CheckItems>{
        val listChecks = ArrayList<CheckItems>()

        val statement: Statement = conn!!.createStatement()
        val checkSQL = "SELECT * FROM type ORDER BY id;"

        var rs = statement.executeQuery(checkSQL)

        while(rs.next()) {
            val tList = CheckItems(0,"!",0)
            tList.dateID = rs.getInt(1)
            tList.description = rs.getString(2)
            tList.counter = 1
            listChecks.add(tList)
            println("${rs.getInt(1)} : ${rs.getString(2)}")

        }

        return listChecks

    }

}