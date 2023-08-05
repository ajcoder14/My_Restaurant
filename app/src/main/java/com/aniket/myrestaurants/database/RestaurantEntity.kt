package com.aniket.myrestaurants.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("restaurants")
data class RestaurantEntity(

    @PrimaryKey val restaurant_Id: Int,
    @ColumnInfo (name = "restaurant_name") val restaurantName: String,
    @ColumnInfo (name = "restaurant_price") val restaurantPrice: String,
    @ColumnInfo (name = "restaurant_rating") val restaurantRating: String,
    @ColumnInfo (name = "restaurant_image") val restaurantImage: String

)

