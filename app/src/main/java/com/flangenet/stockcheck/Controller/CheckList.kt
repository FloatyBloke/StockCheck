package com.flangenet.stockcheck.Controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.flangenet.stockcheck.Adapter.StockItemsAdapter
import com.flangenet.stockcheck.Model.StockCheck
import com.flangenet.stockcheck.Model.StockItem
import com.flangenet.stockcheck.R
import com.flangenet.stockcheck.Utilities.EXTRA_CHECKLIST_DATE
import com.flangenet.stockcheck.Utilities.EXTRA_CHECKLIST_TYPE
import kotlinx.android.synthetic.main.activity_check_list.*
import java.sql.Connection
import java.util.*
import kotlin.collections.ArrayList


class CheckList : AppCompatActivity() {

    private var checkListType : Int = 1
    private var selectedDateText : String = "1971-01-07"
    private var conn:Connection? = null

    var lstItems = ArrayList<StockCheck>()
    lateinit var itemsAdapter: StockItemsAdapter
    var selectedPos: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_list)

        checkListType = intent.getIntExtra(EXTRA_CHECKLIST_TYPE,1)
        selectedDateText = intent.getStringExtra(EXTRA_CHECKLIST_DATE)

        btnNext.setOnClickListener{nextButton()}
        btnPrevious.setOnClickListener{prevButton()}


        val db = ConnectionClass()
        conn = db.dbConnect()

        lstItems = db.getStockCheck(conn, checkListType, selectedDateText)
        conn!!.close()

        itemsAdapter = StockItemsAdapter(this, lstItems as ArrayList<StockCheck>) { position ->
            // item is clicked
            refreshSelected(position)
        }

        recycleStockItems.adapter = itemsAdapter
        val layoutManager = LinearLayoutManager(this)
        recycleStockItems.layoutManager = layoutManager
        refreshSelected(0)

    }

    fun refreshSelected(newItem: Int){
        //println("${selectedPos} - $newItem")
        var newItem2 = newItem

        if (newItem >= lstItems.count()){
            newItem2 = 0
        }
        if (newItem < 0){
            newItem2 = lstItems.count()-1
        }
        //println("After new calc Selected : $selectedPos - Count:${lstItems.count()} - NewItem:$newItem2")
        txtItemDescription.text = lstItems[newItem2].description
        lstItems[selectedPos].selected = false
        lstItems[newItem2].selected = true
        selectedPos = newItem2
        if (lstItems[selectedPos].stock == 0f) {
            edtEntry.setText("")
        } else {
            edtEntry.setText(lstItems[selectedPos].stock.toString())
        }
        edtEntry.selectAll()
        recycleStockItems.smoothScrollToPosition(newItem2)
        recycleStockItems.adapter?.notifyDataSetChanged()

    }

    fun nextButton(){
        val newText = edtEntry.text.toString()
        val newStock: Float = 0f
        if (newText!= "") {
            lstItems[selectedPos].stock = newText.toFloat()
        }
        refreshSelected(selectedPos+1)
    }
    fun prevButton(){
        val newText = edtEntry.text.toString()
        val newStock: Float = 0f
        if (newText!= "") {
            lstItems[selectedPos].stock = newText.toFloat()
        }
        refreshSelected(selectedPos-1)
    }
}
