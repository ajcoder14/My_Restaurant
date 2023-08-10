package com.aniket.myrestaurants.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aniket.myrestaurants.Activity.CartActivity
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.model.FoodItem

class CartItemAdapter(private val cartList: ArrayList<FoodItem>, val context: Context) :
    RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val itemName : TextView = view.findViewById(R.id.txtCartItemName)
        val itemPrice : TextView = view.findViewById(R.id.txtCartPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_cart_view_item, parent, false)

        return CartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    //for return view type for recycler
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartObject = cartList[position]
        holder.itemName.text = cartObject.ItemName
        val cost ="Rs. ${cartObject.ItemCostForOne?.toString()}"
        holder.itemPrice.text = cost
    }
}