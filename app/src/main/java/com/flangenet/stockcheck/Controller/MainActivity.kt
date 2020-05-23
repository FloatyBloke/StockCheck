package com.flangenet.stockcheck.Controller

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.flangenet.stockcheck.Adapter.CheckItemsAdapter
import com.flangenet.stockcheck.Model.CheckItems
import com.flangenet.stockcheck.Model.StockCheck
import com.flangenet.stockcheck.R
import com.flangenet.stockcheck.Utilities.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.progress_bit.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.sql.Connection
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() ,CoroutineScope by MainScope()  {


    var lstItems = ArrayList<CheckItems>()
    lateinit var itemsAdapter: CheckItemsAdapter
    val bob = getStockChecksList()
    var selectedDate = Date()
    var jobSuccess = false
    var dlg: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnTest.setOnClickListener { testButton() }
        //btnNewDay.setOnClickListener { testButton2() }
        btnNextWeek.setOnClickListener { changeDate(1) }
        btnPrevWeek.setOnClickListener { changeDate(-1) }

        bob.addObserver(GetChecksListObserver())

    }
    inner class GetChecksListObserver : Observer {

        override fun update(o: Observable?, arg: Any?) {
            val bob : getStockChecksList = arg as getStockChecksList
            println("I'm back from something ${bob.lstItems} - ${bob.jobSuccess}")
            enableSpinner(false,"Dood it :)")

            if (bob.jobSuccess) {
                lstItems = bob.lstItems
                refreshDataPart2()
            } else {
                Toast.makeText(applicationContext, "Connection Failed...", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableSpinner(false,"Starting off busy")
        refreshData()
    }



    fun refreshData() {
        txtToday.text = prettyDateFormat.format(selectedDate)
        recyclerCheckItems.layoutManager = null
        this@MainActivity.runOnUiThread {dlg = showDialog("Arse Buckets")}
        this@MainActivity.runOnUiThread { enableSpinner(true, "Loading stock check list....") }
        bob.getStockChecksListObservable(selectedDate)



        //val dlg = showDialog("Loading stock checks....")

/*        this@MainActivity.runOnUiThread { enableSpinner(true, "Loading stock check list....") }
        val (tempItems, jobSuccess2) = getStockChecksList(selectedDate)
        println("${tempItems.count()} - $jobSuccess")
        lstItems = tempItems
        if (jobSuccess2) {
            lstItems = tempItems
        } else {
            Toast.makeText(applicationContext, "Connection Failed...", Toast.LENGTH_LONG).show()
        }
        this@MainActivity.runOnUiThread {
            enableSpinner(
                false,
                "Loading stock check list complete...."
            )
        }
        //dlg.dismiss()
        refreshDataPart2()*/
    }


    fun refreshDataPart2(){


        itemsAdapter = CheckItemsAdapter(this, lstItems as ArrayList<CheckItems>) { position,recordCount ->
            // item is clicked
            // Create a new zeroed stock check if none exists
            if (recordCount == 0){
                //enableSpinner(true,"Creating new blank stock check ... ")
                recyclerCheckItems.layoutManager = null
                println("I'm making a new one $position")
                //val dlg = showDialog("Creating blank stock check....")
                val t = launch (Dispatchers.IO) {
                    this@MainActivity.runOnUiThread{enableSpinner(true,"Creating new blank stock check....")}
                    createBlankStockCheck2(position, selectedDate)
                }.invokeOnCompletion {
                    println("Out.....................")
                    //dlg.dismiss()
                    this@MainActivity.runOnUiThread(java.lang.Runnable {this.createBlankStockCheckComplete(position)})
                }

            } else {
                val dlg= showDialog("Opening Stock Check")
                openStockCheck(position)
                dlg.dismiss()
            }


        }

        recyclerCheckItems.adapter = itemsAdapter
        val layoutManager = LinearLayoutManager(this)
        recyclerCheckItems.layoutManager = layoutManager

        dlg?.dismiss()
    }

    fun createBlankStockCheckComplete(position: Int){
        if (jobSuccess){
            enableSpinner(false,"Back from creating new blank stock check - Success")
            openStockCheck(position)
        } else {
            enableSpinner(false,"Back from creating new blank stock check - Failure")
            Toast.makeText(this,"Error creating new blank Stock check", Toast.LENGTH_LONG).show()
        }
    }

    fun openStockCheck(position : Int){
        var lstCheck = ArrayList<StockCheck>()

        val checkListIntent = Intent(this,CheckList::class.java)
        checkListIntent.putExtra(EXTRA_CHECKLIST_TYPE,position)
        checkListIntent.putExtra(EXTRA_CHECKLIST_DESC,lstItems[position-1].description)
        //println(lstItems[position-1].description)
        checkListIntent.putExtra(EXTRA_CHECKLIST_DATE,sqlDateFormat.format(selectedDate))

        lstCheck = getStockCheck(position, selectedDate)
        checkListIntent.putParcelableArrayListExtra(EXTRA_CHECK_ARRAY, lstCheck)
        //startActivity(checkListIntent)
        startActivityForResult(checkListIntent,PASS_ME_A_STOCK_CHECK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("TRACKING","About to receive result :)")
        // Check that it is the SecondActivity with an OK result
        if (requestCode == PASS_ME_A_STOCK_CHECK) {
            if (resultCode == Activity.RESULT_OK) {

                // Get String data from Intent
                val returnArray = data!!.getParcelableArrayListExtra<StockCheck>("keyName")

                // Process returned data
                // println("$returnArray")
                returnArray.forEach {
                    println("${it.checkID} - ${it.description} - ${it.stock}")
                }
                val db = DBHelper()
                val conn = db.dbConnect()
                db.updateStockCheck(conn!!, returnArray)
                conn!!.close()
            }
        }
    }


    fun testButton() {
        //showDialog("Loading stock check list...")

        this@MainActivity.runOnUiThread {dlg = showDialog("Arse Buckets")}
        this@MainActivity.runOnUiThread { enableSpinner(true, "Loading stock check list....") }
    }




    private fun showDialog(title: String) : Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.progress_popup)
        val info = dialog.findViewById(R.id.txtInfo) as TextView
        info.text = title

        val btnCancel = dialog.findViewById(R.id.btnCancel) as Button
/*        yesBtn.setOnClickListener {
            doSummatLater()
            dialog.dismiss() }*/
        btnCancel.setOnClickListener { dialog.dismiss() }

        Thread.sleep(1000)
        dialog.show()
        println("pPPPPPPPpPPPPPpppPLLLLlllLlLll")
        return dialog
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
        Log.d("Spinner","$enable - $info")
        if (enable) {
            llProgressBar.visibility = View.VISIBLE
            //progressBar.visibility = View.VISIBLE
            //txtInfo.visibility = View.VISIBLE
        } else {
            llProgressBar.visibility = View.GONE
            //progressBar.visibility = View.INVISIBLE
            //txtInfo.visibility = View.INVISIBLE
        }

        //btnImport.isEnabled = !enable
        //btnExport.isEnabled = !enable
        //logText.isEnabled = !enable
        //hideKeyboard()
    }


    fun getStockCheck(checkListType : Int, checkListDate: Date) : ArrayList<StockCheck> {
        var lstCheck = ArrayList<StockCheck>()
        val db = DBHelper()
        var conn: Connection? = null
        jobSuccess=false
        try {
            conn = db.dbConnect()
            lstCheck= db.getStockCheck(conn,checkListType,sqlDateFormat.format(selectedDate))
            conn!!.close()
            jobSuccess = true
        } catch (e:Exception){
            println(e.message)
        }

        return lstCheck
    }


    fun getStockChecksList(selectedDate:Date) : Pair<ArrayList<CheckItems>,Boolean> {

        val db = DBHelper()
        var conn: Connection? = null
        var jobSuccess=false
        try {
            conn = db.dbConnect()
            lstItems = db.getListOfStockChecks(conn,selectedDate)
            conn!!.close()
            jobSuccess = true
        } catch (e:Exception){
            println(e.message)
        }
        conn!!.close()
        return Pair(lstItems, jobSuccess)
    }

    fun createBlankStockCheck2(checkType: Int, selectedDate: Date) : Boolean{


        val db = DBHelper()
        var conn: Connection? = null

        jobSuccess = false

        try {
            conn = db.dbConnect()
            db.createBlankStockCheck(conn, checkType, selectedDate)
            jobSuccess = true
        } catch (e:Exception){
            println(e.message)
        } finally {
            // cleanup if cancelled
            conn!!.close()
        }
        return jobSuccess
    }

    private fun dlgUpdate(title: String) : Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.progress_popup)
        val info = dialog.findViewById(R.id.txtInfo) as TextView
        info.text = title

        val btnCancel = dialog.findViewById(R.id.btnCancel) as Button
/*        yesBtn.setOnClickListener {
            doSummatLater()
            dialog.dismiss() }*/
        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
        return dialog
    }

    private fun dlgReadStockCheck(title: String) : Dialog {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.progress_popup)
        val info = dialog.findViewById(R.id.txtInfo) as TextView
        info.text = title

        val btnCancel = dialog.findViewById(R.id.btnCancel) as Button
/*        yesBtn.setOnClickListener {
            doSummatLater()
            dialog.dismiss() }*/
        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
        return dialog
    }




}
