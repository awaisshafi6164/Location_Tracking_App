package com.example.location_tracking_app.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.location_tracking_app.R
import com.example.location_tracking_app.viewmodel.AuthViewModel

class SignUpFragment : Fragment() {

    private lateinit var emailEdit: EditText
    private lateinit var passEdit: EditText
    private lateinit var usernameEdit: EditText
    private lateinit var signInText: Button
    private lateinit var signUpBtn: Button
    private lateinit var viewModel: AuthViewModel
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(AuthViewModel::class.java)

        viewModel.userData.observe(this, Observer { firebaseUser ->
            if (firebaseUser != null) {
                navController.navigate(R.id.action_signUpFragment_to_homeFragment)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailEdit = view.findViewById(R.id.emailEditSignUp)
        passEdit = view.findViewById(R.id.passEditSignUp)
        usernameEdit = view.findViewById(R.id.usernameEditSignUp)
        signInText = view.findViewById(R.id.signInText)
        signUpBtn = view.findViewById(R.id.signUpBtn)

        navController = Navigation.findNavController(view)

        signInText.setOnClickListener {
            signInBtnMethod()
        }

        signUpBtn.setOnClickListener {
            signUpBtnMethod()
        }
    }

    private fun signUpBtnMethod(){
        val email = emailEdit.text.toString()
        val pass = passEdit.text.toString()
        val username = usernameEdit.text.toString()

        if (email.isNotEmpty() && pass.isNotEmpty() && username.isNotEmpty()) {
            viewModel.register(email, pass, username)
        }else{
            Toast.makeText(requireContext(), "Kindly fill the values!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInBtnMethod(){
        navController.navigate(R.id.action_signUpFragment_to_signInFragment)
    }
}
