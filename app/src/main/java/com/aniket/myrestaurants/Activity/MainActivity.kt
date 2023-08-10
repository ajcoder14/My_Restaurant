package com.aniket.myrestaurants.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.room.Room
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.database.RestaurantDatabase
import com.aniket.myrestaurants.fragment.FaqsFragment
import com.aniket.myrestaurants.fragment.FavouriteRestaurantsFragment
import com.aniket.myrestaurants.fragment.HomeFragment
import com.aniket.myrestaurants.fragment.MyProfileFragment
import com.aniket.myrestaurants.fragment.OrderHistoryFragment
import com.aniket.myrestaurants.util.DrawerLocker
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), DrawerLocker {


    //for drawer setting implement from DrawerLocker
    override fun setDrawerEnabled(enabled: Boolean) {
        val lockMode = if (enabled)
            DrawerLayout.LOCK_MODE_UNLOCKED
        else
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED

        drawerLayout.setDrawerLockMode(lockMode)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = enabled
    }

    lateinit var drawerLayout: DrawerLayout
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var toolbar: Toolbar
    lateinit var frameLayout: FrameLayout
    lateinit var navigationView: NavigationView
    lateinit var txtProfileName : TextView
    lateinit var txtProfileNo : TextView
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle


    var previousMenuItem : MenuItem? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //to set text on navigation drawer we initialize here
        navigationView = findViewById(R.id.navigationView)
        //here create headerView
        val headerView: View = navigationView.getHeaderView(0)

        drawerLayout = findViewById(R.id.drawerLayout)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        toolbar = findViewById(R.id.toolbar)
        frameLayout = findViewById(R.id.frameLayout)
        txtProfileName = headerView.findViewById(R.id.txtProfileName)
        txtProfileNo = headerView.findViewById(R.id.txtProfileNumber)

        setUpToolbar()

        openHome()

        setupActionBarToggle()

        sharedPreferences = getSharedPreferences(getString(R.string.Users_Data), Context.MODE_PRIVATE)

        val name = sharedPreferences.getString("user_name", "Your Name")
        val number = sharedPreferences.getString("user_mobile_number", "123456789")

        txtProfileName.text = name
        txtProfileNo.text = " +91 $number"

//

        //it is set action listener on navigation menu
        navigationView.setNavigationItemSelectedListener {

            //we check which item is use for highlight in navigation menu item
            if(previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }

            it.isCheckable = true
            it.isChecked = true
            previousMenuItem = it

            when(it.itemId){
                R.id.itHome ->{
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, HomeFragment())
                        .commit()

                    supportActionBar?.title = "All Restaurants"
                    drawerLayout.closeDrawers()
                }
                R.id.itFavRestaurants -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, FavouriteRestaurantsFragment())
                        .commit()

                    supportActionBar?.title = " FavouriteRestaurants"
                    drawerLayout.closeDrawers()
                }
                R.id.itProfile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, MyProfileFragment())
                        .commit()

                    supportActionBar?.title = "My Profile"
                    drawerLayout.closeDrawers()
                }
                R.id.itOrderHistory -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, OrderHistoryFragment())
                        .commit()

                    supportActionBar?.title = "My Previous Orders"
                    drawerLayout.closeDrawers()
                }
                R.id.itFaq -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, FaqsFragment())
                        .commit()

                    supportActionBar?.title = "FAQ's"
                    drawerLayout.closeDrawers()
                }
                R.id.itLogOut -> {
                    /*Creating a confirmation dialog*/
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Confirmation")
                        .setMessage("Are you sure you want exit?")
                        .setPositiveButton("Yes") { _, _ ->
                            val editor = sharedPreferences.edit()

                            editor.clear()
                            editor.apply()

                            // here we clear our database before log out
                            DBRestaurant(this@MainActivity,  1 ).execute()
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            ActivityCompat.finishAffinity(this)
                        }
                        .setNegativeButton("No") { _, _ ->
                            val intent = Intent(this@MainActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .create()
                        .show()

                    supportActionBar?.title = "Log Out"
                    drawerLayout.closeDrawers()
                }
            }
            return@setNavigationItemSelectedListener true
        }

        //when we click on our name move to profile
        txtProfileName.setOnClickListener {
            val profileFragment = MyProfileFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout, profileFragment)
            transaction.commit()
            supportActionBar?.title = "My profile"
            val mPendingRunnable = Runnable { drawerLayout.closeDrawer(GravityCompat.START) }
            Handler().postDelayed(mPendingRunnable, 50)
        }

    }


    //for set up hamburger icon
    private fun setupActionBarToggle() {
        actionBarDrawerToggle = object :
            ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer) {
            override fun onDrawerStateChanged(newState: Int) {
                super.onDrawerStateChanged(newState)
                val pendingRunnable = Runnable {
                    val inputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                }

                /*delaying the closing of the navigation drawer for that the motion looks smooth*/
                Handler().postDelayed(pendingRunnable, 50)
            }
        }
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        //another way of drawer setting
//        var actionBarDrawerToggle = ActionBarDrawerToggle(
//            this@MainActivity,
//            drawerLayout,
//            R.string.open_drawer,
//            R.string.close_drawer
//        )

//        drawerLayout.addDrawerListener(actionBarDrawerToggle)
//        actionBarDrawerToggle.syncState()

    }
    private fun setUpToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Toolbar Title"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    //add action listener on home button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == android.R.id.home)
        {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openHome(){
        val fragment = HomeFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, HomeFragment())
        transaction.commit()
        supportActionBar?.title = "All Restaurants"
        navigationView.setCheckedItem(R.id.itHome)
    }

    // to avoid error is when we click on back button title should be match
    @Deprecated("Deprecated in Java")
    override fun onBackPressed(){
        val frag = supportFragmentManager.findFragmentById(R.id.frameLayout)

        when(frag){
            !is HomeFragment -> openHome()

            else -> super.onBackPressed()

        }
    }

    //create db for clear all data from db
    class DBRestaurant(context: Context, private val mode : Int)
        : AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res_db").build()

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {

                1 -> {
                    db.restaurantDao().clearData()
                    db.close()
                    return true
                }

            }
            return false

        }

    }
}