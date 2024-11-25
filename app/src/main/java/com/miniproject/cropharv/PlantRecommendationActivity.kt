package com.miniproject.cropharv

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Button
import android.widget.Spinner
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
import com.google.android.material.textfield.TextInputEditText
import com.miniproject.cropharv.R
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class PlantRecommendationActivity : AppCompatActivity(),Callback {

    //private lateinit var yearInput: TextInputEditText
    private lateinit var monthSpinner: Spinner
    private lateinit var recommendedCropTextView: TextView
    private lateinit var predictedYieldTextView: TextView
    private lateinit var predictButton: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_recommendation)

        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")
        val phoneNumber = intent.getStringExtra("phoneNumber")
        bottomNavigationView = findViewById(R.id.bottomNavView)
        predictedYieldTextView = findViewById(R.id.PredictedYieldTextView)
        bottomNavigationView.selectedItemId = R.id.navItem2
        // You can use the received values as needed
        //Log.d("HomeActivity", "Username: $username, Email: $email, Phone: $phoneNumber")

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

        monthSpinner= findViewById(R.id.monthSpinner)
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

// Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)

// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

// Apply the adapter to the spinner
        monthSpinner.adapter = adapter

        // Initialize views
       // yearInput = findViewById(R.id.yearInput)
        //monthSpinner = findViewById(R.id.monthSpinner)
        recommendedCropTextView = findViewById(R.id.recommendedCropTextView)
        predictButton = findViewById(R.id.predictButton)
        //translateButton=findViewById(R.id.translateButton)

        // Handle button click to predict crop
        predictButton.setOnClickListener {
            // Get the selected month from the Spinner
            val monthString = monthSpinner.selectedItem.toString()

            // Create a map to convert month names to numbers
            val monthMap = mapOf(
                "January" to 1,
                "February" to 2,
                "March" to 3,
                "April" to 4,
                "May" to 5,
                "June" to 6,
                "July" to 7,
                "August" to 8,
                "September" to 9,
                "October" to 10,
                "November" to 11,
                "December" to 12
            )

            // Convert selected month to its corresponding number
            val month = monthMap[monthString]

            if (month != null) {
                // Get the predicted crop from predictCropAndShowResult(month)
                val crop:String = predictCropAndShowResult(month)

                // Display the predicted crop or use it as needed
                recommendedCropTextView.text = "Predicted crop: $crop"

                // Pass month and crop to handleUserMessage
                handleUserMessage(month, crop)
            } else {
                recommendedCropTextView.text = "Please select a valid month."
            }
        }

    }

    private fun handleUserMessage(month: Int,crop:String) {
        Log.d("PlantRecommendation", "crop: $crop date :$month" ) // Log the user message

        // Use Gemini's API for other queries
        val messageformat="You are a crop expert and u have a app named as cropharv which does This application uses weather and crop data to predict crop yields, recommend suitable crops for different times. It also helps in predicting potential pest and disease of the leaf image with the help of machine learning techniques. Traditional methods for determining optimal harvest times for each crop are often unreliable, especially with climatic change. This application serves as an aid to resolve those issues. Additionally, it forecasts real-time weather updates and market prices. at harvest time, enabling users to identify the optimal selling time for maximizing the profit from their crops at the market. A chatbot is available to assist users with information about crop yields, market prices, current weather conditions, and pest and disease management and if i ask about the market price search the web and get the price available for the crop in the market dont ask for any specific variety just tell me price of the available variety answer the question below considering that and im from coimbatore and answer the question based on coimbatore if i ask about climate are any other thing i want the response to be brief exactly like how a chatbot would response simple and short and dont add any bold words make all the words in the same format no bullets or bold or italic Question: i want you to predict the yield for $crop in month $month and give output in the format The predicted yield of $crop in $month is predicted_yeild even if i ask about future dates give the answer based on any calculations to predict yield in kilogram per hectare and add kg/ha after the yield eg: 1000 kg/ha refer any websites to know the recent yield and also tell me why $crop is recommended in $month month make it short after @ eg:@paddy is recommend because..."
        ApiClient.sendRequest(messageformat, this)
        // addMessageToChat(text, isUser = false)// Call the Gemini API
    }

    // Function to read CSV and predict crop
    private fun predictCropAndShowResult(inputMonth: Int): String {
        // Access the CSV file from the res/raw directory
        val inputStream = resources.openRawResource(R.raw.recommended_crops_all_months)
        val reader = BufferedReader(InputStreamReader(inputStream))

        // Variable to hold the recommended crop, initialized with a default value
        var recommendedCrop: String = "rice"  // Default crop if no match is found

        // Read the CSV file
        reader.use { bufferedReader ->
            bufferedReader.readLine() // Skip header
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                val columns = line!!.split(",")
                val month = columns[0].toIntOrNull()
                if (month == inputMonth) {
                    recommendedCrop = columns[1]  // Set recommended crop for the matching month
                    break // Exit loop once a match is found
                }
            }
        }

        // Return the recommended crop
        return recommendedCrop
    }



    fun cpredictCropAndShowResult(context: Context,inputMonth: String): String? {
        // Access the CSV file from the res/raw directory
        val resources = context.resources
        val inputStream = resources.openRawResource(R.raw.recommended_crops_all_months)
        val reader = BufferedReader(InputStreamReader(inputStream))

        // Read the CSV file
        var recommendedCrop: String? = null
        reader.use { bufferedReader ->
            bufferedReader.readLine() // Skip header
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                val columns = line!!.split(",")
                // Compare the string representation of the month
                if (columns.isNotEmpty()) {
                    val month = columns[0] // This is now a String
                    if (month == inputMonth) {
                        recommendedCrop = columns[1]
                        break // Exit the loop if the crop is found
                    }
                }
            }
        }
        val result="Your recommended crop for month $inputMonth is $recommendedCrop."
        // Return the recommended crop or null if not found
        return result
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
                                    val yieldInfo = extractkgFromText(text)
                                    val explanationPattern = Regex("""@(.*)""") // Captures everything after '@'
                                    val explanationMatch = explanationPattern.find(text)
                                    val explanation = explanationMatch?.groupValues?.get(1)?.trim() // Get the captured explanation, if available

                                    if (yieldInfo != null) {
                                        runOnUiThread {
                                            // Display the yield result and explanation on the UI
                                            if (explanation != null) {
                                                predictedYieldTextView.text = "Predicted Yield: $yieldInfo kg/ha. \n $explanation"
                                            } else {
                                                predictedYieldTextView.text = "Predicted Yield: $yieldInfo kg/ha."
                                            }
                                        }
                                    } else {
                                        Log.d("YieldExtraction", "Couldn't extract yield from response.")
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

    fun extractkgFromText(text: String): Double? {
        // A regex to match a number followed by "kg/ha" (e.g., "1000 kg/ha")
        val yieldPattern = Regex("""(\d+(?:\.\d+)?)\s*kg/ha""") // Matches numbers with or without decimal before "kg/ha"

        // Find the match in the text
        val matchResult = yieldPattern.find(text)

        return if (matchResult != null) {
            val (yield) = matchResult.destructured
            yield.toDouble()  // Return the yield as a Double
        } else {
            null  // Return null if no match is found
        }
    }

    override fun onFailure(call: Call, e: IOException) {
        Log.e("PlantRecommendation", "API call failed: ${e.message}")
        runOnUiThread {
            recommendedCropTextView.text = "Failed to get response from server."
        }
    }

}
