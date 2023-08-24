package com.example.location_tracking_app.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.location_tracking_app.R
import com.example.location_tracking_app.viewmodel.AuthViewModel


@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(AuthViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        // Simulate a delay using Handler
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if the user data has already changed (to handle fast navigation)
            if (viewModel.userData.value == null) {
                //Toast.makeText(requireContext(), "Below SignUp", Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.action_splashScreenFragment_to_signUpFragment)
            }else if(viewModel.userData.value != null){
                //Toast.makeText(requireContext(), "Below Home", Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.action_splashScreenFragment_to_homeFragment)
            }
        }, 3000)

    }

}
