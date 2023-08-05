package com.aniket.myrestaurants.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.aniket.myrestaurants.R


class LogOutFragment : Fragment() {

    private lateinit var btnLogOut : Button
    lateinit var btnCancel : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_log_out, container, false)

        return view
    }


}