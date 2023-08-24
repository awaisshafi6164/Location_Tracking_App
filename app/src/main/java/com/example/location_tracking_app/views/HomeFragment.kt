package com.example.location_tracking_app.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.location_tracking_app.R
import com.example.location_tracking_app.viewmodel.AuthViewModel
import com.example.location_tracking_app.viewmodel.MapViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Address
import android.os.AsyncTask
import android.util.Log
import android.widget.EditText
import androidx.core.app.ActivityCompat
import com.example.location_tracking_app.repository.GoogleMapDTO
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.*
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var mapViewModel: MapViewModel
    private lateinit var navController: NavController
    private lateinit var signOutBtn: Button
    private lateinit var getDirectionBTN: Button
    private lateinit var userTV: TextView
    private lateinit var startLocET: EditText
    private lateinit var endLocET: EditText

    //
    //
    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    private val PERMISSION_REQUEST_CODE = 123
    //
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(AuthViewModel::class.java)

        mapViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(MapViewModel::class.java)

        viewModel.loggedStatus.observe(this, Observer { isLoggedOut ->
            if (isLoggedOut) {
                navController.navigate(R.id.action_homeFragment_to_signInFragment)
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        signOutBtn = view.findViewById(R.id.signOutBtn)
        userTV = view.findViewById(R.id.userTextView)

        viewModel.usernameLiveData.observe(viewLifecycleOwner, Observer { username ->
            userTV.text = "Hello: $username"
        })

        signOutBtn.setOnClickListener {
            viewModel.signOut()
        }

        //
        //
        mapFragment =  childFragmentManager.findFragmentById(R.id.mapFrag) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSION_REQUEST_CODE
                )
                return@OnMapReadyCallback
            }

            googleMap.isMyLocationEnabled = true

            val pakistan = LatLng(30.3753, 69.3451)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pakistan,5f))

//            val location1 = LatLng(33.66263618397245, 73.05473119595356)
//            googleMap.addMarker(MarkerOptions().position(location1).title("RWR Office"))
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location1,12f))

//            val location2 = LatLng(33.66401100214133, 73.04693296520178)
//            googleMap.addMarker(MarkerOptions().position(location2).title("Railway Station"))

            startLocET = view.findViewById(R.id.startLocET)
            endLocET = view.findViewById(R.id.endLocET)
            getDirectionBTN = view.findViewById(R.id.getDirectionBtn)

            getDirectionBTN.setOnClickListener {
                val startLocationArea = startLocET.text.toString().trim()
                val endLocationArea = endLocET.text.toString().trim()

                if (startLocationArea.isNotEmpty() && endLocationArea.isNotEmpty()) {
                    var startLocationOutside: LatLng? = null // Initialize with null
                    var endLocationOutside: LatLng? = null   // Initialize with null

                    getCoordinateStartLocation(startLocationArea) { startLocation ->
                        if (startLocation != null) {
                            startLocationOutside = startLocation
                            // Move the rest of your code here that depends on startLocation
                            getCoordinateEndLocation(endLocationArea) { endLocation ->
                                if (endLocation != null) {
                                    endLocationOutside = endLocation

                                    val URL = getDirectionURL(startLocationOutside!!, endLocationOutside!!)
                                    Log.d("GoogleMap", "URL : $URL")
                                    GetDirection(URL).execute()
                                } else {
                                    toast("Something is wrong with the end location")
                                }
                            }
                        } else {
                            toast("Something is wrong with the start location")
                        }
                    }
                } else {
                    toast("Please enter a start or end location name")
                }
            }


//            val URL = getDirectionURL(startLocation, location2)
//            Log.d("GoogleMap", "URL : $URL")
//            GetDirection(URL).execute()

//            val URL = getDirectionURL(location1, location2)
//            Log.d("GoogleMap", "URL : $URL")
//            GetDirection(URL).execute()
        })

        //
        //
    }

    private fun getCoordinateStartLocation(area: String, callback: (LatLng?) -> Unit) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        Thread {
            try {
                val addresses: List<Address>? = geocoder.getFromLocationName(area, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val latitude = address.latitude
                    val longitude = address.longitude  // Use longitude instead of latitude here

                    requireActivity().runOnUiThread {
//                        val message = "Latitude: $latitude\nLongitude: $longitude"
//                        toast(message)

                        val startLocation = LatLng(latitude, longitude)
                        googleMap.addMarker(MarkerOptions().position(startLocation).title("Start Location"))
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation,10f))
                        callback(startLocation) // Call the callback with the obtained location
                    }
                } else {
                    requireActivity().runOnUiThread {
                        toast("No results found for the given city")
                        callback(null) // Call the callback with null when no results are found
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    toast("Error: ${e.message}")
                    callback(null) // Call the callback with null in case of error
                }
            }
        }.start()
    }

    private fun getCoordinateEndLocation(area: String, callback: (LatLng?) -> Unit) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        Thread {
            try {
                val addresses: List<Address>? = geocoder.getFromLocationName(area, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val latitude = address.latitude
                    val longitude = address.longitude  // Use longitude instead of latitude here

                    requireActivity().runOnUiThread {
//                        val message = "Latitude: $latitude\nLongitude: $longitude"
//                        toast(message)

                        val endLocation = LatLng(latitude, longitude)
                        googleMap.addMarker(MarkerOptions().position(endLocation).title("End Location"))
                        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(endLocation,10f))
                        callback(endLocation) // Call the callback with the obtained location
                    }
                } else {
                    requireActivity().runOnUiThread {
                        toast("No results found for the given city")
                        callback(null) // Call the callback with null when no results are found
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    toast("Error: ${e.message}")
                    callback(null) // Call the callback with null in case of error
                }
            }
        }.start()
    }


    fun getDirectionURL(origin:LatLng,dest:LatLng) : String{
        val apiKey = getString(R.string.map_api_key)
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=$apiKey"
    }

    inner class GetDirection(val url: String) : AsyncTask<Void,Void,List<List<LatLng>>>(){
        override fun doInBackground(vararg p0: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body()
            val data = responseBody?.string()
            Log.d("GoogleMap", "Response Data: $data")
            val result = ArrayList<List<LatLng>>()
            try {

                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)
                val path = ArrayList<LatLng>()

                for(i in 0..(respObj.routes[0].legs[0].steps.size-1)){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)

            }catch (e: Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for(i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            googleMap.addPolyline(lineoption)
        }

    }

    public fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }

    fun toast(message:String){
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }
}

