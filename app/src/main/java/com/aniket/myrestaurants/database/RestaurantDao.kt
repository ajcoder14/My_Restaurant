package com.aniket.myrestaurants.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.selects.select

@Dao
interface RestaurantDao {

    @Insert
    fun insertRestaurant(restaurantEntity: RestaurantEntity)

    @Delete
    fun deleteRestaurant(restaurantEntity: RestaurantEntity)

    @Query ("DELETE  FROM restaurants")
    fun clearData()

    @Query ("SElECT * FROM restaurants")
    fun getAllRestaurants() : List<RestaurantEntity>

    @Query ("SElECT * FROM restaurants WHERE restaurant_Id = :restaurantId")
    fun getRestaurantId(restaurantId : String) : RestaurantEntity
}