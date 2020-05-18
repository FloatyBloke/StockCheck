package com.flangenet.stockcheck

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.android.synthetic.main.alert_dialog_with_edittext.*
import kotlinx.android.synthetic.main.alert_dialog_with_edittext.view.*
import kotlinx.android.synthetic.main.alert_dialog_with_edittext.view.mainInfoTv


class Test : AppCompatActivity() {

    private lateinit var busyAnimation: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)
        val busyImage = findViewById<ImageView>(R.id.imgBusy).apply {
            setBackgroundResource(R.drawable.busy_animation)
            busyAnimation = this.background as AnimationDrawable
        }
    }

    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_test, container, false)
    }

    override fun onStart() {
        super.onStart()

        button.setOnClickListener { getOut() }

        busyAnimation.start()

        //button click to show dialog
        btnTest2.setOnClickListener {
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_with_edittext, null)
            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Login Form")
            //show dialog
            val  mAlertDialog = mBuilder.show()
            //login button click of custom layout
            mDialogView.btn1.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
                //get text from EditTexts of custom layout
                val name = mDialogView.editText.text.toString()

            }
            //cancel button click of custom layout
            mDialogView.btnCancel.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }
        }
    }

    fun getOut(){
        finish()


    }



}
