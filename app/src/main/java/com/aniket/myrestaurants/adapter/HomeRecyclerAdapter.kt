package com.aniket.myrestaurants.adapter

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.database.RestaurantDatabase
import com.aniket.myrestaurants.database.RestaurantEntity
import com.aniket.myrestaurants.fragment.RestaurantDetailsFragment
import com.aniket.myrestaurants.model.Restaurants
import com.squareup.picasso.Picasso


class HomeRecyclerAdapter(val context: Context, private val itemList: ArrayList<Restaurants>) : RecyclerView.Adapter<HomeRecyclerAdapter.HomeViewHolder>() {

    class HomeViewHolder(view : View) : RecyclerView.ViewHolder(view){

        val txtRestaurantName : TextView = view.findViewById(R.id.txtRestaurantsName)
        val txtPrice : TextView = view.findViewById(R.id.txtPrice)
        val txtRestaurantRating : TextView = view.findViewById(R.id.txtRestaurantRating)
        val imgRestaurant : ImageView = view.findViewById(R.id.imgRestaurants)
        val imgFavRestaurants : ImageView = view.findViewById(R.id.imgFavourite)
        val mainContent : LinearLayout = view.findViewById(R.id.mainContent)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): HomeViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.recycler_home_single_row, p0, false)

        return HomeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(p0: HomeViewHolder, p1: Int) {
        val restaurant = itemList[p1]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            p0.imgRestaurant.clipToOutline = true
    }
        p0.txtRestaurantName.text = restaurant.restaurantName
        p0.txtPrice.text = restaurant.restaurantPrice.toString()
        p0.txtRestaurantRating.text = restaurant.restaurantRating
        Picasso.get().load(restaurant.restaurantImage).error(R.drawable.restaurant_img).into(p0.imgRestaurant)

        val listOffFavourites = GetAllFavAsyncTask(context).execute().get()

        if(listOffFavourites.isNotEmpty() && listOffFavourites.contains(restaurant.restaurantId.toString())) {
            p0.imgFavRestaurants.setImageResource(R.drawable.ic_favourite)
        } else{
            p0.imgFavRestaurants.setImageResource(R.drawable.ic_non_favourite)
        }

        p0.imgFavRestaurants.setOnClickListener{
            val restaurantEntity =  RestaurantEntity(
                restaurant.restaurantId,
                restaurant.restaurantName,
                restaurant.restaurantPrice.toString(),
                restaurant.restaurantRating,
                restaurant.restaurantImage
            )

            if(!DBAsyncTask(context,restaurantEntity,1).execute().get()){
                val async = DBAsyncTask(context, restaurantEntity, 2).execute()
                val result = async.get()

                if(result){
                    p0.imgFavRestaurants.setImageResource(R.drawable.ic_favourite)
                }
            } else{
                val async = DBAsyncTask(context, restaurantEntity, 3).execute()
                val result = async.get()

                if(result){
                    p0.imgFavRestaurants.setImageResource(R.drawable.ic_non_favourite)
                }
            }
        }

            //To move on restaurant details page
            p0.mainContent.setOnClickListener {
                val fragment = RestaurantDetailsFragment()
                val args = Bundle()
                args.putInt("id", restaurant.restaurantId)
                args.putString("name", restaurant.restaurantName)
                fragment.arguments = args
                val transaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frameLayout, fragment)
                transaction.commit()
                (context as AppCompatActivity).supportActionBar?.title = p0.txtRestaurantName.text.toString()
            }
        }

    class DBAsyncTask(context: Context, private val restaurantEntity: RestaurantEntity, private val mode: Int): AsyncTask<Void, Void, Boolean> () {
        /* mode 1 = Check DB if the restaurant is add or not
           mode 2 = add the fav. restaurant in database
           mode 3 = if restaurant already added then remove it
        */

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res_db").build()

        //it used to follow particular command like insert , delete data etc.
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {

                1 -> {
                    val restaurant: RestaurantEntity? =
                        db.restaurantDao().getRestaurantId(restaurantEntity.restaurant_Id.toString())
                    db.close()
                    return restaurant != null
                }

                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }

                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }

            return false
        }

            /*Since the outcome of the above background method is always a boolean, we cannot use the above here.
            * We require the list of favourite restaurants here and hence the outcome would be list.
            * For simplicity we obtain the list of restaurants and then extract their ids which is then compared to the ids
            * inside the list sent to the adapter */}
        class GetAllFavAsyncTask(
            context: Context
        ) :
            AsyncTask<Void, Void, List<String>>() {

            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res_db").build()
            override fun doInBackground(vararg params: Void?): List<String> {

                val list = db.restaurantDao().getAllRestaurants()
                val listOfIds = arrayListOf<String>()
                for (i in list) {
                    listOfIds.add(i.restaurant_Id.toString())
                }
                return listOfIds
            }


    }

}