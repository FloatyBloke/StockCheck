package com.flangenet.stockcheck.Controller

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.flangenet.stockcheck.Adapter.CheckItemsAdapter
import com.flangenet.stockcheck.Model.CheckItems
import com.flangenet.stockcheck.R
import com.flangenet.stockcheck.Test
import com.flangenet.stockcheck.Utilities.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.alert_dialog_with_edittext.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.sql.Connection
import java.util.*


class MainActivity : AppCompatActivity() ,CoroutineScope by MainScope()  {

    private  var conn: Connection? = null
    var lstItems = ArrayList<CheckItems>()
    lateinit var itemsAdapter: CheckItemsAdapter
    val db = DBHelper()
    var selectedDate = Date()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnTest.setOnClickListener { testButton() }
        //btnNewDay.setOnClickListener { testButton2() }
        btnNextWeek.setOnClickListener { changeDate(1) }
        btnPrevWeek.setOnClickListener { changeDate(-1) }
        //getConnection()
        //enableSpinner(false,"Info")
    }

    override fun onResume() {
        super.onResume()
        refreshData()

    }


      fun refreshData() {
         txtToday.text = prettyDateFormat.format(selectedDate)
         recyclerCheckItems.layoutManager = null
         enableSpinner(true,"Loading...")
         var jobSuccess = false
         //runBlocking {
             //val job = launch(Dispatchers.IO) {

          val t = GlobalScope.launch (Dispatchers.IO) {
              conn = db.dbConnect()
              lstItems = db.getChecks(conn,selectedDate)

              conn!!.close()
              jobSuccess = true
          }.invokeOnCompletion {
              println("Out.....................")
              jobSuccess = true
              this@MainActivity.runOnUiThread(java.lang.Runnable {
                  this.refreshDataComplete(jobSuccess)
              })

          }
 /*                try {
                     conn = db.dbConnect()
                     lstItems = db.getChecks(conn,selectedDate)
                     jobSuccess = true
                 } catch(e:Exception) {
                     println(e.message)
                 }
                 finally {
                     conn!!.close()
                 }
                 println("Dropped out of the block")

             //}
             //job.cancelAndJoin()
         refreshDataComplete(jobSuccess)*/

     }

    fun refreshDataComplete(jobSuccess: Boolean){
        if (jobSuccess) {
            enableSpinner(false, "Done")
            refreshDataSuccess()
        } else {
            Toast.makeText(applicationContext,"Connection Failed...",Toast.LENGTH_LONG).show()
        }
    }

    fun refreshDataSuccess(){

        itemsAdapter = CheckItemsAdapter(this, lstItems as ArrayList<CheckItems>) { position,recordCount ->
            // item is clicked
            // refreshSelected(position)
            // println("$position - ${sqlDateFormat.format(selectedDate)}")

            // Create a new zeroed stock check if none exists

            if (recordCount == 0){
                enableSpinner(true,"Please wait......")
                println("I'm making a new one $position")

                    //conn = db.dbConnect()
                    //db.createBlankStockCheck(conn, position, selectedDate)
                    //conn!!.close()

                //runBlocking {
                    val job = launch{
                        try {
                            //Do Work
                            conn = db.dbConnect()
                            db.createBlankStockCheck(conn, position, selectedDate)

                        } finally {
                            // cleanup if cancelled
                            conn!!.close()
                        }
                    }
                    //job.cancelAndJoin()
                    println("I'm Back")
                //}
                enableSpinner(false,"Please wait......")
            }

            val checkListIntent = Intent(this,CheckList::class.java)
            checkListIntent.putExtra(EXTRA_CHECKLIST_TYPE,position)
            checkListIntent.putExtra(EXTRA_CHECKLIST_DESC,lstItems[position-1].description)
            //println(lstItems[position-1].description)
            checkListIntent.putExtra(EXTRA_CHECKLIST_DATE,sqlDateFormat.format(selectedDate))
            startActivity(checkListIntent)
        }

        recyclerCheckItems.adapter = itemsAdapter
        val layoutManager = LinearLayoutManager(this)
        recyclerCheckItems.layoutManager = layoutManager

    }


    fun testButton() {
        val testIntent = Intent(this, Test::class.java)
        //testIntent
        startActivity(testIntent)
        //println(showDialog("Arse Buckets"))
 /*       val testIntent = Intent(this, Test::class.java)
        startActivity(testIntent)
*/

        //showDialog("Hello")
        //withEditText()

    }

    private fun showDialog(title: String) : String{
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.popup_test)
        val body = dialog .findViewById(R.id.txtBody) as TextView
        body.text = title
        val yesBtn = dialog .findViewById(R.id.yesBtn) as Button
        val noBtn = dialog .findViewById(R.id.noBtn) as Button
        yesBtn.setOnClickListener {
            doSummatLater()
            dialog.dismiss() }
        noBtn.setOnClickListener { dialog.dismiss() }

        dialog.show()
        println("pPPPPPPPpPPPPPpppPLLLLlllLlLll")
        return "Yes"


    }

    fun doSummatLater(){
        Toast.makeText(this,"Doing summat later",Toast.LENGTH_LONG).show()
    }

    fun testButton2() {
/*        val checkListIntent = Intent(this,CheckList::class.java)
        checkListIntent.putExtra(EXTRA_CHECKLIST_TYPE,2)
        checkListIntent.putExtra(EXTRA_CHECKLIST_DATE,sqlDateFormat.format(selectedDate))
        startActivity(checkListIntent)*/
        //executeMySQLQuery()

        //t?.close()


    }
    fun changeDate(days:Int){
        val c = Calendar.getInstance()
        c.time = selectedDate
        c.add(Calendar.DATE, days)
        selectedDate = c.time
        refreshData()
    }

    private fun enableSpinner(enable: Boolean, info: String) {
        txtInfo.text = info
        if (enable) {
            cvProgress.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            txtInfo.visibility = View.VISIBLE
        } else {
            cvProgress.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
            txtInfo.visibility = View.INVISIBLE
        }

        //btnImport.isEnabled = !enable
        //btnExport.isEnabled = !enable
        //logText.isEnabled = !enable
        //hideKeyboard()
    }


    fun withEditText() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("With EditText")
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_with_edittext, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
        builder.setView(dialogLayout)
        builder.setCancelable(false)
        builder.setPositiveButton("OK") { dialogInterface, i -> Toast.makeText(applicationContext, "EditText is " + editText.text.toString(), Toast.LENGTH_SHORT).show() }
        builder.show()
    }




}
