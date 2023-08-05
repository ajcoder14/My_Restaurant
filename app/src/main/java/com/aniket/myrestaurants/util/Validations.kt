package com.aniket.myrestaurants.util

import android.util.Patterns

class Validations {


    fun validateMobile(mobile: String): Boolean {
        return mobile.length == 10
    }


    fun validatePasswordLength(password: String): Boolean {
        return password.length >= 4
    }


    fun validateNameLength(name: String): Boolean {
        return name.length >= 3
    }


    fun matchPassword(pass: String, confirmPass: String): Boolean {
        return pass == confirmPass
    }


    fun validateEmail(email: String): Boolean {
        return (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches())
    }

    fun validOtp(otp: String): Boolean{
        return otp.length == 4
    }
}