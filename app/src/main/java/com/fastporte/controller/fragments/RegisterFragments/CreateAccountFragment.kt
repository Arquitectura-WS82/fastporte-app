package com.fastporte.controller.fragments.RegisterFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.Navigation
import com.fastporte.R

class CreateAccountFragment : Fragment() {

    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtConfirmPassword: EditText
    private lateinit var btnNext: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_create_account, container, false)
        initializeViews(view)
        setupTextWatchers()
        return view
    }

    private fun initializeViews(view: View) {
        btnNext = view.findViewById(R.id.btnNext)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtPassword = view.findViewById(R.id.txtPassword)
        txtConfirmPassword = view.findViewById(R.id.txtConfirmPassword)

        btnNext.isEnabled = false

        btnNext.setOnClickListener {
            if (validateFields()) {
                val temporalUser =
                    arrayOf(txtEmail.text.toString(), txtPassword.text.toString())
                val bundle = Bundle()
                bundle.putStringArray("tempUser", temporalUser)
                Navigation.findNavController(view).navigate(
                    R.id.action_createAccountFragment_to_fillInformationFragment,
                    bundle
                )
            }
        }
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateFields()
                btnNext.isEnabled = areAllFieldsValid()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when (s.hashCode()) {
                    txtEmail.text.hashCode() -> txtEmail.error = null
                    txtPassword.text.hashCode() -> txtPassword.error = null
                    txtConfirmPassword.text.hashCode() -> txtConfirmPassword.error = null
                }
            }
        }

        txtEmail.addTextChangedListener(watcher)
        txtPassword.addTextChangedListener(watcher)
        txtConfirmPassword.addTextChangedListener(watcher)
    }

    private fun validateFields(): Boolean {
        val email = txtEmail.text.toString()
        val password = txtPassword.text.toString()
        val confirmPassword = txtConfirmPassword.text.toString()

        var isValid = true

        if (email.isEmpty() || !isEmailValid(email)) {
            txtEmail.error = "Por favor, introduce un email válido"
            isValid = false
        }

        if (password.isEmpty()) {
            txtPassword.error = "La contraseña no puede estar vacía"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            txtConfirmPassword.error = "La confirmación de la contraseña no puede estar vacía"
            isValid = false
        } else if (password != confirmPassword) {
            txtConfirmPassword.error = "Las contraseñas deben ser iguales"
            isValid = false
        }

        return isValid
    }

    private fun areAllFieldsValid(): Boolean {
        return txtEmail.error == null && txtPassword.error == null && txtConfirmPassword.error == null
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}