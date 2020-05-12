package com.flangenet.stockcheck.Controller

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flangenet.stockcheck.R
import kotlinx.android.synthetic.main.activity_welcome.*
import java.sql.Connection
import java.util.*
import kotlin.concurrent.schedule


class Welcome : AppCompatActivity() {

    val db = DBHelper()
    private  var conn: Connection? = null
    var logText: StringBuilder  = java.lang.StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        addLog("Attempting to connect to database")
        addLog("Waiting.....")
        var outputInfo = "??????"
        enableLogSpinner(true)

            conn = db.dbConnect()
            checksComplete(conn.toString())

        askSpeechInput()




    }

    fun check() : Connection?{
        return db.dbConnect()
    }

    fun checksComplete(outputInfo2: String){
        addLog(outputInfo2)
        enableLogSpinner(false)

    }


    fun enableLogSpinner(enable: Boolean){
        if (enable) {
            spinnerLog.visibility = View.VISIBLE
        } else {
            spinnerLog.visibility = View.INVISIBLE
        }

    }


    fun addLog(logLine: String){
        logText.append(System.lineSeparator())
        logText.append(logLine)
        txtLog.text = logText
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
            addLog(results!![0].toString())
            askSpeechInput()
        }
    }

}
