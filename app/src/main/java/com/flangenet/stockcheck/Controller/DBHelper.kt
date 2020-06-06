package com.flangenet.stockcheck.Controller

import android.content.Context
import android.os.StrictMode
import android.util.Log
import androidx.preference.PreferenceManager
import com.flangenet.stockcheck.Model.CheckItems
import com.flangenet.stockcheck.Model.StockCheck
import com.flangenet.stockcheck.Model.ChecksDB
import com.flangenet.stockcheck.Utilities.sqlDateFormat
import java.sql.*
import java.util.Date
import kotlin.collections.ArrayList

class DBHelper(context: Context) {
/*    private val ip = App.prefs.connectIP
    private val db = App.prefs.connectDB
    private val username = App.prefs.connectUser
    private val password = App.prefs.connectPassword*/

    private var ip = PreferenceManager.getDefaultSharedPreferences(context).getString("ip", null)
    private var db = PreferenceManager.getDefaultSharedPreferences(context).getString("db", null)
    private var username = PreferenceManager.getDefaultSharedPreferences(context).getString("user", null)
    private var password = PreferenceManager.getDefaultSharedPreferences(context).getString("password", null)


    fun dbConnect () : Connection? {

/*        ip = "bgz3cg3qm8wkdi24bdle-mysql.services.clever-cloud.com"
        db = "bgz3cg3qm8wkdi24bdle"
        username = "ug3ryuiyhm4kue7r"
        password = "5gI3lKlnDLLgYQsPzIzl"*/

        var conn : Connection? = null
        try {
            Log.e("ASK", "Connection Setup")
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().
            penaltyDialog().penaltyLog().build()
            StrictMode.setThreadPolicy(policy)
            //val connURL = "jdbc:mysql://$ip:3306/$db?characterEncoding=utf8"
            //val connURL = "jdbc:mysql://bgz3cg3qm8wkdi24bdle-mysql.services.clever-cloud.com:3306/bgz3cg3qm8wkdi24bdle"
            val connURL = "jdbc:mysql://$ip:3306/$db?characterEncoding=utf8"
            Class.forName("com.mysql.jdbc.Driver").newInstance()
            Log.e("ASK", "Connection Called")
            DriverManager.setLoginTimeout(5)
            conn = DriverManager.getConnection(connURL,username,password)
            //conn = DriverManager.getConnection(connURL,"ug3ryuiyhm4kue7r","5gI3lKlnDLLgYQsPzIzl")

        } catch (ex : SQLException) {
            Log.e("SQL Error : ", ex.message)
        } catch (ex1 : ClassNotFoundException) {
            Log.e("Class Error : ", ex1.message)
        }  catch (ex2 : Exception) {
            Log.e("Error : ", ex2.message)
        }
        Log.d("Connection","$conn")
        return conn


    }

    suspend fun dbConnectCheck () : String {

        var logInfo: String = "Connection Setup"
        var conn : Connection? = null
        var connString : String? = null

        try {
            Log.e("ASK", "Connection Setup")
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val connURL = "jdbc:mysql://$ip:3306/$db"
            Class.forName("com.mysql.jdbc.Driver").newInstance()

            conn = DriverManager.getConnection(connURL,username,password)
            Log.e("ASK", "Connection Made")
            logInfo = "Connection Made"

        } catch (ex : SQLException) {
            Log.e("SQL Error : ", ex.message)
            logInfo = ex.message.toString()
        } catch (ex1 : ClassNotFoundException) {
            Log.e("Class Error : ", ex1.message)
            logInfo = ex1.message.toString()
        } catch (ex2 : Exception) {
            Log.e("Error : ", ex2.message)
            logInfo = ex2.message.toString()
        }
        return logInfo


    }
    fun createBlankStockCheck(conn: Connection?, checkType:Int, selectedDate: Date){
        val listStockCheck = ArrayList<ChecksDB>()
        val statement: Statement = conn!!.createStatement()

        // Import new items for specified set into array

        var checkSQL = "SELECT * FROM checkitems WHERE type=$checkType ORDER BY displayorder;"
        var rs = statement.executeQuery(checkSQL)
        var resultSQL: Boolean
        println(checkSQL)
        while(rs.next()) {
            val tList = ChecksDB(0,Date(),checkType,0,0F)
            //tList.id = null
            tList.date = selectedDate
            tList.typeID = rs.getInt(2)
            tList.itemID = rs.getInt(4)
            tList.stock = 0f
            listStockCheck.add(tList)
            println("${tList.date} : ${tList.typeID} : ${tList.itemID} : ${tList.stock}")
        }

        // Take array and UPDATE checks with new zero stock check

        var insertSQL = StringBuilder()
        insertSQL.append("INSERT INTO checks (date, typeid, itemid, stock) VALUES ")
        listStockCheck.forEach {
             insertSQL.append("('${sqlDateFormat.format(it.date)}', '${it.typeID}', '${it.itemID}', '${it.stock}'),")
        }
        var t = insertSQL.toString()
        var t2 = t.substring(0,t.length-1) +";"
        statement.execute(t2)

    }

    fun dateToSQLDate(javaDate: Date) : java.sql.Date{
        return java.sql.Date(javaDate.time)
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

        checkSQL = "SELECT checkitems.type, checkitems.displayorder, checkitems.itemid, selchecks.name,selchecks.stock, checksid " +
                "FROM checkitems " +
                "LEFT JOIN" +
                "(SELECT date, typeid, itemid,product.name AS name, stock, checks.id AS checksid FROM checks INNER JOIN product ON checks.itemid=product.id WHERE typeid=$checkType AND date='$selectedDateText') selchecks " +
                "ON checkitems.itemid = selchecks.itemid " +
                "WHERE checkitems.type=$checkType"


        var rs = statement.executeQuery(checkSQL)

        var displayOrder : Int = 0
        while(rs.next()) {
            val tList = StockCheck(0,0,0,"t",0F,0F,false)

            tList.displayOrder = rs.getInt(2)
            tList.productId = rs.getInt(3)
            if (rs.getString(4) == null){
                tList.description = "?????"
            } else {
                tList.description = rs.getString(4)
            }

            tList.stock = rs.getFloat(5)
            tList.oldStock = tList.stock
            //tList.stock=0f
            tList.selected = false
            tList.checkID = rs.getInt(6)
            listStockCheck.add(tList)
            //println("${displayOrder} : ${rs.getInt(1)} : ${rs.getString(2)}")
            displayOrder += 1
        }
        return listStockCheck

    }

    fun getListOfStockChecks(conn: Connection?, selectedDate:Date): ArrayList<CheckItems>  {
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



    fun updateStockCheckItem(conn: Connection, checkID:Int, checkStock: Float){
/*        val statement: Statement = conn!!.createStatement()
        var checkSQL = "UPDATE checks SET stock='$checkStock' WHERE id=$checkID;"
        statement.execute(checkSQL)
        */

        val ps: PreparedStatement = conn.prepareStatement("UPDATE checks SET stock=$checkStock WHERE id=$checkID")
        var i = ps.executeUpdate()
    }




}



