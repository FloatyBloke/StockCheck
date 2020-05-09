package com.flangenet.stockcheck.Controller

import android.content.Intent
import android.os.Bundle
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
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private  var conn: Connection? = null
    var lstItems = ArrayList<CheckItems>()
    lateinit var itemsAdapter: CheckItemsAdapter
    val db = ConnectionClass()
    var selectedDate = Date()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //btnStockView.setOnClickListener { testButton() }
        //btnNewDay.setOnClickListener { testButton2() }
        btnNextWeek.setOnClickListener { changeDate(1) }
        btnPrevWeek.setOnClickListener { changeDate(-1) }
        //getConnection()

        refreshData()

    }
    fun refreshData(){
        txtToday.text = prettyDateFormat.format(selectedDate)
        conn = db.dbConnect()
        lstItems = db.getChecks(conn, selectedDate)
        conn!!.close()

        itemsAdapter = CheckItemsAdapter(this, lstItems as ArrayList<CheckItems>) { position ->
            // item is clicked
            //refreshSelected(position)
            //println("$position - ${sqlDateFormat.format(selectedDate)}")
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
        val checkListIntent = Intent(this,CheckList::class.java)
        checkListIntent.putExtra(EXTRA_CHECKLIST_TYPE,1)
        checkListIntent.putExtra(EXTRA_CHECKLIST_DATE,sqlDateFormat.format(selectedDate))
        startActivity(checkListIntent)
        //executeMySQLQuery()

        //t?.close()

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




}
