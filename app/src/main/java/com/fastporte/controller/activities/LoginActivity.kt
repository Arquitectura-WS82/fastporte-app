package com.fastporte.controller.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.startActivity
import com.fastporte.R
import com.fastporte.helpers.BaseURL
import com.fastporte.network.ClientsService
import com.fastporte.network.DriversService
import com.fastporte.helpers.SharedPreferences
import com.fastporte.models.User
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var radioGroup: RadioGroup
    private var typeUser: String = "client"
    private lateinit var userEmail : EditText
    private lateinit var userPassword : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        inputsConstraint(findViewById(R.id.btn_login))
        val sharedPreferences = SharedPreferences(this)
        sharedPreferences.removeValue("typeUser")
        sharedPreferences.removeValue("id")
        sharedPreferences.removeValue("fullName")
        val login = findViewById<Button>(R.id.btn_login)

        login.setOnClickListener {
            login()
        }

        val forgot = findViewById<TextView>(R.id.tvpassword)
        forgot.setOnClickListener {
            val forgotIntent = Intent(this, PasswordActivity::class.java)
            startActivity(forgotIntent)
        }

        val create = findViewById<TextView>(R.id.tvCreate)
        create.setOnClickListener {
            val registerIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)
        }

        userEmail = findViewById(R.id.et_username)
        userPassword = findViewById(R.id.et_password)

        radioGroup = findViewById(R.id.rgTypeOfUser)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_client -> {
                    typeUser = "client"
                    userEmail.setText("abotello@gmail.com")
                    userPassword.setText("123123")
                    Log.d("LoginActivity", typeUser)

                }
                R.id.rb_driver -> {
                    typeUser = "driver"
                    userEmail.setText("rodalex@gmail.com")
                    userPassword.setText("123123")
                    Log.d("LoginActivity", typeUser)
                }
            }
        }

        toolbar = findViewById(R.id.myPreToolBar)
        setSupportActionBar(toolbar)
        register()

    }

    @SuppressLint("CutPasteId")
    private fun login() {

        val url = BaseURL.BASE_URL.toString() + "api/";

        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        if(typeUser == "client") {
            val clientService: ClientsService = retrofit.create(ClientsService::class.java)
            //val listClient = clientService.getClient("json")
            val clientTmp = clientService.searchEmailPassword(userEmail.text.toString(), userPassword.text.toString())

            clientTmp.enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    val user = response.body()
                    if (user != null) {
                        clientIntent(user.id, user.name, user.lastname)
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error in the request", Toast.LENGTH_SHORT).show()
                    Log.d("LoginActivity Client", t.toString())
                    Log.d("LoginActivity Client", t.message.toString())
                    Log.d("LoginActivity Client", "URL: " + call.request().url.toString())
                }
            })

        } else if (typeUser == "driver") {
            val driverService: DriversService = retrofit.create(DriversService::class.java)
            //val listDriver = driverService.getDriver("json")
            val driverTmp = driverService.searchEmailPassword(userEmail.text.toString(), userPassword.text.toString())

            driverTmp.enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    val user = response.body()
                    if (user != null) {
                        driverIntent(user.id, user.name, user.lastname)
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error in the request", Toast.LENGTH_SHORT).show()
                    Log.d("LoginActivity Driver", t.toString())
                }
            })

            /*listDriver.enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    val userList = response.body()
                    if (userList != null) {
                        for (user in userList) {
                            if (userEmail.text.toString() == user.email && userPassword.text.toString() == user.password) {
                                driverIntent(user.id, user.name, user.lastname)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Log.d("LoginActivity Driver", t.toString())
                }
            })*/
        }

    }

    private fun clientIntent(id: Int, name: String, lastName: String) {
        val clientIntent = Intent(this, ClientActivity::class.java)
        saveSharedPreferences("id", id.toString())
        saveSharedPreferences("typeUser", "client")
        saveSharedPreferences("fullName", "$name $lastName")
        val etPassword = findViewById<EditText>(R.id.et_password)
        etPassword.text.clear()
        startActivity(clientIntent)
    }

    private fun driverIntent(id: Int, name: String, lastName: String) {
        val carrierIntent = Intent(this, CarrierActivity::class.java)
        saveSharedPreferences("id", id.toString())
        saveSharedPreferences("typeUser", "driver")
        saveSharedPreferences("fullName", "$name $lastName")
        val etPassword = findViewById<EditText>(R.id.et_password)
        etPassword.text.clear()
        startActivity(carrierIntent)
    }

    private fun saveSharedPreferences(KeyName: String, value: String) {
        val sharedPreferences = SharedPreferences(this)
        sharedPreferences.removeValue(KeyName)
        sharedPreferences.save(KeyName, value)
    }

    private fun register() {
        val tvCreateAccount = findViewById<TextView>(R.id.tvCreateAccount)
        val registerActivity = Intent(this, RegisterActivity::class.java)
        tvCreateAccount.setOnClickListener {
            startActivity(registerActivity)
        }
    }

    private fun inputsConstraint(view: View){
        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        btnLogin.isEnabled = false

        etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    etUsername.error = "This field can't be empty"
                    btnLogin.isEnabled = false
                } else {
                    etUsername.error = null
                    if (!etUsername.text.isNullOrEmpty() && !etPassword.text.isNullOrEmpty()) {
                        btnLogin.isEnabled = true
                    }
                }
            }
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    etPassword.error = "This field can't be empty"
                    btnLogin.isEnabled = false
                } else {
                    etPassword.error = null
                    if (!etUsername.text.isNullOrEmpty() && !etPassword.text.isNullOrEmpty()) {
                        btnLogin.isEnabled = true
                    }
                }
            }
        })

    }
}