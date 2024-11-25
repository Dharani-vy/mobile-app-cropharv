package com.miniproject.cropharv

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    // Declare UI components
    private lateinit var textViewLocation: TextView
    private lateinit var textViewWeatherType: TextView
    private lateinit var textViewFeelsLike: TextView
    private lateinit var textViewTemperature: TextView
    private lateinit var textViewHumidity: TextView
    private lateinit var textViewPressure: TextView
    private lateinit var textViewPrecipitation: TextView
    private lateinit var textViewCloudiness: TextView
    private lateinit var textViewSunrise: TextView
    private lateinit var textViewSunset: TextView
    private lateinit var textViewChanceOfRain: TextView
    private lateinit var textViewChanceOfSnow: TextView
    private lateinit var imageViewWeatherIcon: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var cardPlantDiseasePrediction: CardView
    private lateinit var cardPlantRecommendation: CardView
    private lateinit var cardMarketPricePrediction: CardView
    private lateinit var chatbotButton: AppCompatImageView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var viewname: TextView

    private val apiKey = "564a2a74c5e54ff236c40c51e6ee7dad"  // Replace with your OpenWeatherMap API key

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
//    private val STORAGE_PERMISSION_CODE = 1
//    private val FILE_PICKER_REQUEST_CODE = 1001


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")
        val phoneNumber = intent.getStringExtra("phoneNumber")
        bottomNavigationView = findViewById(R.id.bottomNavView)
        viewname = findViewById(R.id.view_name)

        viewname.setText("Welcome $username,")
        // You can use the received values as needed
        Log.d("HomeActivity", "Username: $username, Email: $email, Phone: $phoneNumber")

        // Example of forwarding these values to another activity
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navItem1 -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("email", email)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                }
                R.id.navItem2 -> {
                    val intent = Intent(this, PlantRecommendationActivity::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("email", email)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                }
                R.id.navItem3 -> {
                    val intent = Intent(this, MarketPricePredictionActivity::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("email", email)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                }
                R.id.navItem4 -> {
                    val intent = Intent(this, PlantDiseasePrediction::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("email", email)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                }
                R.id.navItem5 -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("email", email)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                }
                else -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("email", email)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                }
            }
            true
        }

        // Initialize UI components
        textViewLocation = findViewById(R.id.location1)
        textViewWeatherType = findViewById(R.id.weatherType1)
        textViewFeelsLike = findViewById(R.id.feelsLike)
        textViewTemperature = findViewById(R.id.temperature1)
        textViewHumidity = findViewById(R.id.humiditytext)
        textViewPressure = findViewById(R.id.pressuretext)
        textViewPrecipitation = findViewById(R.id.precipitationtext)
        textViewCloudiness = findViewById(R.id.cloudinessText)
        textViewSunrise = findViewById(R.id.sunriseText)
        textViewSunset = findViewById(R.id.sunsetText)
        textViewChanceOfRain = findViewById(R.id.chanceOfRainText)
        textViewChanceOfSnow = findViewById(R.id.chanceOfSnowText)
        imageViewWeatherIcon = findViewById(R.id.weatherIcon)
        cardPlantDiseasePrediction = findViewById(R.id.card_plant_disease_prediction)
        cardPlantRecommendation = findViewById(R.id.card_plant_recommendation)
        cardMarketPricePrediction = findViewById(R.id.card_market_price_prediction)
        chatbotButton = findViewById(R.id.chatbotButton)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        cardPlantDiseasePrediction.setOnClickListener {
            // Navigate to Plant Recommendation activity
            val intent = Intent(this, PlantDiseasePrediction::class.java)
            intent.putExtra("username", username)
            intent.putExtra("email", email)
            intent.putExtra("phoneNumber", phoneNumber)
            startActivity(intent)
        }

        cardPlantRecommendation.setOnClickListener {
            // Navigate to Plant Recommendation activity
            val intent = Intent(this, PlantRecommendationActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("email", email)
            intent.putExtra("phoneNumber", phoneNumber)
            startActivity(intent)
        }

        cardMarketPricePrediction.setOnClickListener {
            // Navigate to Market Price Prediction activity
            val intent = Intent(this, MarketPricePredictionActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("email", email)
            intent.putExtra("phoneNumber", phoneNumber)
            startActivity(intent)
        }

        chatbotButton.setOnClickListener {
            // Navigate to the Chatbot Activity or Fragment
            val intent = Intent(this, ChatBotActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("email", email)
            intent.putExtra("phoneNumber", phoneNumber)// Replace with your chatbot activity
            startActivity(intent)
        }

        // Get the last known location when the page is loaded
        checkLocationPermission()
       // checkStoragePermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getLastKnownLocation()
        }
    }


    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                Log.d("Location", "Latitude: $latitude, Longitude: $longitude")  // Log location

                // Fetch address from latitude and longitude
                val address = getAddressFromLocation(latitude, longitude)
                Log.d("address"," $address")
                textViewLocation.text = address

                // Fetch weather data
                fetchWeatherData(latitude, longitude)
            } ?: run {
                textViewLocation.text = "Unable to get location."
            }
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return if (addresses != null && addresses.isNotEmpty()) {
            addresses[0].locality ?: "Address not found"
        } else {
            "Address not found"
        }
    }

    private fun loadFragment(fragment: Fragment, isAppInitialized: Boolean) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

        if (isAppInitialized) {
            fragmentTransaction.add(R.id.frameLayout, fragment)
        } else {
            fragmentTransaction.add(R.id.frameLayout, fragment)
        }
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }


    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getWeather(latitude, longitude, apiKey, "metric")
            if (response.isSuccessful) {
                val weather = response.body()
                weather?.let {
                    val weatherId = it.weather[0].id
                    val weatherDescription = it.weather[0].description
                    val temperature = it.main.temp
                    val humidity = it.main.humidity
                    val feelsLike = it.main.feels_like
                    val pressure = it.main.pressure
                    val chanceOfRain = it.rain?.`1h` ?: 0.0
                    val cloudiness = it.clouds?.all ?: 0
                    val sunrise = it.sys?.sunrise?.let { unixTime -> formatUnixTime(unixTime) } ?: "N/A"
                    val sunset = it.sys?.sunset?.let { unixTime -> formatUnixTime(unixTime) } ?: "N/A"
                    val chanceOfSnow = it.snow?.`1h` ?: 0.0
                    val isNight = it.weather[0].icon.endsWith("n") // Check if it's night based on the icon

                    // Log weather data
                    Log.d("Weather", "Description: $weatherDescription")
                    Log.d("Weather", "Temperature: $temperature°C")
                    Log.d("Weather", "Humidity: $humidity%")
                    Log.d("Weather", "Feels Like: $feelsLike°C")
                    Log.d("Weather", "Pressure: $pressure hPa")
                    Log.d("Weather", "Chance of Rain: $chanceOfRain mm")
                    Log.d("Weather", "Cloudiness: $cloudiness%")
                    Log.d("Weather", "Sunrise: $sunrise")
                    Log.d("Weather", "Sunset: $sunset")
                    Log.d("Weather", "Chance of Snow: $chanceOfSnow mm")

                    // Determine the icon resource based on weather ID
                    val iconResId = when (weatherId) {
                        in 200..232 -> if (isNight) R.drawable.icon_thunder else R.drawable.icon_thunder
                        in 300..321 -> if (isNight) R.drawable.icon_rain else R.drawable.icon_rain
                        in 500..504 -> if (isNight) R.drawable.icon_rain else R.drawable.icon_rain
                        in 600..622 -> if (isNight) R.drawable.icon_snow else R.drawable.icon_snow
                        in 701..761 -> if (isNight) R.drawable.icon_fog else R.drawable.icon_fog
                        else -> if (isNight) R.drawable.icon_moon else R.drawable.icon_sun
                    }

                    withContext(Dispatchers.Main) {
                        textViewWeatherType.text = weatherDescription.capitalize()
                        textViewTemperature.text = "${temperature.toInt()}°C"
                        textViewFeelsLike.text = "Feels Like: ${feelsLike.toInt()}°C"
                        textViewHumidity.text = "${humidity}%"
                        textViewPressure.text = "${pressure}hPa"
                        textViewPrecipitation.text = "${chanceOfRain} mm"
                        textViewCloudiness.text = "${cloudiness}%"
                        textViewSunrise.text = sunrise
                        textViewSunset.text = sunset
                        textViewChanceOfRain.text = "${chanceOfRain} mm"
                        textViewChanceOfSnow.text = "${chanceOfSnow} mm"

                        imageViewWeatherIcon.setImageResource(iconResId)
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatUnixTime(unixTime: Long): String {
        val date = java.util.Date(unixTime * 1000)
        val formatter = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation()
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
