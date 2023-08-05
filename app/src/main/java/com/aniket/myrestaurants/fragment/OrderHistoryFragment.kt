package com.aniket.myrestaurants.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.adapter.OrderHistoryAdapter
import com.aniket.myrestaurants.model.OrderDetails
import com.aniket.myrestaurants.util.DrawerLocker
import java.lang.reflect.Array.getInt

class OrderHistoryFragment : Fragment() {

    lateinit var recyclerOrderHistory : RecyclerView
    lateinit var orderHistoryAdapter: OrderHistoryAdapter
    lateinit var llHasOrders : LinearLayout
    lateinit var llHasNoOrders : RelativeLayout
    lateinit var rlLoading : RelativeLayout
    lateinit var sharedPreferences: SharedPreferences
    var orderHistoryList = ArrayList<OrderDetails>()
    private var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_order_history, container, false)
//        (activity as DrawerLocker).setDrawerEnabled(true)
        llHasOrders  = view.findViewById(R.id.llHasOrders)
        llHasNoOrders = view.findViewById(R.id.rlNoOrders)
        recyclerOrderHistory = view.findViewById(R.id.recyclerOrderHistory)
        rlLoading = view?.findViewById(R.id.rlLoading) as RelativeLayout
        rlLoading.visibility = View.VISIBLE

        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.Users_Data), Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("user_Id", null) as String

        sendServerRequest(userId)

        return  view
    }

    private fun sendServerRequest( userId : String) {
        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/orders/fetch_result/"

        val jsonObjectRequest =
            object : JsonObjectRequest(Request.Method.GET, url + userId, null, Response.Listener {

                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        rlLoading.visibility = View.GONE

                        val resArray = data.getJSONArray("data")
                        if (resArray.length() == 0) {
                            llHasNoOrders.visibility = View.VISIBLE
                            llHasOrders.visibility = View.GONE

                        } else {
                            for (i in 0 until resArray.length()) {
                                val orderObject = resArray.getJSONObject(i)
                                val foodItems = orderObject.getJSONArray("food_items")
                                val orderDetails = OrderDetails(
                                    orderObject.getInt("order_id"),
                                    orderObject.getString("restaurant_name"),
                                    orderObject.getString("order_placed_at"),
                                    foodItems
                                )
                                orderHistoryList.add(orderDetails)
                                if (orderHistoryList.isEmpty()) {
                                    llHasNoOrders.visibility = View.VISIBLE
                                    llHasOrders.visibility = View.GONE

                                } else {
                                    llHasNoOrders.visibility = View.GONE
                                    llHasOrders.visibility = View.VISIBLE

                                }
                                if (activity != null) {
                                    orderHistoryAdapter =
                                        OrderHistoryAdapter(activity as Context, orderHistoryList)
                                    val mLayoutManager = LinearLayoutManager(activity as Context)
                                    recyclerOrderHistory.layoutManager = mLayoutManager
                                    recyclerOrderHistory.itemAnimator = DefaultItemAnimator()
                                    recyclerOrderHistory.adapter = orderHistoryAdapter
                                } else {
                                    queue.cancelAll(this::class.java.simpleName)
                                }
                            }

                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, Response.ErrorListener {
                Toast.makeText(activity as Context, it.message, Toast.LENGTH_SHORT).show()
            })

                {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"

                /*The below used token will not work, kindly use the token provided to you in the training*/
                headers["token"] = "e1a275a0db2e15"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }
}