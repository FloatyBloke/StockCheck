package com.flangenet.stockcheck.Controller

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception


class DatabaseHelper {
  /*  lateinit var ctx: Context

    private lateinit var isDone = false
    private lateinit var rv : RecyclerView
    private lateinit var query : String
    private lateinit var adapter : Int = 0
    private  var recordCount : Int = 0
    private  var functionType : Int = 0
    private lateinit var records : ArrayList<Any>
    lateinit var connectionClass: ConnectionClass

    inner class SyncData : AsyncTask<String, String, String>(){
        private var message = "No Connection or WIndows Firewall, not enough permissions Error!"
        lateinit var prog : ProgressDialog

        override fun onPreExecute() {
            records.clear()
            recordCount = 0
            prog = ProgressDialog.show(ctx,"Reading Data....","Loading.... Please Wait", true)
            //super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String {
            var myConn = connectionClass?.dbConn()
            if (myConn ==null){
                isDone = false
            } else {
                val statement = myConn!!.createStatement()
                val cursor =statement.executeQuery(query)
                if (cursor != null){
                    while (cursor!!.next()){
                        try {
                            when (functionType) {
                                1 -> records?.add(
                                    Customer(
                                        cursor!!.getInt("CustomerNo"),
                                        cursor!!.getString("CustomerName")
                                    )
                                )
                                2 -> records?.add(
                                    Product(
                                        cursor!!.getInt("ProductID"),
                                        cursor!!.getString("Barcode"),
                                        cursor!!.getString("ProductName1")
                                    )
                                )
                            }
                            recordCount++
                        } catch (ex : Exception){
                            ex.printStackTrace()
                        }

                    }
                    message = "Found $recordCount"
                    isDone = true
                } else {
                    message = "There are no Records"
                    isDone = false
                    
                    
                }
            }
        }

    }*/

}