package com.flangenet.stockcheck.Controller

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.flangenet.stockcheck.R
import kotlinx.android.synthetic.main.activity_welcome.*
import java.util.*
import kotlinx.coroutines.*



class Welcome : AppCompatActivity() {


    val db = DBHelper(applicationContext)
    private  var logInfo: String = "Starting"
    var logText: StringBuilder  = java.lang.StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        addLog("Attempting to connect to database")
        addLog("Server : ${App.prefs.connectIP}")
        addLog("Database : ${App.prefs.connectDB}")
        addLog("User Name  : ${App.prefs.connectUser}")
        addLog("Password : ${App.prefs.connectPassword}")
        addLog("Waiting.....")


        var outputInfo = "??????"
        enableLogSpinner(true)
        var connectFlag : Boolean = false

        // Test connection to server

        val t = GlobalScope.launch (Dispatchers.IO) {
            logInfo = db.dbConnectCheck()
        }.invokeOnCompletion {
            println("Out.....................")
            connectFlag = true
            this@Welcome.runOnUiThread(java.lang.Runnable {this.checksComplete("Completed connection test")})

        }

        // Routine to make the log look like it's doing something

        val handler = Handler()
            var i = 0
            Thread(Runnable {
                while (i < 180 && !connectFlag) {
                    i += 1
                    handler.post(Runnable {
                        addLog(".")
                    })
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                println("Passed through")

            }).start()
        println("Dropped out the bottom")


    }


    fun checksComplete(outputInfo2: String){

        addLog(outputInfo2)
        var connectedFlag : Boolean = true
        println(logInfo)

        if (logInfo == "Connection Made"){
            addLog("Connection Ok")
        } else {
            addLog("Connection Failed...Talk to Lee :)")
            addLog(logInfo)
        }
        enableLogSpinner(false)
    }


    fun enableLogSpinner(enable: Boolean){
        if (enable) {
            spinnerLog.visibility = View.VISIBLE
        } else {
            spinnerLog.visibility = View.INVISIBLE
        }
        btnContinue.isEnabled = !enable
    }


    fun addLog(logLine: String){
        if (logLine != ".") {
            logText.append(System.lineSeparator())
        }
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
