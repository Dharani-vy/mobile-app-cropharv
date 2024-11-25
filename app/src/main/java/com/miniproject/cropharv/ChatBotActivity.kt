package com.miniproject.cropharv

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.miniproject.cropharv.models.PlantDiseaseModel
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.math.pow
import org.json.JSONException


class ChatBotActivity  : AppCompatActivity() ,Callback{

    private lateinit var chatContainer: LinearLayout
    private lateinit var chatScrollView: ScrollView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var plantDiseaseModel: PlantDiseasePredictionc
    private lateinit var plantRecommendModel: PlantRecommendationActivity


    private val SELECT_IMAGE_REQUEST_CODE = 100

    // Define storage permissions
    private val STORAGE_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // Register permission request launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                // All permissions granted
                //openImagePicker() // Open the image picker after permissions are granted
            } else {
                Toast.makeText(this, "Permissions to access storage were denied.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)

        // Initialize views
        chatContainer = findViewById(R.id.chatContainer)
        chatScrollView = findViewById(R.id.chatScrollView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        // Instantiate the PlantDiseaseModel
        plantDiseaseModel = PlantDiseasePredictionc(this)
        plantRecommendModel = PlantRecommendationActivity()

        // Request storage permissions when the activity is created
        checkStoragePermissions()

        displayWelcomeMessage()
        // Set an OnClickListener to handle sending messages
        sendButton.setOnClickListener {
            val userMessage = messageEditText.text.toString()
            if (userMessage.isNotEmpty()) {
                // Display user's message in the chat
                addMessageToChat(userMessage, isUser = true)

                // Handle user's message with the chatbot logic
                handleUserMessage(userMessage)

                // Clear the input field after sending
                messageEditText.text.clear()
            }
        }
    }

    private fun checkStoragePermissions() {
        // Check if any of the permissions are not granted
        if (STORAGE_PERMISSIONS.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            // Request the permissions
            requestPermissionLauncher.launch(STORAGE_PERMISSIONS)
        }
    }

//    private fun openImagePicker() {
//        // Create an Intent to pick an image from the gallery
//        val intent = Intent(Intent.ACTION_PICK).apply {
//            type = "image/*" // Set the type to pick images
//        }
//        startActivityForResult(intent, PICK_IMAGE_REQUEST) // Start image picker activity
//    }
    private var isAwaitingMonth = false

    private fun handleUserMessage(message: String) {
        Log.d("ChatBotActivity", "User message: $message") // Log the user message
        val monthMap = mapOf(
            "january" to 1, "february" to 2, "march" to 3,
            "april" to 4, "may" to 5, "june" to 6,
            "july" to 7, "august" to 8, "september" to 9,
            "october" to 10, "november" to 11, "december" to 12
        )

        when {
            Regex("detect|identify|find\\s+disease", RegexOption.IGNORE_CASE).containsMatchIn(message) -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE)
            }

            Regex("recommend crop|crop suggestion|planting guide|best crop|crop advice|plant recommended", RegexOption.IGNORE_CASE).containsMatchIn(message) -> {
                isAwaitingMonth = true // Set the state to expect the month input
                addMessageToChat("Which month are you interested in for the crop recommendation?", isUser = false)
            }

            isAwaitingMonth -> {
                val monthInput = message.trim().toLowerCase() // Convert input to lowercase for consistent matching
                val monthNumber = monthMap[monthInput] // Get month number from map

                if (monthNumber != null) {
                    // Convert monthNumber to String before passing it to cpredictCropAndShowResult
                    val cropRecommendation = plantRecommendModel.cpredictCropAndShowResult(this, monthNumber.toString())
                    addMessageToChat(cropRecommendation ?: "No crop recommendation found for $monthInput.", isUser = false)
                } else {
                    addMessageToChat("Please enter a valid month name (e.g., January, February).", isUser = false)
                }
                isAwaitingMonth = false // Reset the state
            }


            message.contains("market price", ignoreCase = true) -> {
                val marketPrice = getMarketPrice()
                addMessageToChat(marketPrice, isUser = false)
            }
            else -> {
                // Use Gemini's API for other queries
                val messageformat="You are a crop expert and u have a app named as cropharv which does This application uses weather and crop data to predict crop yields, recommend suitable crops for different times. It also helps in predicting potential pest and disease of the leaf image with the help of machine learning techniques. Traditional methods for determining optimal harvest times for each crop are often unreliable, especially with climatic change. This application serves as an aid to resolve those issues. Additionally, it forecasts real-time weather updates and market prices. at harvest time, enabling users to identify the optimal selling time for maximizing the profit from their crops at the market. A chatbot is available to assist users with information about crop yields, market prices, current weather conditions, and pest and disease management and if i ask about the market price search the web and get the price available for the crop in the market dont ask for any specific variety just tell me price of the available variety answer the question below considering that and im from coimbatore and answer the question based on coimbatore if i ask about climate are any other thing i want the response to be brief exactly like how a chatbot would response simple and short and dont add any bold words make all the words in the same format no bullets or bold or italic Question: $message"
                ApiClient.sendRequest(messageformat, this)
                // addMessageToChat(text, isUser = false)// Call the Gemini API
            }
        }
    }

    override  fun onResponse(call: Call, response: Response) {
        val responseBody = response.body?.string()
       // println("Response Body: $responseBody")

        if (response.isSuccessful) {
            try {
                val jsonResponse = JSONObject(responseBody)
                println("Parsed JSON: $jsonResponse")

                // Access the candidates array
                val candidatesArray = jsonResponse.getJSONArray("candidates")
                if (candidatesArray.length() > 0) {
                    val firstCandidate = candidatesArray.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val partsArray = content.getJSONArray("parts")

                    // Now you can access the text from the parts array
                    if (partsArray.length() > 0) {
                        val text = partsArray.getJSONObject(0).getString("text")
                        println("Response Text: $text")
                        runOnUiThread {
                            addMessageToChat(text, isUser = false) // Update UI on the main thread
                        }
                    } else {
                        println("No parts in content")
                    }
                } else {
                    println("No candidates found")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                println("JSON Parsing Error: ${e.message}")
            }
        } else {
            println("Request unsuccessful: ${response.code}")
        }
    }

    private fun displayWelcomeMessage() {
        val welcomeMessage = """
        Welcome to CropHarv! 🌱
        I'm here to help you with crop recommendations, disease detection, weather updates, and more.
        You can ask me things like:
        - "What crop should I plant this month?"
        - "Can you identify a disease from this image?"
        - "What's the current market price for crops?"
        
        How can I assist you today?
    """.trimIndent()

        addMessageToChat(welcomeMessage, isUser = false)
    }



    // Override onFailure to handle errors
    override fun onFailure(call: Call, e: IOException) {
        runOnUiThread {
            addMessageToChat("Error: Request failed. Please try again later.", isUser = false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SELECT_IMAGE_REQUEST_CODE -> {
                    val uri = data?.data
                    if (uri != null) {
                        try {
                            // Use a ContentResolver for newer API versions
                            val inputStream = contentResolver.openInputStream(uri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            val resizedBitmap = resizeBitmap(bitmap, 500, 500)

                            // Ensure tflite model is initialized
                            if (::plantDiseaseModel.isInitialized) {
                                // Call the predictDisease method with the selected bitmap
                                val result = plantDiseaseModel.cpredictDisease(resizedBitmap)
                                addImageToChat(resizedBitmap)
                                displayDynamicResult(result)
                                // Add the result to the chat
                                Log.e("Result",result)
                                //addMessageToChat(result, isUser = false)
                            } else {
                                addMessageToChat("Model not initialized.", isUser = false)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            addMessageToChat("Error loading image: ${e.message}", isUser = false)
                        }
                    } else {
                        addMessageToChat("No image selected.", isUser = false)
                    }
                }
            }
        }
    }

    private fun addImageToChat(bitmap: Bitmap) {
        // Create a new ImageView dynamically
        val imageView = ImageView(this)

        // Set the bitmap to the ImageView
        imageView.setImageBitmap(bitmap)

        // Optionally set LayoutParams to control the appearance
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            500 // Set a fixed height or adjust as needed
        )
        layoutParams.setMargins(8, 8, 8, 8) // Optional: Add margins
        imageView.layoutParams = layoutParams

        // Add the ImageView to the chatContainer
        chatContainer.addView(imageView)

        // Scroll to the bottom to make sure the image is visible
        chatScrollView.post {
            chatScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun displayDynamicResult(result: String) {
        // Create a new TextView dynamically
        val resultTextView = TextView(this)

        // Set properties for the TextView
        resultTextView.text = result
        resultTextView.textSize = 16f
        resultTextView.setPadding(8, 8, 8, 8)
        resultTextView.setTextColor(Color.BLACK) // Set color for the result text
        resultTextView.setBackgroundColor(Color.LTGRAY) // Optional: Background color

        // Optionally set LayoutParams to control the appearance
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 8, 8, 8) // Optional: Add margins
        resultTextView.layoutParams = layoutParams

        // Add the TextView to the chatContainer
        chatContainer.addView(resultTextView)

        // Scroll to the bottom to make sure the result is visible
        chatScrollView.post {
            chatScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }


    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxWidth
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxHeight
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }


    private fun addMessageToChat(message: String, isUser: Boolean) {
        // Create a new TextView for the message
        val messageTextView = TextView(this)
        messageTextView.text = message
        messageTextView.textSize = 16f
        messageTextView.setPadding(16, 12, 16, 12)

        // Set layout parameters for the TextView
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Set different colors and backgrounds for user and bot messages
        if (isUser) {
            messageTextView.setBackgroundResource(R.drawable.user_message_background) // User message background
            messageTextView.setTextColor(Color.WHITE) // User's messages in white
            layoutParams.gravity = Gravity.END // Align user message to the right
            layoutParams.marginStart = 100 // Adjust as needed
            layoutParams.marginEnd = 0
        } else {
            messageTextView.setBackgroundResource(R.drawable.bot_message_background) // Bot message background
            messageTextView.setTextColor(Color.BLACK) // Bot's messages in black
            layoutParams.gravity = Gravity.START // Align bot message to the left
            layoutParams.marginStart = 0
            layoutParams.marginEnd = 100 // Adjust as needed
        }

        layoutParams.topMargin = 8
        layoutParams.bottomMargin = 8

        // Set the layout parameters to the TextView
        messageTextView.layoutParams = layoutParams

        // Add the message TextView to the chat container
        chatContainer.addView(messageTextView)

        // Scroll to the bottom to see the latest message
        chatScrollView.post {
            chatScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun getCropRecommendation(): String {
        // Placeholder function to simulate crop recommendation logic
        return "Recommended crop: Wheat"
    }

    private fun getMarketPrice(): String {
        // Placeholder function to simulate market price fetching logic
        return "Market price: $250/ton"
    }
}