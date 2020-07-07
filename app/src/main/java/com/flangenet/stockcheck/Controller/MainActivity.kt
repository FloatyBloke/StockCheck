package com.flangenet.stockcheck.Controller

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.flangenet.stockcheck.Adapter.CheckItemsAdapter
import com.flangenet.stockcheck.Model.CheckItems
import com.flangenet.stockcheck.Model.StockCheck
import com.flangenet.stockcheck.R
import com.flangenet.stockcheck.Utilities.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.progress_bit.*
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetAddress
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() ,CoroutineScope by MainScope()  {


    var lstItems = ArrayList<CheckItems>()
    lateinit var itemsAdapter: CheckItemsAdapter
    val bob = GetStockChecksList()
    var selectedDate = Date()
    var jobSuccess = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //btnTest.setOnClickListener { testButton() }
        //btnNewDay.setOnClickListener { testButton2() }
        btnNextWeek.setOnClickListener { changeDate(1) }
        btnPrevWeek.setOnClickListener { changeDate(-1) }
        setSupportActionBar(findViewById(R.id.miToolbar))

        //bob.addObserver(GetChecksListObserver())
        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            true
        }

        R.id.action_favorite -> {
            // User chose the "Favorite" action, mark the current item
            // as a favorite...
            val t = ping("bgz3cg3qm8wkdi24bdle-mysql.services.clever-cloud.com")
            Toast.makeText(this,"${PreferenceManager.getDefaultSharedPreferences(this).getString("password", null)} - $t",Toast.LENGTH_LONG).show()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    inner class GetChecksListObserver : Observer {

        override fun update(o: Observable?, arg: Any?) {
            val bob : GetStockChecksList = arg as GetStockChecksList
            //println("I'm back from something ${bob.lstItems} - ${bob.jobSuccess}")
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
        launch(Dispatchers.Main) {
            enableSpinner(true,"Starting off busy")
        }
        refreshData()
    }



    fun refreshData() {
        txtToday.text = prettyDateFormat.format(selectedDate)
        recyclerCheckItems.layoutManager = null


        launch(Dispatchers.Main) {enableSpinner(true,"Loading stock check list ....")}
        //bob.getStockChecksListObservable(selectedDate)

        val (tempItems, jobSuccess2) = getStockChecksList(selectedDate)
        println("${tempItems.count()} - $jobSuccess")
        lstItems = tempItems
        if (jobSuccess2) {
            lstItems = tempItems
            recyclerCheckItems.visibility = View.VISIBLE
        } else {
            Toast.makeText(applicationContext, "Connection Failed...", Toast.LENGTH_LONG).show()
        }

        launch(Dispatchers.Main) {enableSpinner(false,"Loading stock check list complete....")}
        //dlg.dismiss()
        refreshDataPart2()
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
    }

    private fun createBlankStockCheckComplete(position: Int){
        if (jobSuccess){
            enableSpinner(false,"Back from creating new blank stock check - Success")
            openStockCheck(position)
        } else {
            enableSpinner(false,"Back from creating new blank stock check - Failure")
            Toast.makeText(this,"Error creating new blank Stock check", Toast.LENGTH_LONG).show()
        }
    }

    private fun openStockCheck(position : Int){

        val checkListIntent = Intent(this,CheckList::class.java)
        checkListIntent.putExtra(EXTRA_CHECKLIST_TYPE,position)
        checkListIntent.putExtra(EXTRA_CHECKLIST_DESC,lstItems[position-1].description)
        //println(lstItems[position-1].description)
        checkListIntent.putExtra(EXTRA_CHECKLIST_DATE,sqlDateFormat.format(selectedDate))

        var lstCheck: ArrayList<StockCheck> = getStockCheck(position, selectedDate)
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
                val returnArray: ArrayList<StockCheck> = data!!.getParcelableArrayListExtra<StockCheck>("keyName")
                // Process returned data
                // println("$returnArray")
/*                returnArray.forEach {
                    println("${it.checkID} - ${it.description} - ${it.stock}")
                }*/
                launch(Dispatchers.Main) {enableSpinner(true,"Loading stock check list ....")}
                updateStockCheck(returnArray)
                launch(Dispatchers.Main) {enableSpinner(false,"Loading stock check list complete ....")}

            }
        }
    }


    fun testButton() {
        //showDialog("Loading stock check list...")

        /*this@MainActivity.runOnUiThread {dlg = showDialog("Arse Buckets")}
        this@MainActivity.runOnUiThread { enableSpinner(true, "Loading stock check list....") }*/

        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
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
            recyclerCheckItems.visibility = View.INVISIBLE
            llProgressBar.visibility = View.VISIBLE
            //progressBar.visibility = View.VISIBLE
            //txtInfo.visibility = View.VISIBLE
        } else {
            recyclerCheckItems.visibility = View.VISIBLE
            llProgressBar.visibility = View.INVISIBLE
            //progressBar.visibility = View.INVISIBLE
            //txtInfo.visibility = View.INVISIBLE
        }

        //btnImport.isEnabled = !enable
        //btnExport.isEnabled = !enable
        //logText.isEnabled = !enable
        //hideKeyboard()
    }

    private fun getStockCheck(checkListType : Int, checkListDate: Date) : ArrayList<StockCheck> {
        var lstCheck = ArrayList<StockCheck>()
        val db = DBHelper(this)
        var conn: Connection? = null
        jobSuccess=false
        try {
            conn = db.dbConnect()
            if (conn != null) {
                lstCheck= db.getStockCheck(conn,checkListType,sqlDateFormat.format(selectedDate))
                jobSuccess = true
                conn.close()
            }
        } catch (e:Exception){
            println(e.message)
        }

        return lstCheck
    }


    private fun getStockChecksList(selectedDate:Date) : Pair<ArrayList<CheckItems>,Boolean>  = runBlocking (Dispatchers.IO){

        val db = DBHelper(applicationContext)
        var conn: Connection? = null
        var jobSuccess=false
        try {
            conn = db.dbConnect()
            if (conn != null) {
                lstItems = db.getListOfStockChecks(conn, selectedDate)
                jobSuccess = true
                conn!!.close()
            }
        } catch (e:Exception) {
            println(e.message)
        }

        return@runBlocking Pair(lstItems, jobSuccess)
    }

    private fun createBlankStockCheck2(checkType: Int, selectedDate: Date) : Boolean{

        val db = DBHelper(this)
        var conn: Connection? = null

        jobSuccess = false

        try {
            conn = db.dbConnect()
            if (conn != null) {
                db.createBlankStockCheck(conn, checkType, selectedDate)
                jobSuccess = true
                conn!!.close()
            }
        } catch (e:Exception){
            println(e.message)
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

    private fun updateStockCheck(lstItems: ArrayList<StockCheck>){

        val db = DBHelper(this)
        var conn: Connection? = null
        var jobSuccess=false
        var ps: PreparedStatement
        var recCount = 0

        // Check if any update is needed
        lstItems.forEach {
            if (it.stock != it.inStock) {
                recCount = +1
            }
        }

        if (recCount>0) {
            try {
                conn = db.dbConnect()
                if (conn != null) {
                    lstItems.forEach {
                        if (it.stock != it.inStock) {
                            ps = conn!!.prepareStatement("UPDATE checks SET stock=${it.stock}, prep=${it.prep} WHERE id=${it.checkID}")
                            var i = ps.executeUpdate()
                            recCount = +1
                            println("stock=${it.stock}, prep=${it.prep} WHERE id=${it.checkID}")
                        }

                    }
                    jobSuccess = true
                    Toast.makeText(this,"Stock check update success",Toast.LENGTH_LONG).show()
                    conn!!.close()
                }
            } catch (e: Exception) {
                Log.e("SQL", "Update stock check failed")
                Toast.makeText(this, "Stock check update failed", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this,"Nothing to update",Toast.LENGTH_LONG).show()
        }
    }


    fun ping(host: String?): Long? = runBlocking (Dispatchers.IO){
        val startTime = Date()
        var outSecs: Long = 0
        try {
            val address: InetAddress = InetAddress.getByName(host)
            if (address.isReachable(1000)) {
                outSecs = (startTime.time - Date().time) / 1000
            }
        } catch (e: IOException) {
            // Host not available, nothing to do here
        }
        return@runBlocking outSecs
    }
}
