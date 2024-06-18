package com.fastporte.controller.fragments.RegisterFragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.fastporte.R
import com.fastporte.databinding.ActivityRegisterBinding
import com.fastporte.models.User
import java.text.SimpleDateFormat
import java.util.*

class FillInformationFragment : Fragment() {
    lateinit var user: User
    private var userType = ""
    private var userCard = ""
    val types = arrayListOf("Select user type", "Transportista", "Cliente")
    val cards = arrayListOf("Select type", "DNI", "Pasaporte")
    private lateinit var mBinding: ActivityRegisterBinding
    private lateinit var txtName: EditText
    private lateinit var txtLastName: EditText
    private lateinit var txtDateBirth: TextView
    private lateinit var txtPhoneNumber: EditText
    private lateinit var spUserType: Spinner
    private lateinit var spIdentityCardType: Spinner
    private lateinit var txtIdentityCardTypeNumber: EditText
    private lateinit var btnNext2: Button
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_fill_information, container, false)
        initializeViews(view)
        setupTextWatchers()
        setupSpinners()
        uploadDocument(view)
        uploadProfileId(view)
        return view
    }

    private fun initializeViews(view: View) {
        txtName = view.findViewById(R.id.txtName)
        txtLastName = view.findViewById(R.id.txtLastName)
        txtDateBirth = view.findViewById(R.id.txtDateBirth)
        txtPhoneNumber = view.findViewById(R.id.txtPhoneNumber)
        spUserType = view.findViewById(R.id.spUserType)
        spIdentityCardType = view.findViewById(R.id.spIdentityCardType)
        txtIdentityCardTypeNumber = view.findViewById(R.id.txtIdentityCardTypeNumber)
        btnNext2 = view.findViewById(R.id.btnNext2)

        txtDateBirth.setOnClickListener {
            val today = Calendar.getInstance()
            today.add(Calendar.YEAR, -18)
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                    txtDateBirth.text = dateFormat.format(selectedDate.time)
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = today.timeInMillis
            }.show()
        }

        btnNext2.setOnClickListener {
            if (validateFields()) {
                val argumentos = arguments
                if (argumentos != null) {
                    val valor = argumentos.getStringArray("tempUser")
                    if (valor != null) {
                        user = User(
                            txtDateBirth.text.toString(),
                            "",
                            valor[0].toString(),
                            0,
                            txtName.text.toString(),
                            txtLastName.text.toString(),
                            "",
                            txtPhoneNumber.text.toString(),
                            "Peru",
                            valor[1].toString(),
                            ""
                        )
                        val bundle = Bundle()
                        bundle.putSerializable("tempInfoUser", user)
                        if (userType == "Cliente") {
                            bundle.putSerializable("userType", "client")
                        } else {
                            bundle.putSerializable("userType", "driver")
                        }
                        Navigation.findNavController(view).navigate(
                            R.id.action_fillInformationFragment_to_newAccountFragment,
                            bundle
                        )
                    }
                }
            }
        }
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                btnNext2.isEnabled = validateFields()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        txtName.addTextChangedListener(watcher)
        txtLastName.addTextChangedListener(watcher)
        txtDateBirth.addTextChangedListener(watcher)
        txtPhoneNumber.addTextChangedListener(watcher)
        txtIdentityCardTypeNumber.addTextChangedListener(watcher)
        spUserType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                btnNext2.isEnabled = validateFields()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        spIdentityCardType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                btnNext2.isEnabled = validateFields()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun validateFields(): Boolean {
        val name = txtName.text.toString()
        val lastName = txtLastName.text.toString()
        val dateBirth = txtDateBirth.text.toString()
        val phoneNumber = txtPhoneNumber.text.toString()
        val identityCardTypeNumber = txtIdentityCardTypeNumber.text.toString()

        return name.isNotEmpty() && name.matches(Regex("^[a-zA-Z\\s]*$")) &&
                lastName.isNotEmpty() && lastName.matches(Regex("^[a-zA-Z\\s]*$")) &&
                dateBirth.isNotEmpty() &&
                phoneNumber.isNotEmpty() &&
                identityCardTypeNumber.isNotEmpty() &&
                userType.isNotEmpty() && userCard.isNotEmpty()
    }

    private fun setupSpinners() {
        setupSpinner(spUserType, types, "Select user type")
        setupSpinner(spIdentityCardType, cards, "Select card type")
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>, hint: String) {
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (position == 0) {
                    (view as TextView).text = hint
                    //view.setTextColor(resources.getColor(R.color.hint_color)) // set the hint color
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                if (position == 0) {
                    (view as TextView).text = hint
                    //view.setTextColor(resources.getColor(R.color.hint_color)) // set the hint color
                } else {
                    //(view as TextView).setTextColor(resources.getColor(R.color.default_text_color)) // set the default text color
                }
                return view
            }

            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0, false)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    if (spinner.id == R.id.spUserType) {
                        userType = parent.getItemAtPosition(position).toString()
                    } else if (spinner.id == R.id.spIdentityCardType) {
                        userCard = parent.getItemAtPosition(position).toString()
                    }
                    btnNext2.isEnabled = validateFields()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun uploadDocument(view_: View) {
//        val txtUploadPhoto = view_.findViewById<TextView>(R.id.txtUploadPhoto)
//        txtUploadPhoto.setOnClickListener {
//            val intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.type = "image/*"
//            startActivityForResult(intent, 1)
//        }
    }

    private fun uploadProfileId(view_: View) {
        val txtUploadDocument = view_.findViewById<TextView>(R.id.txtUploadDocument)
        txtUploadDocument.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1 -> {
                    val selectedImageUri = data?.data
                    // Manejar la imagen seleccionada
                }
            }
        }
    }
}