package com.fastporte.controller.fragments.CarrierFragments.CarrierProfile.Components

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.fastporte.R
import com.fastporte.databinding.FragmentAddVehicleBinding
import com.fastporte.helpers.BaseURL
import com.fastporte.helpers.SharedPreferences
import com.fastporte.models.User
import com.fastporte.models.Vehicle
import com.fastporte.network.ProfileService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AddVehicle : DialogFragment() {
    private lateinit var binding: FragmentAddVehicleBinding
    val bundle = Bundle()
    lateinit var vehicle: Vehicle
    private var positiveButton: View? = null

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BaseURL.BASE_URL.toString())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val vehicleService: ProfileService = retrofit.create(ProfileService::class.java)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentAddVehicleBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        builder.setPositiveButton("Add") { dialog, which ->
            val type = binding.spinnerVehicleType.selectedItem.toString()
            val capacity = (binding.etCapacity.text.toString().toIntOrNull() ?: 0)
            val unit = binding.spinnerCapacityUnit.selectedItem.toString()
            var urlImage = binding.etPhoto.text.toString()
            var user = User("", "", "", 0, "", "", "", "", "", "", "")

            val length = if (binding.layoutLength.visibility == View.VISIBLE) binding.etLength.text.toString().toDoubleOrNull() else null
            val width = if (binding.layoutWidth.visibility == View.VISIBLE) binding.etWidth.text.toString().toDoubleOrNull() else null
            val height = if (binding.layoutHeight.visibility == View.VISIBLE) binding.etHeight.text.toString().toDoubleOrNull() else null

            if (urlImage.isEmpty())
                urlImage = "https://png.pngtree.com/png-vector/20191021/ourlarge/pngtree-vector-car-icon-png-image_1834527.jpg"

            vehicle = Vehicle(id = 0, type = type, quantity = capacity, photo = urlImage, driver = user, length = length, width = width, height = height)
            postVehicle()
            dismiss()
        }
        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton?.isEnabled = false
            setupTextWatchers()
        }

        setupDropdowns()

        dialog.show()
        return dialog
    }

    private fun setupDropdowns() {
        val vehicleTypes = resources.getStringArray(R.array.vehicle_types)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVehicleType.adapter = adapter

        binding.spinnerVehicleType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedType = vehicleTypes[position]
                handleVehicleTypeSelection(selectedType)
                positiveButton?.isEnabled = shouldEnablePositiveButton()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val capacityUnits = resources.getStringArray(R.array.capacity_units)
        val capacityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, capacityUnits)
        capacityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCapacityUnit.adapter = capacityAdapter
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                positiveButton?.isEnabled = shouldEnablePositiveButton()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.etCapacity.addTextChangedListener(textWatcher)
        binding.etLength.addTextChangedListener(textWatcher)
        binding.etWidth.addTextChangedListener(textWatcher)
        binding.etHeight.addTextChangedListener(textWatcher)
        binding.etPhoto.addTextChangedListener(textWatcher)
    }

    private fun handleVehicleTypeSelection(type: String) {
        when (type) {
            "Trailer", "Refrigerated Truck", "Box Truck", "Panel Van", "Container Truck" -> {
                binding.layoutLength.visibility = View.VISIBLE
                binding.layoutWidth.visibility = View.VISIBLE
                binding.layoutHeight.visibility = View.VISIBLE
            }
            else -> {
                binding.layoutLength.visibility = View.GONE
                binding.layoutWidth.visibility = View.GONE
                binding.layoutHeight.visibility = View.GONE
            }
        }
    }

    private fun shouldEnablePositiveButton(): Boolean {
        val type = binding.spinnerVehicleType.selectedItem.toString()
        val capacityFilled = binding.etCapacity.text.toString().isNotEmpty()
        val lengthFilled = binding.layoutLength.visibility == View.GONE || binding.etLength.text.toString().isNotEmpty()
        val widthFilled = binding.layoutWidth.visibility == View.GONE || binding.etWidth.text.toString().isNotEmpty()
        val heightFilled = binding.layoutHeight.visibility == View.GONE || binding.etHeight.text.toString().isNotEmpty()

        return if (type in listOf("Trailer", "Refrigerated Truck", "Box Truck", "Panel Van", "Container Truck")) {
            capacityFilled && lengthFilled && widthFilled && heightFilled
        } else {
            capacityFilled
        }
    }

    private fun postVehicle() {
        val request = vehicleService.postVehicleDriver(
            SharedPreferences(this.requireContext()).getValue("id")!!.toInt(), vehicle
        )

        request.enqueue(object : Callback<Vehicle> {
            override fun onFailure(call: Call<Vehicle>, t: Throwable) {
                if (isAdded && !isDetached) {
                    showToast("Failed to save vehicle", false)
                    Toast.makeText(context, "Failed to save vehicle", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call<Vehicle>, response: Response<Vehicle>) {
                if (isAdded && !isDetached) {
                    if (response.isSuccessful) {
                        showToast("Vehicle saved successfully", true)
                    } else {
                        showToast("Failed to save vehicle", false)
                        Toast.makeText(context, "Failed to save vehicle", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun showToast(message: String, isSuccess: Boolean) {
        if (isAdded && !isDetached) {
            val inflater = layoutInflater
            val layout: LinearLayout = inflater.inflate(R.layout.custom_toast, null) as LinearLayout
            val toastMessage = layout.findViewById<TextView>(R.id.toastMessage)

            toastMessage.text = message
            val backgroundColor = if (isSuccess) {
                requireContext().getColor(R.color.accept)
            } else {
                requireContext().getColor(R.color.decline)
            }
            layout.setBackgroundColor(backgroundColor)

            with (Toast(requireContext())) {
                duration = Toast.LENGTH_LONG
                view = layout
                show()
            }
        }
    }
}