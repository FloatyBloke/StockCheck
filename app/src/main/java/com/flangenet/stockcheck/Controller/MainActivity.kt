package com.flangenet.stockcheck.Controller

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flangenet.stockcheck.R
import com.flangenet.stockcheck.Utilities.EXTRA_CHECKLIST_TYPE
import kotlinx.android.synthetic.main.activity_main.*
import java.sql.Connection


class MainActivity : AppCompatActivity() {

    private  var conn: Connection? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnStockView.setOnClickListener { testButton() }
        btnNewDay.setOnClickListener { testButton2() }
        //getConnection()

    }


    fun testButton() {
        val checkListIntent = Intent(this,CheckList::class.java)
        checkListIntent.putExtra(EXTRA_CHECKLIST_TYPE,1)
        startActivity(checkListIntent)
        //executeMySQLQuery()

        //t?.close()

    }
    fun testButton2() {
        val checkListIntent = Intent(this,CheckList::class.java)
        checkListIntent.putExtra(EXTRA_CHECKLIST_TYPE,2)
        startActivity(checkListIntent)
        //executeMySQLQuery()

        //t?.close()

    }




}
