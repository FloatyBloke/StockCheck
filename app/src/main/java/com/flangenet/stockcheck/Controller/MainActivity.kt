package com.flangenet.stockcheck.Controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.flangenet.stockcheck.Adapter.CheckItemsAdapter
import com.flangenet.stockcheck.Model.CheckItems
import com.flangenet.stockcheck.R
import com.flangenet.stockcheck.Utilities.EXTRA_CHECKLIST_DATE
import com.flangenet.stockcheck.Utilities.EXTRA_CHECKLIST_TYPE
import com.flangenet.stockcheck.Utilities.prettyDateFormat
import com.flangenet.stockcheck.Utilities.sqlDateFormat
import kotlinx.android.synthetic.main.activity_main.*
import java.sql.Connection
import java.util.*
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

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
        enableSpinner(false,"Info")



    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }
    fun refreshData(){
        txtToday.text = prettyDateFormat.format(selectedDate)
        conn = db.dbConnect()
        lstItems = db.getChecks(conn, selectedDate)
        conn!!.close()

        itemsAdapter = CheckItemsAdapter(this, lstItems as ArrayList<CheckItems>) { position,recordCount ->
            // item is clicked
            // refreshSelected(position)
            // println("$position - ${sqlDateFormat.format(selectedDate)}")

            // Create a new zeroed stock check if none exists

            if (recordCount == 0){
                enableSpinner(true,"Please wait......")
                GlobalScope.launch(Dispatchers.Main) {
                    conn = db.dbConnect()
                    db.createBlankStockCheck(conn, position, selectedDate)
                    conn!!.close()
                }.invokeOnCompletion { println("I'm Back") }
                enableSpinner(false,"Please wait......")
                    
            }

            val checkListIntent = Intent(this,CheckList::class.java)
            checkListIntent.putExtra(EXTRA_CHECKLIST_TYPE,(position))
            checkListIntent.putExtra(EXTRA_CHECKLIST_DATE,sqlDateFormat.format(selectedDate))
            startActivity(checkListIntent)
        }

        recyclerCheckItems.adapter = itemsAdapter
        val layoutManager = LinearLayoutManager(this)
        recyclerCheckItems.layoutManager = layoutManager

    }


    fun testButton() {
        val testIntent = Intent(this,Welcome::class.java)
        startActivity(testIntent)


    }
    fun testButton2() {
        val checkListIntent = Intent(this,CheckList::class.java)
        checkListIntent.putExtra(EXTRA_CHECKLIST_TYPE,2)
        checkListIntent.putExtra(EXTRA_CHECKLIST_DATE,sqlDateFormat.format(selectedDate))
        startActivity(checkListIntent)
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
            progressBar.visibility = View.VISIBLE
            txtInfo.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
            txtInfo.visibility = View.INVISIBLE
        }

        //btnImport.isEnabled = !enable
        //btnExport.isEnabled = !enable
        //logText.isEnabled = !enable
        //hideKeyboard()
    }

}
