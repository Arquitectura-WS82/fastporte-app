package com.fastporte.controller.fragments.CarrierFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fastporte.R
import com.fastporte.adapter.CarrierNotificationFinishAdapter
import com.fastporte.adapter.CarrierNotificationRequestAdapter
import com.fastporte.adapter.ClientNotificationsAdapter
import com.fastporte.adapter.ClientNotificationsDenniedAdapter
import com.fastporte.helpers.SharedPreferences
import com.fastporte.models.ClientNotification
import com.fastporte.models.DriverNotification
import com.fastporte.network.NotificationService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CarrierNotificationsFragment : Fragment(),CarrierNotificationFinishAdapter.NotificationAdapterFinishListener,CarrierNotificationRequestAdapter.NotificationAdapterRequestListener {
    lateinit var notificationRequestRecyclerView: RecyclerView
    lateinit var notificationFinishedRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_carrier_notifications, container, false)
        notificationRequestRecyclerView=view.findViewById(R.id.rv_carrier_notifications_request)
        notificationFinishedRecyclerView=view.findViewById(R.id.rv_carrier_notifications_finish)
        loadNotifications(view)
        return view
    }

    private fun loadNotifications(view_: View) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api-fastporte.azurewebsites.net/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(NotificationService::class.java)
        val request = service.getDriverNotifiacations(SharedPreferences(view_.context).getValue("id")!!.toInt(),
            "json")
        request.enqueue(object :Callback<List<DriverNotification>>{
            override fun onResponse(
                call: Call<List<DriverNotification>>,
                response: Response<List<DriverNotification>>
            ) {
                val notificationList = response.body()
                if (notificationList != null) {
                    val requestNotifications = mutableListOf<DriverNotification>()
                    val finishedNotifications = mutableListOf<DriverNotification>()

                    for (notification in notificationList) {
                        if (isValidNotification(notification)) {
                            if (notification.status.status == "OFFER") {
                                requestNotifications.add(notification)
                            } else {
                                finishedNotifications.add(notification)
                            }
                        }
                    }

                    notificationRequestRecyclerView.adapter = CarrierNotificationRequestAdapter(requestNotifications, requireContext(),this@CarrierNotificationsFragment)
                    notificationRequestRecyclerView.layoutManager = LinearLayoutManager(context)

                    notificationFinishedRecyclerView.adapter = CarrierNotificationFinishAdapter(finishedNotifications, requireContext(),this@CarrierNotificationsFragment)
                    notificationFinishedRecyclerView.layoutManager = LinearLayoutManager(context)
                }
            }

            override fun onFailure(call: Call<List<DriverNotification>>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun isValidNotification(notification: DriverNotification): Boolean {
        return (notification.visible && notification.status.status == "OFFER") ||
                (notification.status.status == "HISTORY")
    }

    override fun onButtonRequestClick(driverNotification: DriverNotification) {
        //CODIGO
        Toast.makeText(context,driverNotification.id.toString(),Toast.LENGTH_SHORT).show()
    }

    override fun onButtonFinishClick(driverNotification: DriverNotification) {
        //CODIGO
        Toast.makeText(context,driverNotification.id.toString(),Toast.LENGTH_SHORT).show()
    }

}