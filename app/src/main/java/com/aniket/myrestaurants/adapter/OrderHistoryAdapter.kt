package com.aniket.myrestaurants.adapter

import android.content.Context
import android.service.autofill.FillEventHistory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.model.FoodItem
import com.aniket.myrestaurants.model.OrderDetails
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderHistoryAdapter(val context : Context, private val orderHistoryList : ArrayList<OrderDetails>)

    : RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    class OrderHistoryViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val txtResName : TextView = view.findViewById(R.id.txtResHistoryName)
        val txtDate : TextView = view.findViewById(R.id.txtDate)
        val recyclerResHistory : RecyclerView = view.findViewById(R.id.recyclerResHistoryItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_order_history_row, parent, false)

        return OrderHistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
       return orderHistoryList.size
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val orderHistoryObject = orderHistoryList[position]
        holder.txtResName.text = orderHistoryObject.resName
        holder.txtDate.text = formatDate(orderHistoryObject.orderDate)
        setUpRecycler(holder.recyclerResHistory, orderHistoryObject)
    }

     private fun setUpRecycler(recyclerResHistory: RecyclerView, orderHistoryList: OrderDetails){
        val foodItemList = ArrayList<FoodItem>()
        for(i in 0 until orderHistoryList.foodItem.length()){
            val foodJson = orderHistoryList.foodItem.getJSONObject(i)
                foodItemList.add(
                    FoodItem(
                        foodJson.getString("food_item_id"),
                        foodJson.getString("name"),
                        foodJson.getString("cost").toInt()
                    )
            )
        }
        val cartItemAdapter = CartItemAdapter(foodItemList, context)
        val mLayoutManager = LinearLayoutManager(context)
        recyclerResHistory.adapter = cartItemAdapter
        recyclerResHistory.itemAnimator = DefaultItemAnimator()
        recyclerResHistory.layoutManager = mLayoutManager
    }

    private fun formatDate(dateString: String):String? {
        val inputFormatter = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.ENGLISH)
        val date : Date = inputFormatter.parse(dateString) as Date

        val outPutFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        return outPutFormatter.format(date)
    }


}