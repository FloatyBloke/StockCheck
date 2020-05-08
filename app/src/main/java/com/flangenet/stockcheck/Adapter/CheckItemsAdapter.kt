package com.flangenet.stockcheck.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flangenet.stockcheck.Model.CheckItems
import com.flangenet.stockcheck.R

class CheckItemsAdapter (val context: Context, val items:ArrayList<CheckItems>, val itemClick:(Int) -> Unit):
    RecyclerView.Adapter<CheckItemsAdapter.ViewHolder>(){

    inner class ViewHolder (itemView: View, val itemClick: (Int) -> Unit) : RecyclerView.ViewHolder(itemView){
        private val description: TextView = itemView.findViewById<TextView>(R.id.txtDescription)
        private val iD: TextView = itemView.findViewById<TextView>(R.id.txtId)
        private val count: TextView = itemView.findViewById<TextView>(R.id.txtCount)

        fun bindItems(item: CheckItems, position: Int){
            description.text = item.description
            iD.text = position.toString()
            count.text = item.counter.toString()

            itemView.setOnClickListener{
                //it.setBackgroundColor(context.getColor(android.R.color.holo_green_light))
                itemClick(position)
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

        holder.bindItems(items[position], position)
        // println("${holder.adapterPosition}")



    }
}