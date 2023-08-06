package com.aniket.myrestaurants.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.adapter.HomeRecyclerAdapter
import com.aniket.myrestaurants.model.Restaurants
import com.aniket.myrestaurants.util.ConnectionManager
import com.aniket.myrestaurants.util.DrawerLocker
import com.internshala.foodrunner.util.Sorter
import java.util.Collections


class HomeFragment : Fragment() {

    lateinit var recyclerHome: RecyclerView
    lateinit var layoutManager :RecyclerView.LayoutManager
    private lateinit var  recyclerAdapter : HomeRecyclerAdapter
    lateinit var progressLayout : RelativeLayout
    lateinit var progressBar : ProgressBar
    private var checkedItem: Int = -1

    private val restaurantsList = arrayListOf<Restaurants>( )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)


        recyclerHome = view.findViewById(R.id.recyclerHome)
        layoutManager = LinearLayoutManager(activity)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout = view.findViewById(R.id.progressLayout)

        (activity as DrawerLocker).setDrawerEnabled(true)

        progressLayout.visibility = View.VISIBLE

        setHasOptionsMenu(true)

        val queue = Volley.newRequestQueue(activity as Context)

        val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

        if(ConnectionManager().checkConnectivity(activity as Context)){
            val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {

                try {
                    progressLayout.visibility = View.GONE

                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                    if(success){

                            val responseData = data.getJSONArray("data")
                            for (i in 0 until responseData.length()){

                                val restaurantJsonObject = responseData.getJSONObject(i)
                                val restaurantObject = Restaurants(
                                    restaurantJsonObject.getString("id").toInt(),
                                    restaurantJsonObject.getString("name"),
                                    restaurantJsonObject.getString("rating"),
                                    restaurantJsonObject.getString("cost_for_one").toInt(),
                                    restaurantJsonObject.getString("image_url"),
                                )
                                restaurantsList.add(restaurantObject)

                                recyclerAdapter = HomeRecyclerAdapter(activity as Context, restaurantsList)

                                recyclerHome.adapter = recyclerAdapter

                                recyclerHome.layoutManager = layoutManager

                            }
                        } else {
                            // if response is not success
                            Toast.makeText(
                                activity as Context,
                                "some error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }  catch (e: Exception){
                        Toast.makeText(activity as Context,"some unexpected Error occurred!!",Toast.LENGTH_SHORT).show()
                    }

                }, Response.ErrorListener {
                    if (activity != null) {
                        Toast.makeText(activity as Context, "Volley Error occurred!!", Toast.LENGTH_SHORT).show()
                    }
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

        } else{
                //if internet is inactive
                val dialog = AlertDialog.Builder(activity as Context)
                dialog.setTitle("Error")
                dialog.setMessage("Internet connection not found")
                dialog.setPositiveButton("Open Settings") { text, listener ->
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    activity?.finish()
                }

                dialog.setNegativeButton("Exit") { text, listener ->
                    //when we press back button it exit from app
                    ActivityCompat.finishAffinity(activity as Activity)
                }
                dialog.create()
                dialog.show()
        }


        return (view)
    }

    //for set view on filter
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.sorter_menu, menu)
    }

    //for set  click listener on filter
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.actionSort-> showDialog(context as Context)
        }

        return super.onOptionsItemSelected(item)
    }

    //to set alert dialog for filter
        @SuppressLint("NotifyDataSetChanged")
        private fun showDialog(context: Context){

        val builder: androidx.appcompat.app.AlertDialog.Builder? = androidx.appcompat.app.AlertDialog.Builder(context)
        builder?.setTitle("Filter By:")
        builder?.setSingleChoiceItems(R.array.filters, checkedItem){ _, isChecked ->
            checkedItem= isChecked
        }
        builder?.setPositiveButton("ok"){ _,_ ->

            when(checkedItem){
                0 ->{
                    Collections.sort(restaurantsList, Sorter.costComparator)
                }

                1 ->{
                    Collections.sort(restaurantsList, Sorter.costComparator)
                    restaurantsList.reverse()
                }

                2-> {
                    Collections.sort(restaurantsList, Sorter.ratingComparator)
                    restaurantsList.reverse()
                }
            }
            recyclerAdapter.notifyDataSetChanged()

        }
            builder?.setNegativeButton("Cancel") { _, _ ->

            }
            builder?.create()
            builder?.show()

    }

}