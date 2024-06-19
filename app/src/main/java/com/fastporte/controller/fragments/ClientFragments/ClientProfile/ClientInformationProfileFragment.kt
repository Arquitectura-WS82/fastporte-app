package com.fastporte.controller.fragments.ClientFragments.ClientProfile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fastporte.R
import com.fastporte.helpers.BaseURL
import com.fastporte.helpers.General
import com.fastporte.helpers.SharedPreferences
import com.fastporte.models.User
import com.fastporte.network.ProfileService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar

class ClientInformationProfileFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_information_profile, container, false)

        loadData(view)
        return view
    }

    private fun loadData(view: View) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseURL.BASE_URL.toString())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val profileService: ProfileService = retrofit.create(ProfileService::class.java)

        val request = profileService.getClientProfile(
            id = SharedPreferences(view.context).getValue("id")!!.toInt(),
            format = "json"
        )

        request.enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d("profileInformationFragment", t.toString())
            }

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    showData(response.body()!!)
                }
            }
        })
    }

    private fun showData(user: User) {

        val btName = view?.findViewById<TextView>(R.id.btName)
        val btAge = view?.findViewById<TextView>(R.id.btAge)
        val btEmail = view?.findViewById<TextView>(R.id.btEmail)
        val btPhone = view?.findViewById<TextView>(R.id.btPhone)

        btName?.text = user.name
        btAge?.text = General.calculateAge(user.birthdate).toString()
        btEmail?.text = user.email
        btPhone?.text = user.phone

    }

}