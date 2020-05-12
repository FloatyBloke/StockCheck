package com.flangenet.stockcheck.Controller

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.flangenet.stockcheck.Adapter.StockItemsAdapter
import com.flangenet.stockcheck.Model.StockCheck
import com.flangenet.stockcheck.R
import com.flangenet.stockcheck.Utilities.EXTRA_CHECKLIST_DATE
import com.flangenet.stockcheck.Utilities.EXTRA_CHECKLIST_TYPE
import kotlinx.android.synthetic.main.activity_check_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.Connection
import java.util.*
import kotlin.collections.ArrayList


class CheckList : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var checkListType : Int = 1
    private var selectedDateText : String = "1971-01-07"
    private var conn:Connection? = null

    var lstItems = ArrayList<StockCheck>()
    lateinit var itemsAdapter: StockItemsAdapter
    var selectedPos: Int = 0
    //Text To Speech
    var mTTS: TextToSpeech? = null

    override fun onDestroy() {
        // shutdown TTS
        if (mTTS != null){
            mTTS!!.stop()
            mTTS!!.shutdown()
        }
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mTTS = TextToSpeech(this,this)
        setContentView(R.layout.activity_check_list)

        checkListType = intent.getIntExtra(EXTRA_CHECKLIST_TYPE,1)
        selectedDateText = intent.getStringExtra(EXTRA_CHECKLIST_DATE)

        btnNext.setOnClickListener{nextButton()}
        btnPrevious.setOnClickListener{prevButton()}
        //btnUpdate.setOnClickListener{updateCheck()}

        // Retrieve Stock Check
        val db = DBHelper()
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


        // Capture Enter key and perform nextButton
        edtEntry.setOnKeyListener { v, keyCode, event ->
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) {
                    nextButton()
            }
            return@setOnKeyListener false
        }

        mTTS!!.speak(intent.getStringExtra(EXTRA_CHECKLIST_DATE),TextToSpeech.QUEUE_FLUSH,null,"")
        enableSpinner(false)
        refreshSelected(0)
    }

    private fun refreshSelected(newItem: Int){
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
        edtEntry.requestFocus()
        edtEntry.selectAll()

        recycleStockItems.smoothScrollToPosition(newItem2)
        recycleStockItems.adapter?.notifyDataSetChanged()
        println("About to say ${lstItems[newItem2].description}")
        mTTS!!.speak(lstItems[newItem2].description,TextToSpeech.QUEUE_FLUSH,null)
        //mTTS.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null)




    }

    private fun nextButton(){
        val newText = edtEntry.text.toString()
        val newStock: Float = 0f
        if (newText!= "") {
            lstItems[selectedPos].stock = newText.toFloat()
        }
        refreshSelected(selectedPos+1)
        askSpeechInput()
    }
    private fun prevButton(){
        val newText = edtEntry.text.toString()
        val newStock: Float = 0f
        if (newText!= "") {
            lstItems[selectedPos].stock = newText.toFloat()
        }
        refreshSelected(selectedPos-1)
    }

    private fun updateCheck(){
        val db = DBHelper()
        conn = db.dbConnect()
        enableSpinner(true)
        GlobalScope.launch(Dispatchers.IO){
            lstItems.forEach{
                db.updateCheck(conn!!,it.checkID,it.stock)
                //println("${it.checkID} , ${it.stock}")
            }
            conn!!.close()
        }.invokeOnCompletion { println("I'm Back") }

        enableSpinner(false)

    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = mTTS!!.setLanguage(Locale.UK)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            } else {
                //buttonSpeak!!.isEnabled = true
                Log.e("TTS","Speech language ok")
                refreshSelected(selectedPos)
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            println("BACK IN TIME")
            updateCheck()
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER){
            println("Enter Pressed")
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun enableSpinner(enable: Boolean) {
        txtInfoCheck.text = "Please Wait....Saving"
        if (enable) {
            progressBarCheck.visibility = View.VISIBLE
            txtInfoCheck.visibility = View.VISIBLE
        } else {
            progressBarCheck.visibility = View.INVISIBLE
            txtInfoCheck.visibility = View.INVISIBLE
        }

        btnNext.isEnabled = !enable
        btnPrevious.isEnabled = !enable
        edtEntry.isEnabled = !enable
        //hideKeyboard()
    }
    private fun askSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi speak something");
        startActivityForResult(intent, 1000);
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            Log.d("TAG-R", results?.toString())
            edtEntry.setText(results!![0].toString())
            nextButton()
        }
    }


}




