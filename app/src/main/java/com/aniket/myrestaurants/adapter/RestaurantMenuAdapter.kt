package com.aniket.myrestaurants.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.model.FoodItem

class RestaurantMenuAdapter(
    val context: Context, val itemList: ArrayList<FoodItem>, private val listener:
    OnItemClickListener
): RecyclerView.Adapter<RestaurantMenuAdapter.DescriptionViewHolder>() {

    companion object {
        var isCartEmpty = true
    }

    class DescriptionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtItemNo: TextView = view.findViewById(R.id.txtItemNo)
        val txtItem: TextView = view.findViewById(R.id.txtItem)
        val txtItemPrice: TextView = view.findViewById(R.id.txtItemPrice)
        val btnAddItem: Button = view.findViewById(R.id.btnAddToCart)
        val btnRemoveItem: Button = view.findViewById(R.id.btnRemoveFromCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DescriptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_menu_single_row, parent, false)

        return DescriptionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    interface OnItemClickListener {
        fun onAddItemClick(foodItem: FoodItem)
        fun onRemoveItemClick(foodItem: FoodItem)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DescriptionViewHolder, position: Int) {
        val restaurantItem = itemList[position]
        holder.txtItemNo.text = (position + 1).toString()
        holder.txtItem.text = restaurantItem.ItemName
        holder.txtItemPrice.text = restaurantItem.ItemCostForOne.toString()

        holder.btnAddItem.setOnClickListener {
            holder.btnAddItem.visibility = View.GONE
            holder.btnRemoveItem.visibility = View.VISIBLE
            listener.onAddItemClick(restaurantItem)
        }

        holder.btnRemoveItem.setOnClickListener {
            holder.btnRemoveItem.visibility = View.GONE
            holder.btnAddItem.visibility = View.VISIBLE
            listener.onRemoveItemClick(restaurantItem)

        }

    }
}
