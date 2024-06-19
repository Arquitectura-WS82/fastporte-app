package com.fastporte.controller.fragments.ClientFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.Navigation
import androidx.viewpager.widget.ViewPager
import com.fastporte.R
import com.fastporte.helpers.BaseURL
import com.fastporte.models.User
import com.fastporte.network.ProfileService
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ClientSearchDriverProfile : Fragment() {
    lateinit var user: User;
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    private val retrofit = Retrofit.Builder()
        .baseUrl(BaseURL.BASE_URL.toString())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val profileService: ProfileService = retrofit.create(ProfileService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View =
            inflater.inflate(R.layout.fragment_client_search_driver_profile, container, false)

        user = arguments?.getSerializable("searchUserTemp") as User

        val request = profileService.getDriverRating(user.id, "json")

        request.enqueue(object : retrofit2.Callback<Float> {
            var rating: Float = 0.0f
            override fun onFailure(call: retrofit2.Call<Float>, t: Throwable) {
                //Log.d("Activity Fail", "Error: $t")
                loadInformation(view, rating)
            }

            override fun onResponse(
                call: retrofit2.Call<Float>,
                response: retrofit2.Response<Float>
            ) {
                if (response.isSuccessful) {
                    rating = response.body()!!
                    loadInformation(view, rating)
                }
            }
        })

        contractDriver(view)

        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.view_pager)

        val adapter = ViewPagerAdapter(childFragmentManager, user)
        adapter.addFragment(ClientSearchPersonalnformationfragment(), "Personal Information")
        adapter.addFragment(ClientSearchExperienceFragment(), "Experience")
        adapter.addFragment(ClientSearchVehicleFragment(), "Vehicle")
        adapter.addFragment(ClientSearchCommentsFragment(), "Comments")
        back(view)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

        return view
    }

    private fun back(view_: View) {
        val tvbacksearchprofile = view_.findViewById<TextView>(R.id.tvbacksearchprofile)
        tvbacksearchprofile.setOnClickListener() {
            Navigation.findNavController(view_)
                .navigate(R.id.action_clientSearchDriverProfile_to_searchResultFragment)
        }
    }

    private fun contractDriver(view_: View) {
        val btSearchVehicleContract = view_.findViewById<Button>(R.id.btSearchVehicleContract)
        btSearchVehicleContract.setOnClickListener() {
            val bundle = Bundle()
            bundle.putSerializable("searchUserTemp", user)
            Navigation.findNavController(view_)
                .navigate(
                    R.id.action_clientSearchDriverProfile_to_clientRequestServiceFragment,
                    bundle
                )
        }
    }

    private fun loadInformation(view_: View, rating: Float) {
        val tvSearchVehicleProfileName =  view_.findViewById<TextView>(R.id.tvSearchVehicleProfileName)
        val tvSearchVehicleProfileDescription = view_.findViewById<TextView>(R.id.tvSearchVehicleProfileDescription)
        val rbSearchVehicleUserStar = view_.findViewById<RatingBar>(R.id.rbSearchVehicleUserStar)
        val civtvSearchVehicleCarrierProfile = view_.findViewById<CircleImageView>(R.id.civtvSearchVehicleCarrierProfile)

        tvSearchVehicleProfileName.text = user.name
        tvSearchVehicleProfileDescription.text = user.description
        rbSearchVehicleUserStar.rating = rating

        val picBuilder = Picasso.Builder(requireContext())
        picBuilder.downloader((OkHttp3Downloader(context)))

        picBuilder.build().load(user.photo)
            .placeholder(R.color.white)
            .error(R.drawable.default_profile)
            .into(civtvSearchVehicleCarrierProfile)

    }

    private inner class ViewPagerAdapter(manager: FragmentManager, private val user: User) :
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList = ArrayList<Fragment>()
        private val fragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            val fragment = fragmentList[position]
            if (fragment is ClientSearchPersonalnformationfragment) {
                fragment.setUser(user)
            }
            if (fragment is ClientSearchExperienceFragment) {
                fragment.setUser(user)
            }
            if (fragment is ClientSearchVehicleFragment) {
                fragment.setUser(user)
            }
            if (fragment is ClientSearchCommentsFragment) {
                fragment.setUser(user)
            }
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }
}