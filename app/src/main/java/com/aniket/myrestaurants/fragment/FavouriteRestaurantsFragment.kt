package com.aniket.myrestaurants.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.adapter.FavouriteRecyclerAdapter
import com.aniket.myrestaurants.database.RestaurantDatabase
import com.aniket.myrestaurants.database.RestaurantEntity


class FavouriteRestaurantsFragment : Fragment() {

    lateinit var recyclerFavourite : RecyclerView
    lateinit var imgFavRest : ImageView
    lateinit var imgLayout: RelativeLayout
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: FavouriteRecyclerAdapter
    var dbRestaurantList = listOf<RestaurantEntity>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_favourite_restaurants, container, false)

        recyclerFavourite = view.findViewById(R.id.recyclerFavourite)
        imgFavRest = view.findViewById(R.id.imgFavRest)
        imgLayout = view.findViewById(R.id.imgLayout)
        imgLayout.visibility = View.VISIBLE

        layoutManager = LinearLayoutManager(activity as Context)

        dbRestaurantList = RetrieveFavourites(context as Context).execute().get()

        val favRestaurantCnt = dbRestaurantList.size

        if (activity != null){
            if(favRestaurantCnt == 0){
                imgLayout.visibility = View.VISIBLE

            }else{
                imgLayout.visibility = View.GONE
            }

            recyclerAdapter = FavouriteRecyclerAdapter(context as Context , dbRestaurantList)

            recyclerFavourite.adapter = recyclerAdapter

            recyclerFavourite.layoutManager = layoutManager

        }
        return view
    }


    class RetrieveFavourites(val context : Context): AsyncTask<Void, Void, List<RestaurantEntity>>(){

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg pO : Void?): List<RestaurantEntity> {
            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java,"res_db").build()

            //here we call getAllBooks method from Dao to fetch all books
            return db.restaurantDao().getAllRestaurants()
        }
    }

}