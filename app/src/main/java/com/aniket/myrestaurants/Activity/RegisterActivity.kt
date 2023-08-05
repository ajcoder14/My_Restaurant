package com.aniket.myrestaurants.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import android.widget.Toolbar
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.util.Validations
import com.aniket.myrestaurants.util.ConnectionManager
import com.google.android.gms.cast.framework.SessionManager
import org.json.JSONObject


class RegisterActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var btnRegister: Button
    lateinit var etName : EditText
    lateinit var etPhoneNumber : EditText
    lateinit var etPassword : EditText
    lateinit var etEmail : EditText
    lateinit var etAddress : EditText
    lateinit var etConfirmPassword : EditText
    lateinit var progressBar: ProgressBar
    lateinit var rlRegister: RelativeLayout
    lateinit var sessionManager: SessionManager
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //we set here shared preference for save our user data
        sharedPreferences = getSharedPreferences(getString(R.string.Users_Data), Context.MODE_PRIVATE)


        rlRegister = findViewById(R.id.rlRegister)
        etName = findViewById(R.id.edtName)
        etEmail = findViewById(R.id.edtEmail)
        etPhoneNumber = findViewById(R.id.edtPhone)
        etAddress = findViewById(R.id.edtAddress)
        etPassword = findViewById(R.id.edtPassword)
        etConfirmPassword = findViewById(R.id.edtConfirmPassword)
        btnRegister = findViewById(R.id.btnSingUp)
        progressBar = findViewById(R.id.progressBar)

        rlRegister.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE

        supportActionBar?.hide()
        btnRegister.setOnClickListener {

            rlRegister.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE

            if (Validations().validateNameLength(etName.text.toString())) {
                etName.error = null
                if (Validations().validateEmail(etEmail.text.toString())) {
                    etEmail.error = null
                    if (Validations().validateMobile(etPhoneNumber.text.toString())) {
                        etPhoneNumber.error = null
                        if (Validations().validatePasswordLength(etPassword.text.toString())) {
                            etPassword.error = null
                            if (Validations().matchPassword(
                                    etPassword.text.toString(),
                                    etConfirmPassword.text.toString()
                                )
                            ) {
                                etPassword.error = null
                                etConfirmPassword.error = null

                                if (ConnectionManager().checkConnectivity(this@RegisterActivity)) {
                                    sendRegisterRequest(
                                        etName.text.toString(),
                                        etPhoneNumber.text.toString(),
                                        etAddress.text.toString(),
                                        etPassword.text.toString(),
                                        etEmail.text.toString()
                                    )
                                } else {
                                    rlRegister.visibility = View.VISIBLE
                                    progressBar.visibility = View.INVISIBLE
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "No Internet",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                rlRegister.visibility = View.VISIBLE
                                progressBar.visibility = View.INVISIBLE
                                etPassword.error = "Passwords don't match"
                                etConfirmPassword.error = "Passwords don't match"
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Passwords don't match",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        } else {
                            rlRegister.visibility = View.VISIBLE
                            progressBar.visibility = View.INVISIBLE
                            etPassword.error = "Password should be more than or equal 4 digits"
                            Toast.makeText(
                                this@RegisterActivity,
                                "Password should be more than or equal 4 digits",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        rlRegister.visibility = View.VISIBLE
                        progressBar.visibility = View.INVISIBLE
                        etPhoneNumber.error = "Invalid Mobile number"
                        Toast.makeText(
                            this@RegisterActivity,
                            "Invalid Mobile number",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    rlRegister.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                    etEmail.error = "Invalid email"
                    Toast.makeText(this@RegisterActivity, "Invalid email", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                rlRegister.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
                etName.error = "Invalid Name"
                Toast.makeText(this@RegisterActivity, "Invalid Name", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun sendRegisterRequest(name: String, Phone: String, address: String, password:String, email:String){

            val queue = Volley.newRequestQueue(this)
            val url = "http://13.235.250.119/v2/register/fetch_result"
            //here we create json object to pass data with post request
            val jsonParams = JSONObject()
            jsonParams.put("name",name)
            jsonParams.put("mobile_number",Phone)
            jsonParams.put("password",password)
            jsonParams.put("address",address)
            jsonParams.put("email",email)

            val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {

                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if(success){
                        val response = data.getJSONObject("data")
                        sharedPreferences.edit()
                            .putString("user_Id", response.getString("user_id")).apply()
                        sharedPreferences.edit()
                            .putString("user_name", response.getString("name")).apply()
                        sharedPreferences.edit()
                            .putString("user_mobile_number", response.getString("mobile_number")).apply()
                        sharedPreferences.edit()
                            .putString("user_address", response.getString("address")).apply()
                        sharedPreferences.edit()
                            .putString("user_email", response.getString("email")).apply()

                        Toast.makeText(this@RegisterActivity, "Registered  successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)

                        finish()

                    }else{
                        rlRegister.visibility = View.VISIBLE
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(this@RegisterActivity, "some error occurred to register",Toast.LENGTH_SHORT).show()
                    }
                } catch (e:Exception){
                    rlRegister.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                    e.printStackTrace()
                    Toast.makeText(this@RegisterActivity, "some error occurred",Toast.LENGTH_SHORT).show()
                }

                }, Response.ErrorListener {
                    Toast.makeText(
                        this@RegisterActivity , "Volley error $it", Toast.LENGTH_SHORT).show()
                    rlRegister.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
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

    override fun onSupportNavigateUp(): Boolean {
        Volley.newRequestQueue(this).cancelAll(this::class.java.simpleName)
        onBackPressed()
        return true
    }
    }


