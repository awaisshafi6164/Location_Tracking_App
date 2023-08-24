package com.example.location_tracking_app.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.location_tracking_app.R
import com.example.location_tracking_app.viewmodel.AuthViewModel

class SignInFragment : Fragment() {

    private lateinit var emailEdit: EditText
    private lateinit var passEdit: EditText
    private lateinit var signUpText: Button
    private lateinit var signInBtn: Button
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
                navController.navigate(R.id.action_signInFragment_to_homeFragment)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailEdit = view.findViewById(R.id.emailEditSignIn)
        passEdit = view.findViewById(R.id.passEditSignIn)
        signUpText = view.findViewById(R.id.signUpText)
        signInBtn = view.findViewById(R.id.signInBtn)

        navController = Navigation.findNavController(view)

        signUpText.setOnClickListener {
            signUpBtnMethod()
        }

        signInBtn.setOnClickListener {
            signInBtnMethod()
        }
    }

    private fun signInBtnMethod(){
        val email = emailEdit.text.toString()
        val pass = passEdit.text.toString()

        if (email.isNotEmpty() && pass.isNotEmpty()) {
            viewModel.signIn(email, pass)
        }
    }

    private fun signUpBtnMethod(){
        navController.navigate(R.id.action_signInFragment_to_signUpFragment)
    }
}
