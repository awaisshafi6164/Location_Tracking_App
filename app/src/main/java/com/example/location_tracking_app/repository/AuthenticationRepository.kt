package com.example.location_tracking_app.repository

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.location_tracking_app.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class AuthenticationRepository(private val application: Application) {
    private val firebaseUserMutableLiveData = MutableLiveData<FirebaseUser>()
    private val userLoggedMutableLiveData = MutableLiveData<Boolean>()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    val getFirebaseUserMutableLiveData: MutableLiveData<FirebaseUser>
        get() = firebaseUserMutableLiveData

    val getUserLoggedMutableLiveData: MutableLiveData<Boolean>
        get() = userLoggedMutableLiveData

    init {
        if (auth.currentUser != null) {
            firebaseUserMutableLiveData.postValue(auth.currentUser)
        }
    }

    fun register(email: String, pass: String, username: String) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    saveUsernameToDatabase(user.uid, username)
                }
                firebaseUserMutableLiveData.postValue(auth.currentUser)
            } else {
                Toast.makeText(application, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun login(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firebaseUserMutableLiveData.postValue(auth.currentUser)
            } else {
                Toast.makeText(application, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun signOut() {
        auth.signOut()
        userLoggedMutableLiveData.postValue(true)
    }

    private fun saveUsernameToDatabase(userId: String, username: String) {
        val userRef: DatabaseReference = database.getReference("users").child(userId)

        userRef.child("username").setValue(username)
        userRef.child("email").setValue(auth.currentUser?.email)
            .addOnSuccessListener {
                Toast.makeText(application, "Save Username Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(application, "Failed to save username: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun getUserTV(): MutableLiveData<String> {
        val usernameLiveData = MutableLiveData<String>()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef: DatabaseReference = database.getReference("users").child(userId)

            userRef.child("username").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.value as? String
                    usernameLiveData.postValue(username ?: "Default Username")
                }

                override fun onCancelled(error: DatabaseError) {
                    usernameLiveData.postValue("Error fetching username")
                }
            })
        } else {
            usernameLiveData.postValue("User not logged in")
        }

        return usernameLiveData
    }

    fun toast(message: String){
        Toast.makeText(application,message,Toast.LENGTH_SHORT).show()
    }
}