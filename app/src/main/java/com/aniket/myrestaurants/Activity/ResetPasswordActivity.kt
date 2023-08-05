package com.aniket.myrestaurants.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.util.Validations
import com.aniket.myrestaurants.util.ConnectionManager
import org.json.JSONObject
import java.lang.Exception

class ResetPasswordActivity : AppCompatActivity() {

    lateinit var etOtp: EditText
    lateinit var etPassword: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var btnReset: Button
    lateinit var txtMessage : TextView
    lateinit var sharedPreferences: SharedPreferences

    var mobile_no:String? = "12345"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        supportActionBar?.hide()

        etOtp = findViewById(R.id.edtOtp)
        etPassword = findViewById(R.id.edtPassword)
        etConfirmPassword = findViewById(R.id.edtConfirmPassword)
        btnReset = findViewById(R.id.btnReset)
        txtMessage = findViewById(R.id.txtMessage)

        sharedPreferences = getSharedPreferences(getString(R.string.Users_Data), Context.MODE_PRIVATE)


        if(intent != null){
            mobile_no = intent.getStringExtra("mobile_number")
        }else{
            finish()
            Toast.makeText(this@ResetPasswordActivity, "Some unexpected error occurred",Toast.LENGTH_SHORT).show()
        }

        if(mobile_no == "12345"){
            finish()
            Toast.makeText(this@ResetPasswordActivity, "Some unexpected error occurred",Toast.LENGTH_SHORT).show()
        }

        btnReset.setOnClickListener {
            if (Validations().validOtp(etOtp.text.toString())) {
                etOtp.error = null
                if (Validations().validatePasswordLength(etPassword.text.toString())) {
                    etPassword.error = null
                    if (Validations().matchPassword(
                            etPassword.text.toString(),
                            etConfirmPassword.text.toString()
                        )
                    ) {
                        etPassword.error = null
                        etConfirmPassword.error = null

                        if (ConnectionManager().checkConnectivity(this@ResetPasswordActivity)) {
                            sendResetRequest(
                                etOtp.text.toString(),
                                etPassword.text.toString(),
                                mobile_no.toString()
                            )
                        } else {
                            Toast.makeText(
                                this@ResetPasswordActivity,
                                "No Internet",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        etPassword.error = "Passwords don't match"
                        etConfirmPassword.error = "Passwords don't match"
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Passwords don't match",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    etPassword.error = "Passwords Invalid"
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Passwords Invalid",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                etOtp.error = "Invalid otp"
                Toast.makeText(
                    this@ResetPasswordActivity,
                    "Invalid otp",
                    Toast.LENGTH_SHORT
                ) .show()
            }
        }
    }

    private fun sendResetRequest(otp:String, password: String, mobileNo:String){
        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v2/reset_password/fetch_result"

        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNo)
        jsonParams.put("password", password)
        jsonParams.put("otp", otp)

        val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {

                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")

                        if (success) {
                            val message = data.getString("successMessage")

                            txtMessage.text = message


                            Toast.makeText(this, "password reset successfully", Toast.LENGTH_SHORT)
                                .show()


                            sharedPreferences.edit().clear().apply()

                            val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                            startActivity(intent)

                            finish()

                        } else {
                            Toast.makeText(
                                this@ResetPasswordActivity,
                                "Some error occurred to reset",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }catch (e:Exception){
                        Toast.makeText(this@ResetPasswordActivity, " Some error occurred", Toast.LENGTH_SHORT).show()

                    }

                }, Response.ErrorListener {
                    Toast.makeText(this@ResetPasswordActivity, "Volley error $it", Toast.LENGTH_SHORT).show()
                })
                {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "e1a275a0db2e15"
                        return headers
                    }
                }
        queue.add(jsonObjectRequest)


    }

}


