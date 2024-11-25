package com.miniproject.cropharv

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar

class MarketPricePredictionActivity : AppCompatActivity() ,Callback{

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var cropSpinner: Spinner
    private lateinit var Date: TextView
    private lateinit var predictButton: Button
    private lateinit var predictionResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_price_prediction) // Ensure you have this layout

        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")
        val phoneNumber = intent.getStringExtra("phoneNumber")
        Date = findViewById(R.id.dateInput)
        predictButton = findViewById(R.id.predictButton)
        predictionResult = findViewById(R.id.recommendedCropTextView)
        bottomNavigationView = findViewById(R.id.bottomNavView)
        bottomNavigationView.selectedItemId = R.id.navItem3
        // You can use the received values as needed
        //Log.d("HomeActivity", "Username: $username, Email: $email, Phone: $phoneNumber")
        cropSpinner= findViewById(R.id.cropSpinner)
        Date = findViewById(R.id.dateInput)

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.crop_names,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            cropSpinner.adapter = adapter
        }

        Date.setOnClickListener {
            // Get current date
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Show DatePickerDialog
            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Set selected date in TextView (Note: months are indexed from 0)
                    val formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                    Date.text = formattedDate
                },
                year,
                month,
                day
            )
            datePicker.show()
        }

        predictButton.setOnClickListener {
            val crop = cropSpinner.selectedItem.toString()
            val date = Date.text.toString()
            if (crop.isNotEmpty() && date.isNotEmpty()) {
                // Display user's message in the chat

                // Handle user's message with the chatbot logic
                handleUserMessage(crop,date)

                // Clear the input field after sending
                //messageEditText.text.clear()
            }
        }

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
    }

    private fun handleUserMessage(crop: String,date:String) {
        Log.d("MarketPriceActivity", "crop: $crop date :$date" ) // Log the user message

        // Use Gemini's API for other queries
        val messageformat="You are a crop expert and u have a app named as cropharv which does This application uses weather and crop data to predict crop yields, recommend suitable crops for different times. It also helps in predicting potential pest and disease of the leaf image with the help of machine learning techniques. Traditional methods for determining optimal harvest times for each crop are often unreliable, especially with climatic change. This application serves as an aid to resolve those issues. Additionally, it forecasts real-time weather updates and market prices. at harvest time, enabling users to identify the optimal selling time for maximizing the profit from their crops at the market. A chatbot is available to assist users with information about crop yields, market prices, current weather conditions, and pest and disease management and if i ask about the market price search the web and get the price available for the crop in the market dont ask for any specific variety just tell me price of the available variety answer the question below considering that and im from coimbatore and answer the question based on coimbatore if i ask about climate are any other thing i want the response to be brief exactly like how a chatbot would response simple and short and dont add any bold words make all the words in the same format no bullets or bold or italic Question: i want you to predict the market price for $crop in date $date and give output in the format The predicted price of $crop in $date is predicted_price even if i ask about future dates give the answer based on any calculations to predict price and add INR after the cost eg: 40 INR refer to kisandeal.com for info"
        ApiClient.sendRequest(messageformat, this)
        // addMessageToChat(text, isUser = false)// Call the Gemini API
    }

    override fun onResponse(call: Call, response: Response) {
        val responseBody = response.body?.string()

        if (response.isSuccessful) {
            try {
                val jsonResponse = JSONObject(responseBody)
                Log.d("MarketPriceResponse", "Response JSON: $jsonResponse")

                // Check if the response contains the expected fields
                if (jsonResponse.has("candidates")) {
                    val candidatesArray = jsonResponse.getJSONArray("candidates")

                    if (candidatesArray.length() > 0) {
                        val firstCandidate = candidatesArray.getJSONObject(0)

                        // Check if content object exists
                        if (firstCandidate.has("content")) {
                            val content = firstCandidate.getJSONObject("content")

                            // Check if parts array exists
                            if (content.has("parts")) {
                                val partsArray = content.getJSONArray("parts")

                                if (partsArray.length() > 0) {
                                    val text = partsArray.getJSONObject(0).getString("text")

                                    // Extract price information from text
                                    val priceInfo = extractPriceFromText(text)
                                    if (priceInfo != null) {
                                        runOnUiThread {
                                            // Display the price result on the UI
                                            predictionResult.text = "Predicted Price: ${priceInfo.first} ${priceInfo.second}"
                                        }
                                    } else {
                                        Log.d("MarketPriceResponse", "Couldn't extract market price from response.")
                                    }
                                } else {
                                    Log.d("MarketPriceResponse", "No parts found in content.")
                                }
                            } else {
                                Log.d("MarketPriceResponse", "No parts field in content.")
                            }
                        } else {
                            Log.d("MarketPriceResponse", "No content field in candidate.")
                        }
                    } else {
                        Log.d("MarketPriceResponse", "No candidates found.")
                    }
                } else {
                    Log.d("MarketPriceResponse", "No candidates field in response.")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("MarketPriceResponse", "JSON Parsing Error: ${e.message}")
            }
        } else {
            Log.e("MarketPriceResponse", "Request unsuccessful: ${response.code}")
        }
    }

    override fun onFailure(call: Call, e: IOException) {
        Log.e("MarketPricePrediction", "API call failed: ${e.message}")
        runOnUiThread {
            predictionResult.text = "Failed to get response from server."
        }
    }

    fun extractPriceFromText(text: String): Pair<Double, String>? {
        // A regex to match a number followed by a currency (e.g., "1200 INR")
        val pricePattern = Regex("""(\d+(?:\.\d+)?)\s*(INR|USD|EUR|[A-Z]{3})""") // Adjust for your currencies

        val matchResult = pricePattern.find(text)
        return if (matchResult != null) {
            val (price, currency) = matchResult.destructured
            Pair(price.toDouble(), currency)
        } else {
            null
        }
    }

}
