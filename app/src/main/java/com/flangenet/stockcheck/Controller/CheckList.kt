package com.flangenet.stockcheck.Controller

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.flangenet.stockcheck.Adapter.StockItemsAdapter
import com.flangenet.stockcheck.Model.StockCheck
import com.flangenet.stockcheck.R
import com.flangenet.stockcheck.Utilities.*
import kotlinx.android.synthetic.main.activity_check_list.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CheckList : AppCompatActivity(), TextToSpeech.OnInitListener, CoroutineScope by MainScope()  {

    private var checkListType : Int = 1
    private var selectedDateText : String = "1971-01-07"
    private var checkListDescription : String = ":)"
    private var conn:Connection? = null
    private var speechStatus = false

    var lstItems = ArrayList<StockCheck>()
    lateinit var itemsAdapter: StockItemsAdapter
    var selectedPos: Int = 0
    //Text To Speech
    var mTTS: TextToSpeech? = null
    var jobSuccess = false


    override fun onDestroy() {
        // shutdown TTS
        if (mTTS != null){
            mTTS!!.stop()
            mTTS!!.shutdown()
        }
        super.onDestroy()
    }

    companion object {
        private val TAG = CheckList::class.java.simpleName
        private const val STOCK_CHECK_ARRAY = "STOCK_CHECK_ARRAY"
        private const val SPEECH_STATUS = "SPEECH_STATUS"
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(STOCK_CHECK_ARRAY, lstItems)
        outState.putBoolean(SPEECH_STATUS, speechStatus)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mTTS = TextToSpeech(this, this)
        mTTS!!.stop()

        setContentView(R.layout.activity_check_list)

        checkListType = intent.getIntExtra(EXTRA_CHECKLIST_TYPE, 1)
        checkListDescription = intent.getStringExtra(EXTRA_CHECKLIST_DESC)
        selectedDateText = intent.getStringExtra(EXTRA_CHECKLIST_DATE)
        lstItems = intent.getParcelableArrayListExtra(EXTRA_CHECK_ARRAY)

        btnNext.setOnClickListener { nextButton() }
        btnPrevious.setOnClickListener { prevButton() }
        togPr.setOnClickListener { prepToggle() }
        //btnUpdate.setOnClickListener{updateCheck()}
        fabSpeechOutput.setOnClickListener { speechStatusToggle() }

        // Capture Enter key and perform nextButton
        edtEntry.setOnKeyListener { v, keyCode, event ->
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN)) {
                nextButton()
            }
            return@setOnKeyListener false
        }

        // Retrieve Stock Check - if screen rotated use passed array

        if (savedInstanceState != null) {
            //wcDate = sqlDateToDate(savedInstanceState.getString(WEEK_COMMENCING_DATE)!!)
            lstItems = savedInstanceState.getParcelableArrayList<StockCheck>(STOCK_CHECK_ARRAY)!!
            speechStatus = savedInstanceState.getBoolean(SPEECH_STATUS)

            speechStatus = !speechStatus
            speechStatusToggle()

            var counter = 0
            lstItems.forEach {
                if (it.selected) {
                    selectedPos = counter
                }
                println(counter)
                counter += 1
            }
            println("Passed Position : $selectedPos")
        }


        itemsAdapter = StockItemsAdapter(this, lstItems as ArrayList<StockCheck>) { position ->
            // item is clicked
            refreshSelected(position)
        }

        recycleStockItems.adapter = itemsAdapter
        val layoutManager = LinearLayoutManager(this)
        recycleStockItems.layoutManager = layoutManager


        //this.say(intent.getStringExtra(EXTRA_CHECKLIST_TYPE))
        var headerInfo : String = "${checkListDescription} - ${selectedDateText}"

        txtCheckHeader.text = "${checkListDescription}${System.lineSeparator()}${sqlTextDateToUKDate(selectedDateText)}"
        say(headerInfo)
        refreshSelected(selectedPos)
    }


    private fun speechStatusToggle(){
        if (speechStatus) {
            fabSpeechOutput.setImageResource(R.drawable.ic_action_voice_over_off)
        } else {
            fabSpeechOutput.setImageResource(R.drawable.ic_action_record_voice_over)
        }

        speechStatus = ! speechStatus
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
        println("After new calc Selected : $selectedPos - Count:${lstItems.count()} - NewItem2:$newItem2")
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

        togPr.isChecked = lstItems[selectedPos].prep


        recycleStockItems.smoothScrollToPosition(newItem2)
        recycleStockItems.adapter?.notifyDataSetChanged()
        println("About to say ${lstItems[newItem2].description}")

        say(lstItems[newItem2].description)

    }

    private fun say(stuff: String){
        if (speechStatus){
            mTTS!!.speak(stuff,TextToSpeech.QUEUE_FLUSH,null)
        }


    }
    private fun nextButton(){
        val newText = edtEntry.text.toString()
        val newStock: Float = 0f



        if (newText!= "") {

            lstItems[selectedPos].changed = newText.toFloat() != lstItems[selectedPos].stock

            lstItems[selectedPos].stock = newText.toFloat()
        }

        refreshSelected(selectedPos+1)
        //askSpeechInput()
    }
    private fun prevButton(){
        val newText = edtEntry.text.toString()
        val newStock: Float = 0f
        if (newText!= "") {
            lstItems[selectedPos].stock = newText.toFloat()
        }
        refreshSelected(selectedPos-1)
    }

    private fun updateStockCheck(){
        // put array back to parent intent
        val resultIntent = Intent()
        resultIntent.putParcelableArrayListExtra("keyName", lstItems)
        setResult(Activity.RESULT_OK, resultIntent)
        Log.d("TRACKING","About to return to MainActivity :)")
        finish()
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
        say("Speech initialisation Complete")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            // Back button pressed , Exit from activity
            println("Back button pressed")
            updateStockCheck()
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER){
            println("Enter Pressed")
        }
        return super.onKeyDown(keyCode, event)
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

            var t : Float = results!![0].toFloatOrNull()!!
            if (t == null){
                say("I didn't understand")
                refreshSelected(selectedPos)
            } else {
                edtEntry.setText(results!![0].toString())
                nextButton()
            }

        }
    }

    private fun sqlTextDateToUKDate(sqlTextDate: String) : String {
        val date = SimpleDateFormat("yyyy-MM-dd").parse(sqlTextDate)
       // val format = dateFormat
        return dateFormat.format(date)
    }
    private fun prepToggle() {
        lstItems[selectedPos].prep = !lstItems[selectedPos].prep
        lstItems[selectedPos].changed = true
        //refreshSelected(selectedPos)
        recycleStockItems.adapter?.notifyDataSetChanged()

    }

}




