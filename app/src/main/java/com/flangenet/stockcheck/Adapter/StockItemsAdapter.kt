package com.flangenet.stockcheck.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flangenet.stockcheck.Model.StockCheck
import com.flangenet.stockcheck.Model.StockItem
import com.flangenet.stockcheck.R
import kotlinx.android.synthetic.main.stock_line.view.*

class StockItemsAdapter (val context: Context, val items:ArrayList<StockCheck>, val itemClick:(Int) -> Unit):RecyclerView.Adapter<StockItemsAdapter.ViewHolder>(){

    inner class ViewHolder (itemView: View, val itemClick: (Int) -> Unit) : RecyclerView.ViewHolder(itemView){
        private val description: TextView = itemView.findViewById<TextView>(R.id.Description)
        private val iD: TextView = itemView.findViewById<TextView>(R.id.txtId)
        private val stock: TextView = itemView.findViewById<TextView>(R.id.txtStock)

        fun bindItems(item:StockCheck, position: Int){
            description.text = item.description
            iD.text = position.toString()
            stock.text = String.format("%.2f", item.stock)
            itemView.setOnClickListener{
                //it.setBackgroundColor(context.getColor(android.R.color.holo_green_light))
                itemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockItemsAdapter.ViewHolder {
       // println("*****$viewType")
        val view = LayoutInflater.from(context).inflate(
            R.layout.stock_line,parent, false)
        return ViewHolder(view,itemClick)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: StockItemsAdapter.ViewHolder, position: Int) {
        if (items[position].selected) {
            holder.itemView.setBackgroundColor(context.getColor(android.R.color.holo_green_light))

        } else {
            holder.itemView.setBackgroundColor(context.getColor(android.R.color.white))
            //holder.itemView.Description.fontFeatureSettings
        }
        holder.bindItems(items[position], position)
       // println("${holder.adapterPosition}")



    }


}