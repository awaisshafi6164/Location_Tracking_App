package com.example.location_tracking_app.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.location_tracking_app.repository.AuthenticationRepository
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AuthenticationRepository
    val userData: MutableLiveData<FirebaseUser>
    val loggedStatus: MutableLiveData<Boolean>

    init {
        repository = AuthenticationRepository(application)
        userData = repository.getFirebaseUserMutableLiveData
        loggedStatus = repository.getUserLoggedMutableLiveData
    }

    fun register(email: String?, pass: String?, username: String?) {
        repository.register(email!!, pass!!, username!!)
    }

    fun signIn(email: String?, pass: String?) {
        repository.login(email!!, pass!!)
    }

    fun signOut() {
        repository.signOut()
    }

    val usernameLiveData: LiveData<String> = repository.getUserTV()

}