package com.aniket.myrestaurants.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.aniket.myrestaurants.R
import com.aniket.myrestaurants.util.Validations
import com.aniket.myrestaurants.util.ConnectionManager
import org.json.JSONObject

class ForgetActivity : AppCompatActivity() {

    lateinit var etEmail : EditText
    lateinit var etPhone : EditText
    lateinit var btnSubmit : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget)

        supportActionBar?.hide()

        etEmail = findViewById(R.id.edtEmail)
        etPhone = findViewById(R.id.edtPhone)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener{
            if(Validations().validateEmail(etEmail.text.toString())){
                etEmail.error = null
                if(Validations().validateMobile(etPhone.text.toString())){
                    etPhone.error = null
                    if (ConnectionManager().checkConnectivity(this@ForgetActivity)) {
                        sendForgetRequest(
                            etEmail.text.toString(),
                            etPhone.text.toString()
                        )
                    }else {
                        Toast.makeText(
                            this@ForgetActivity,
                            "No Internet",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    }else{
                     etPhone.error = "Invalid no."
                    Toast.makeText(
                        this@ForgetActivity,
                        "Invalid mobile no.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{

                    etEmail.error = "Invalid Email"
                    Toast.makeText(
                        this@ForgetActivity,
                        "Invalid email number",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

    private fun sendForgetRequest(email:String, mobile_no:String){
        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v2/forgot_password/fetch_result"

        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobile_no)
        jsonParams.put("email", email)

        val jsonObjectRequest =
            object : JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {

                try
                {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if(success){
                        val firstTry = data.getBoolean("first_try")
                        if(firstTry){
                            //here we move to reset password page and send mobile no. also
                            val intent = Intent(this@ForgetActivity, ResetPasswordActivity::class.java)
                            intent.putExtra("mobile_number",mobile_no)
                            startActivity(intent)
                        }else{
                            Toast.makeText(this, "sorry try again after 24 hours", Toast.LENGTH_SHORT).show()
//                            val intent = Intent(this@ForgetActivity, ResetPasswordActivity::class.java)
//                            intent.putExtra("mobile_number",mobile_no)
//                            startActivity(intent)
                        }

                    }else{
                        Toast.makeText(this@ForgetActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }catch (e:Exception){
                    Toast.makeText(this@ForgetActivity, " Some error occurred", Toast.LENGTH_SHORT).show()
                }

            },Response.ErrorListener {
                Toast.makeText(this@ForgetActivity, "Volley error $it", Toast.LENGTH_SHORT).show()

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
