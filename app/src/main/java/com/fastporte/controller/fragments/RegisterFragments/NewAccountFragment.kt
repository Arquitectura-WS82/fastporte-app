package com.fastporte.controller.fragments.RegisterFragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.fastporte.Interface.RegisterInterface
import com.fastporte.controller.activities.LoginActivity
import com.fastporte.R
import com.fastporte.helpers.BaseURL
import com.fastporte.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewAccountFragment : Fragment() {
    lateinit var clientUser: User
    lateinit var driverUser: User
    private lateinit var txtUsername: EditText
    private lateinit var txtUserDescription: EditText
    private lateinit var checkboxConditions: CheckBox
    private lateinit var checkboxInformation: CheckBox
    private lateinit var btnSignUp: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_new_account, container, false)

        val termsConditions = view.findViewById<Button>(R.id.btTermsConditions)

        termsConditions.setOnClickListener {
            val url =
                "https://www.freeprivacypolicy.com/live/8c9bf7ae-7d09-424d-8243-d6f04cc8c058"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        initializeViews(view)
        setupTextWatchers()
        // Inflate the layout for this fragment
        return view
    }

    private fun initializeViews(view: View) {
        btnSignUp = view.findViewById(R.id.btnSignUp)
        txtUsername = view.findViewById(R.id.txtUsername)
        txtUserDescription = view.findViewById(R.id.txtUserDescription)
        checkboxConditions = view.findViewById(R.id.checkboxConditions)
        checkboxInformation = view.findViewById(R.id.checkboxInformation)

        // Filtro para no permitir espacios ni caracteres especiales en el campo de username
        txtUsername.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (!Character.isLetter(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        })

        btnSignUp.setOnClickListener {
            val username = txtUsername.text.toString()
            val userDescription = txtUserDescription.text.toString()
            val conditionsChecked = checkboxConditions.isChecked
            val informationChecked = checkboxInformation.isChecked

            if (username.isEmpty() || userDescription.isEmpty() || !conditionsChecked ) {
                Toast.makeText(
                    context,
                    "Debe rellenar y marcar todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val tempInfoUser = arguments?.getSerializable("tempInfoUser") as User
                val userTypeText = arguments?.getString("userType")

                val url = BaseURL.BASE_URL.toString() + "api/";

                val retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val registerService: RegisterInterface =
                    retrofit.create(RegisterInterface::class.java)

                if (userTypeText == "client") {
                    clientUser = User(
                        tempInfoUser.birthdate,
                        txtUserDescription.text.toString(),
                        tempInfoUser.email,
                        tempInfoUser.id,
                        tempInfoUser.name,
                        tempInfoUser.lastname,
                        txtUsername.text.toString(),
                        tempInfoUser.phone,
                        tempInfoUser.region,
                        tempInfoUser.password,
                        "https://static.vecteezy.com/system/resources/previews/005/544/718/original/profile-icon-design-free-vector.jpg"
                    )

                    Log.d("NewAccountFragment", clientUser.toString())

                    registerService.registerClient(clientUser).enqueue(object : Callback<User> {
                        override fun onResponse(call: Call<User>, response: Response<User>) {
                            if (response.isSuccessful) {
                                val loginIntent = Intent(context, LoginActivity::class.java)
                                startActivity(loginIntent)
                            }
                        }

                        override fun onFailure(call: Call<User>, t: Throwable) {
                            TODO("Not yet implemented")
                        }
                    })
                } else {
                    driverUser = User(
                        tempInfoUser.birthdate,
                        txtUserDescription.text.toString(),
                        tempInfoUser.email,
                        tempInfoUser.id,
                        tempInfoUser.name,
                        tempInfoUser.lastname,
                        txtUsername.text.toString(),
                        tempInfoUser.phone,
                        tempInfoUser.region,
                        tempInfoUser.password,
                        "https://static.vecteezy.com/system/resources/previews/005/544/718/original/profile-icon-design-free-vector.jpg"
                    )
                    registerService.registerDriver(driverUser).enqueue(object : Callback<User> {
                        override fun onResponse(call: Call<User>, response: Response<User>) {
                            if (response.isSuccessful) {
                                val loginIntent = Intent(context, LoginActivity::class.java)
                                startActivity(loginIntent)
                            }
                        }

                        override fun onFailure(call: Call<User>, t: Throwable) {
                            TODO("Not yet implemented")
                        }
                    })
                }
            }
        }
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                btnSignUp.isEnabled = validateFields()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        txtUsername.addTextChangedListener(watcher)
        txtUserDescription.addTextChangedListener(watcher)
        checkboxConditions.setOnCheckedChangeListener { _, _ -> btnSignUp.isEnabled = validateFields() }
        checkboxInformation.setOnCheckedChangeListener { _, _ -> btnSignUp.isEnabled = validateFields() }
    }

    private fun validateFields(): Boolean {
        val username = txtUsername.text.toString()
        val userDescription = txtUserDescription.text.toString()
        val conditionsChecked = checkboxConditions.isChecked

        return username.isNotEmpty() && userDescription.isNotEmpty() && conditionsChecked
    }
}