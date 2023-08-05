package com.aniket.myrestaurants.Activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.adapter.CartItemAdapter
import com.aniket.myrestaurants.adapter.RestaurantMenuAdapter
import com.aniket.myrestaurants.database.OrderEntity
import com.aniket.myrestaurants.database.RestaurantDatabase
import com.aniket.myrestaurants.fragment.RestaurantDetailsFragment
import com.aniket.myrestaurants.model.FoodItem
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    lateinit var toolbar : Toolbar
    lateinit var recyclerCart : RecyclerView
    lateinit var cartItemAdapter: CartItemAdapter
    lateinit var rlLoading : RelativeLayout
    lateinit var rlCart: RelativeLayout
    lateinit var txtResName : TextView
    lateinit var btnPlaceholder: Button
    lateinit var sharedPreferences: SharedPreferences
    var resId: Int = 0
    var resName: String = "Res"
    var orderList = ArrayList<FoodItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        init()
        setupToolbar()
        setUpCartList()
        placeOrder()
    }

    private fun init(){
        rlLoading = findViewById(R.id.rlLayout)
        rlCart = findViewById(R.id.rlCart)
        txtResName = findViewById(R.id.txtCardResName)
        txtResName.text = RestaurantDetailsFragment.resName

        val bundle = intent.getBundleExtra("data")
        resId = bundle?.getInt("resId", 0) as Int
        resName = bundle.getString("resName", "any") as String

    }

    private fun setupToolbar(){
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun setUpCartList() {
        recyclerCart = findViewById(R.id.recyclerCardItems)
        val dbList = GetItemsFromDBAsync(applicationContext).execute().get()
        Log.d("unex", dbList.toString())
        /*Extracting the data saved in database and then using Gson to convert the String of food items into a list
        * of food items*/
        for (element in dbList) {
            orderList.addAll(
                Gson().fromJson(element.foodItems, Array<FoodItem>::class.java).asList()
            )
        }


        /*If the order list extracted from DB is empty we do not display the cart*/
        if (orderList.isEmpty()) {
            rlCart.visibility = View.GONE
            rlLoading.visibility = View.VISIBLE

        } else {
            rlCart.visibility = View.VISIBLE
            rlLoading.visibility = View.GONE
        }

        /*Else we display the cart using the cart item adapter*/
        cartItemAdapter =  CartItemAdapter(orderList, this@CartActivity)
        val mLayoutManager = LinearLayoutManager(this@CartActivity,)
        recyclerCart.layoutManager = mLayoutManager
        recyclerCart.itemAnimator = DefaultItemAnimator()
        recyclerCart.adapter = cartItemAdapter

    }

    private fun placeOrder() {
        btnPlaceholder = findViewById(R.id.btnConfirmOrder)

        /*Before placing the order, the user is displayed the price or the items on the button for placing the orders*/
        var sum = 0
        for(i in 0 until orderList.size){
            sum += orderList[i].ItemCostForOne as Int
        }
        val total = "Place Order(Total : Rs. $sum)"
        btnPlaceholder.text = total

        btnPlaceholder.setOnClickListener{
            rlLoading.visibility = View.VISIBLE
            rlCart.visibility = View.INVISIBLE
            sendServerRequest()
        }
    }

    private fun sendServerRequest() {
        val queue = Volley.newRequestQueue(this@CartActivity)
        val url = "http://13.235.250.119/v2/place_order/fetch_result/"

        /*Creating the json object required for placing the order*/
        val jsonParams = JSONObject()
            sharedPreferences = getSharedPreferences(getString(R.string.Users_Data),Context.MODE_PRIVATE)
            val id =  sharedPreferences.getString("user_Id", null)as String
        //here we get user id from shared preferences
        jsonParams.put(
            "user_id", id)
//               Log.d("user_id",getSharedPreferences("Users_data",Context.MODE_PRIVATE).getString("user_id",null).toString())


        jsonParams.put("restaurant_id",RestaurantDetailsFragment.resId?.toString()as String)
        var sum = 0
        for(i in 0 until orderList.size) {
            sum += orderList[i].ItemCostForOne as Int
        }
        jsonParams.put("total_cost", sum.toString())

        val foodArray = JSONArray()
        for(i in 0 until orderList.size) {
            val foodId = JSONObject()
            foodId.put("food_item_id", orderList[i].Id)
            foodArray.put(i, foodId)
        }
        jsonParams.put("food", foodArray)
//        Log.d("user_id", jsonParams.toString())

        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                    if (success) {

                    /*If order is placed, clear the DB for the recently added items
                   * Once the DB is cleared, notify the user that the order has been placed*/

                        ClearDBAsync(applicationContext, resId.toString()).execute().get()
                        RestaurantMenuAdapter.isCartEmpty = true

                        Toast.makeText(this, "order placed successfully", Toast.LENGTH_SHORT).show()

//                        val intent = Intent(this@CartActivity, OrderPlaced::class.java)
//                        startActivity(intent)

                        //Here we have done something new. We used the Dialog class to display the order placed message
//                        It is just a neat trick to avoid creating a whole new activity for a very small purpose
                        val dialog = Dialog(
                            this@CartActivity,
                            android.R.style.Theme_Black_NoTitleBar_Fullscreen
                        )
                        dialog.setContentView(R.layout.order_placed_dialog)
                        dialog.setCancelable(false)
                        val btnOk = dialog.findViewById<Button>(R.id.btnOk)
                        dialog.show()

                        btnOk.setOnClickListener {
                            dialog.dismiss()
                            startActivity(Intent(this@CartActivity, MainActivity::class.java))
                            ActivityCompat.finishAffinity(this@CartActivity)
                        }
                    } else {
                        rlCart.visibility = View.VISIBLE
                        Toast.makeText(this@CartActivity, "Some Error occurred", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    rlCart.visibility = View.VISIBLE
                    e.printStackTrace()
                }
            }, Response.ErrorListener {
                rlCart.visibility = View.VISIBLE
                Toast.makeText(this@CartActivity, it.message, Toast.LENGTH_SHORT).show()

        })
            {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"

                    //The below used token will not work, kindly use the token provided to you in the training
                    headers["token"] = "e1a275a0db2e15"
                    return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    /*Async-task class for extracting the items from the database*/
    class GetItemsFromDBAsync(context: Context) : AsyncTask<Void, Void, List<OrderEntity>>() {
        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res_db").build()
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): List<OrderEntity> {
            val l = db.orderDao().getAllOrders()
            Log.d("safa", l.toString())
            return l
        }
    }

    /*Asynctask class for clearing the recently added items from the database*/
    class ClearDBAsync(context: Context, private val resId: String): AsyncTask<Void, Void, Boolean >(){

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res_db").build()

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): Boolean {
            db.orderDao().deleteOrders(resId)
            db.close()
            return true
        }

    }

        /*When the user presses back, we clear the cart so that when the returns to the cart, there is no
    * redundancy in the entries*/
        override fun onSupportNavigateUp(): Boolean {
            if (ClearDBAsync(applicationContext, resId.toString()).execute().get()) {
                RestaurantMenuAdapter.isCartEmpty = true
                onBackPressed()
                return true
            }
            return false
        }

        @Deprecated("Deprecated in Java")
        override fun onBackPressed(){
            ClearDBAsync(applicationContext, resId.toString()).execute().get()
            RestaurantMenuAdapter.isCartEmpty = true
            super.onBackPressed()
        }

    }