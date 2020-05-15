package com.flangenet.stockcheck.Controller

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.flangenet.stockcheck.Adapter.CheckItemsAdapter
import com.flangenet.stockcheck.Model.CheckItems
import com.flangenet.stockcheck.R
import com.flangenet.stockcheck.Utilities.*
import kotlinx.android.synthetic.main.activity_main.*
import java.sql.Connection
import java.util.*


class MainActivity : AppCompatActivity() ,NoticeDialogFragment1.NoticeDialogListener {

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
                println("I'm making a new one $position")
                //GlobalScope.launch(Dispatchers.Main) {
                    conn = db.dbConnect()
                    db.createBlankStockCheck(conn, position, selectedDate)
                    conn!!.close()
                //}.invokeOnCompletion { println("I'm Back") }
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
        //val testIntent = Intent(this,Welcome::class.java)
        //startActivity(testIntent)
        //println(showDialog("Arse Buckets"))
 /*       val testIntent = Intent(this, Test::class.java)
        startActivity(testIntent)
*/

        showNoticeDialog()
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
        yesBtn.setOnClickListener { dialog.dismiss() }
        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
        return "Yes"

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

    fun showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        val dialog = NoticeDialogFragment1()
        dialog.show(supportFragmentManager, "NoticeDialogFragment")
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        Toast.makeText(this,"Positive Button",Toast.LENGTH_LONG).show()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        Toast.makeText(this,"Negative Button",Toast.LENGTH_LONG).show()
    }

}
