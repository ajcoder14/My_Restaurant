package com.aniket.myrestaurants.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.aniket.myrestaurants.Activity.CartActivity
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.adapter.RestaurantMenuAdapter
import com.aniket.myrestaurants.database.OrderEntity
import com.aniket.myrestaurants.database.RestaurantDatabase
import com.aniket.myrestaurants.model.FoodItem
import com.aniket.myrestaurants.util.ConnectionManager
import com.google.gson.Gson

class RestaurantDetailsFragment : Fragment() {

    private lateinit var recyclerMenu: RecyclerView
    private lateinit var restaurantMenuAdapter: RestaurantMenuAdapter
    private var menuList = arrayListOf<FoodItem>()
    private lateinit var rlLoading: RelativeLayout
    private var orderList = arrayListOf<FoodItem>()
    lateinit var sharedPreferences: SharedPreferences


    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var goToCart: Button
        var resId: Int? = 0
        var resName: String? = ""
    }

    //for add home button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_restaurant_details, container, false)


        sharedPreferences =
            activity?.getSharedPreferences("Users_data", Context.MODE_PRIVATE) as SharedPreferences

        rlLoading = view?. findViewById(R.id.rlLayout) as RelativeLayout
        rlLoading.visibility = View.VISIBLE
        resId = arguments?.getInt("id", 0)
        resName = arguments?.getString("name", "any")
//        (activity as DrawerLocker).setDrawerEnabled(false)
        setHasOptionsMenu(true)
        goToCart = view.findViewById(R.id.btnGoToCart)  as Button
        goToCart.visibility = View.GONE

        goToCart.setOnClickListener{
            proceedToCart()
        }

        setUpRestaurantMenu(view)


//        //it is used to handle the back press
//        val callBack = object : OnBackPressedCallback(true)
//        {
//            override fun handleOnBackPressed() {
//                val builder = androidx.appcompat.app.AlertDialog.Builder(context as Context)
//                builder.setTitle("Confirm")
//                builder.setMessage("If your Exit Your all items are reset!!")
//                    .setPositiveButton("Yes"){ _ , _ ->
//                        requireActivity().finish()
//                    }
//                    .setNegativeButton("Cancel"){_ , _ ->
//
//                    }
//                    .create()
//                    .show()
//            }
//        }
//        requireActivity().onBackPressedDispatcher.addCallback(callBack)

        return view
    }



    private fun setUpRestaurantMenu(view : View){

        recyclerMenu = view.findViewById(R.id.recyclerMenuItem)
        if(ConnectionManager().checkConnectivity(activity as Context)){
            val queue = Volley.newRequestQueue(context as Context)
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

            val jsonObjectRequest =
                //here we pass restaurant id with url
                object : JsonObjectRequest(Request.Method.GET, url + resId, null, Response.Listener {
                    rlLoading.visibility = View.GONE

                    try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                        if(success){

                            val resArray = data.getJSONArray("data")
                            for(i in 0 until resArray.length()){
                                val foodJsonObject = resArray.getJSONObject(i)

                                val foodItem = FoodItem(
                                    foodJsonObject.getString("id"),
                                    foodJsonObject.getString("name"),
                                    foodJsonObject.getString("cost_for_one").toInt()
                                )
                                menuList.add(foodItem)

                                restaurantMenuAdapter = RestaurantMenuAdapter(activity as Context, menuList,
                                    object : RestaurantMenuAdapter.OnItemClickListener {
                                        override fun onAddItemClick(foodItem: FoodItem) {
                                            orderList.add(foodItem)
                                            if (orderList.size > 0) {
                                                goToCart.visibility = View.VISIBLE
                                                RestaurantMenuAdapter.isCartEmpty = false
                                            }
                                        }

                                        override fun onRemoveItemClick(foodItem: FoodItem) {
                                                orderList.remove(foodItem)
                                                if (orderList.isEmpty()) {
                                                    goToCart.visibility = View.GONE
                                                    RestaurantMenuAdapter.isCartEmpty = true
                                                }
                                        }
                                    })

                               val mLayoutManager = LinearLayoutManager(activity)
                                recyclerMenu.layoutManager = mLayoutManager
                                recyclerMenu.itemAnimator = DefaultItemAnimator()
                                recyclerMenu.adapter = restaurantMenuAdapter

                            }

                        } else {
                            // if response is not success
                            Toast.makeText(
                                activity as Context,
                                "some error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e : Exception){
                        e.printStackTrace()                    }

                }, Response.ErrorListener {
                    Toast.makeText( activity as Context, "Volley Error occurred!!", Toast.LENGTH_SHORT).show()
                })
                {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers  ["Content-type"] = "application/json"
                        headers ["token"] = "e1a275a0db2e15"
                        return headers
                    }
                }
            queue.add(jsonObjectRequest)
        }
        else {
            Toast.makeText(activity as Context, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }

    }

        private fun proceedToCart(){
        /*Here we see the implementation of Gson.
        * Whenever we want to convert the custom data types into simple data types
        * which can be transferred across for utility purposes, we will use Gson*/

        val gson = Gson()
        /*With the below code, we convert the list of order items into simple string which can be easily stored in DB*/
        val foodItems = gson.toJson(orderList)

            val async =ItemsOfCart(activity as Context, resId.toString(), foodItems, 1).execute()
            val result = async.get()
            if(result){
                val data = Bundle()
                data.putInt("resId", resId as Int)
                data.putString("resName", resName)
                val intent = Intent(activity , CartActivity::class.java)
                intent.putExtra("data", data)
                startActivity(intent)
            } else {
                Toast.makeText((activity as Context), "Some unexpected error", Toast.LENGTH_SHORT)
                    .show()
            }

    }







    class ItemsOfCart(context: Context, private val restaurantId : String, private val foodItems : String, private val mode : Int)
        :AsyncTask<Void , Void , Boolean>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res_db").build()

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    db.orderDao().insertOrder(OrderEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }

                2 -> {
                    db.orderDao().deleteOrder(OrderEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }

            }
                return false

        }

    }
}