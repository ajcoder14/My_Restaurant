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

class LoginActivity : AppCompatActivity() {

        lateinit var edtPhone: EditText
        lateinit var edtPassword: EditText
        lateinit var btnLogin : Button
        lateinit var txtForgetPassword : TextView
        lateinit var txtRegister : TextView
        lateinit var sharedPreferences: SharedPreferences



        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)

            supportActionBar?.hide()

            edtPhone = findViewById(R.id.edtPhone)
            edtPassword = findViewById(R.id.edtPassword)
            btnLogin = findViewById(R.id.btnLogIn)
            txtForgetPassword = findViewById(R.id.txtForgetP)
            txtRegister = findViewById(R.id.txtRegister)

            sharedPreferences = getSharedPreferences(getString(R.string.Users_Data), Context.MODE_PRIVATE)

            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn",false)

            if(isLoggedIn){
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            //move to forget password page
            txtForgetPassword.setOnClickListener{
                val intent = Intent(this@LoginActivity, ForgetActivity::class.java)
                startActivity(intent)
            }

            //move to register page
            txtRegister.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

                btnLogin.setOnClickListener{
                    if(Validations().validateMobile(edtPhone.text.toString())){
                        edtPhone.error = null

                        if(Validations().validatePasswordLength(edtPassword.text.toString())){
                            edtPassword.error = null

                            //here we check  internet connection
                            if (ConnectionManager().checkConnectivity(this@LoginActivity)){
                              sendLoginRequest(
                                  edtPhone.text.toString(),
                                  edtPassword.text.toString()
                              )
                            }else{
                                Toast.makeText(
                                    this@LoginActivity,
                                    "No Internet Connection",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }else{
                            edtPassword.error = "Password Invalid"
                            Toast.makeText(
                                this@LoginActivity,
                                "Invalid Password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }else{
                        edtPhone.error = "Mobile no. Invalid"
                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid Mobile number",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }


        private fun sendLoginRequest(mobile_no:String, password:String){
            val queue = Volley.newRequestQueue(this)
            val url = "http://13.235.250.119/v2/login/fetch_result"

            val jsonParams = JSONObject()
            jsonParams.put("mobile_number",mobile_no)
            jsonParams.put("password", password)

            val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {


                    try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                        if(success) {

                            val response = data.getJSONObject("data")
                            sharedPreferences.edit()
                                .putString("user_Id", response.getString("user_id")).apply()

                            sharedPreferences.edit()
                                .putString("user_name", response.getString("name")).apply()

                            sharedPreferences.edit()
                                .putString("user_email", response.getString("email")).apply()

                            sharedPreferences.edit()
                                .putString("user_mobile_number", response.getString("mobile_number")).apply()

                            sharedPreferences.edit()
                                .putString("user_address", response.getString("address")).apply()

                            sharedPreferences.edit()
                                .putBoolean("isLoggedIn",true).apply()

                            Toast.makeText(this@LoginActivity, "Login  successfully", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)

                            finish()
                        }else{
                            Toast.makeText(this@LoginActivity, "Some error occurred to login", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e:Exception){
                        Toast.makeText(this@LoginActivity, "Some error occurred", Toast.LENGTH_SHORT).show()
                    }

                },Response.ErrorListener {
                    Toast.makeText(this@LoginActivity, "Volley error $it", Toast.LENGTH_SHORT).show()

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
