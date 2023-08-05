package com.aniket.myrestaurants.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.aniket.myrestaurants.R

class MyProfileFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    lateinit var txtName:TextView
    lateinit var txtPhone:TextView
    lateinit var txtEmail:TextView
    lateinit var txtAddress:TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_my_profile, container, false)

        txtName = view.findViewById(R.id.txtName)
        txtPhone = view.findViewById(R.id.txtPhone)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtAddress = view.findViewById(R.id.txtAddress)

        //we use requireActivity() instead of (activity as context)
        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.Users_Data),Context.MODE_PRIVATE)

        val name = sharedPreferences.getString("user_name", "My Name")
        val phone = sharedPreferences.getString("user_mobile_number", "My Phone")
        val email = sharedPreferences.getString("user_email", "My Email")
        val address = sharedPreferences.getString("user_address", "My Address")

        //temporary use
        txtName.text = name
        txtPhone.text = phone
        txtEmail.text = email
        txtAddress.text = address
        return view
    }

}