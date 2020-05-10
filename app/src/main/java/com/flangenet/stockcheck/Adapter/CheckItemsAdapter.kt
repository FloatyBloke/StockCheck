package com.flangenet.stockcheck.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flangenet.stockcheck.Model.CheckItems
import com.flangenet.stockcheck.R

class CheckItemsAdapter (val context: Context, val items:ArrayList<CheckItems>, val itemClick:(Int,Int) -> Unit):
    RecyclerView.Adapter<CheckItemsAdapter.ViewHolder>(){

    inner class ViewHolder (itemView: View, val itemClick: (Int,Int) -> Unit) : RecyclerView.ViewHolder(itemView){
        private val description: TextView = itemView.findViewById<TextView>(R.id.txtDescription)
        //private val iD: TextView = itemView.findViewById<TextView>(R.id.txtId)
        //private val count: TextView = itemView.findViewById<TextView>(R.id.txtCount)
        private val imgTick: ImageView = itemView.findViewById<ImageView>(R.id.imgTick)

        fun bindItems(item: CheckItems, checkID: Int){
            description.text = item.description
            //iD.text = position.toString()
            //iD.text = item.checkID.toString()
            //count.text = item.counter.toString()
            if (item.counter > 0){
                imgTick.visibility = ImageView.VISIBLE
            } else {
                imgTick.visibility = ImageView.INVISIBLE
            }


            itemView.setOnClickListener{
                //it.setBackgroundColor(context.getColor(android.R.color.holo_green_light))
                itemClick(checkID,item.counter)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckItemsAdapter.ViewHolder {
        // println("*****$viewType")
        val view = LayoutInflater.from(context).inflate(
            R.layout.check_line,parent, false)
        return ViewHolder(view,itemClick)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: CheckItemsAdapter.ViewHolder, position: Int) {

        holder.bindItems(items[position], items[position].checkID)
        // println("${holder.adapterPosition}")



    }
}