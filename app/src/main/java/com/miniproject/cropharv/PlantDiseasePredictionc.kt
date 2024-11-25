package com.miniproject.cropharv

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import android.net.Uri
import android.view.View
import java.io.IOException
import java.nio.MappedByteBuffer


class PlantDiseasePredictionc(context: Context) : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var selectImageButton: Button
    // private lateinit var captureImageButton: Button
    private lateinit var predictButton: Button
    private lateinit var translateButton: Button
    private lateinit var textViewDiseaseDescription : TextView
    private lateinit var textViewTreatment: TextView
    private lateinit var textViewLink: TextView
    private lateinit var textViewPest: TextView
    private lateinit var tflite: Interpreter

    // Constants for selecting and capturing images
    private val SELECT_IMAGE_REQUEST_CODE = 100
    // private val CAPTURE_IMAGE_REQUEST_CODE = 101


    init {
        initializeModel(context)
    }

    //constructor() : super()

    // List of disease classes
    private val diseaseClasses = arrayOf(
        "Apple___Apple_scab",
        "Apple___Black_rot",
        "Apple___Cedar_apple_rust",
        "Apple___healthy",
        "Blueberry___healthy",
        "Cherry_(including_sour)___Powdery_mildew",
        "Cherry_(including_sour)___healthy",
        "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot",
        "Corn_(maize)___Common_rust_",
        "Corn_(maize)___Northern_Leaf_Blight",
        "Corn_(maize)___healthy",
        "Grape___Black_rot",
        "Grape___Esca_(Black_Measles)",
        "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)",
        "Grape___healthy",
        "Orange___Haunglongbing_(Citrus_greening)",
        "Peach___Bacterial_spot",
        "Peach___healthy",
        "Pepper,_bell___Bacterial_spot",
        "Pepper,_bell___healthy",
        "Potato___Early_blight",
        "Potato___Late_blight",
        "Potato___healthy",
        "Raspberry___healthy",
        "Soybean___healthy",
        "Squash___Powdery_mildew",
        "Strawberry___Leaf_scorch",
        "Strawberry___healthy",
        "Tomato___Bacterial_spot",
        "Tomato___Early_blight",
        "Tomato___Late_blight",
        "Tomato___Leaf_Mold",
        "Tomato___Septoria_leaf_spot",
        "Tomato___Spider_mites Two-spotted_spider_mite",
        "Tomato___Target_Spot",
        "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
        "Tomato___Tomato_mosaic_virus",
        "Tomato___healthy"
    )

    private val pestsCausingDisease = arrayOf(
        "Apple scab fungus",
        "Black rot fungus",
        "Cedar apple rust fungus",
        "None",
        "None",
        "Powdery mildew fungus",
        "None",
        "Cercospora fungus",
        "Common rust fungus",
        "Northern Leaf Blight fungus",
        "None",
        "Black rot fungus",
        "Esca fungus",
        "Isariopsis Leaf Spot fungus",
        "None",
        "Asian Citrus Psyllid",
        "Bacterial pathogen",
        "None",
        "Bacterial pathogen",
        "None",
        "Alternaria fungus",
        "Phytophthora infestans",
        "None",
        "None",
        "None",
        "Powdery mildew fungus",
        "None",
        "None",
        "Bacterial pathogen",
        "Alternaria fungus",
        "Phytophthora infestans",
        "Leaf mold fungus",
        "Septoria fungus",
        "Spider mites",
        "Target Spot fungus",
        "Tomato Yellow Leaf Curl Virus (spread by whiteflies)",
        "Tomato Mosaic Virus",
        "None"
    )

    private val diseaseDescription = arrayOf(
        "Apple scab shows as dark, scabby lesions on leaves and fruit.",
        "Black rot appears as concentric rings of rot on apples.",
        "Cedar apple rust causes bright orange spots on leaves.",
        "Healthy apple with no disease signs.",
        "Healthy blueberry plant with no disease signs.",
        "Powdery mildew shows white, powdery fungus on leaves.",
        "Healthy cherry plant with no disease signs.",
        "Gray leaf spot appears as small, rectangular lesions on corn leaves.",
        "Common rust causes pustules on corn leaves.",
        "Northern Leaf Blight forms long, elliptical lesions on corn leaves.",
        "Healthy corn plant with no disease signs.",
        "Black rot forms black fruiting bodies on grape leaves and fruits.",
        "Esca shows as brownish streaks on grape wood and fruits.",
        "Leaf blight appears as brown spots surrounded by yellow halos.",
        "Healthy grape plant with no disease signs.",
        "Citrus greening causes yellowing of leaves and deformed fruits.",
        "Bacterial spot causes small, dark, water-soaked lesions on leaves and fruit.",
        "Healthy peach plant with no disease signs.",
        "Bacterial spot causes small, dark lesions on leaves and fruits.",
        "Healthy pepper plant with no disease signs.",
        "Early blight causes brown spots with concentric rings on potato leaves.",
        "Late blight causes dark lesions on leaves and tubers.",
        "Healthy potato plant with no disease signs.",
        "Healthy raspberry plant with no disease signs.",
        "Healthy soybean plant with no disease signs.",
        "Powdery mildew shows white, powdery fungus on squash leaves.",
        "Leaf scorch appears as reddish-brown edges on strawberry leaves.",
        "Healthy strawberry plant with no disease signs.",
        "Bacterial spot causes small, dark lesions on tomato leaves and fruits.",
        "Early blight causes brown spots with concentric rings on tomato leaves.",
        "Late blight causes dark lesions on tomato leaves and fruits.",
        "Leaf mold appears as pale green spots that turn brown on tomato leaves.",
        "Septoria leaf spot causes small, dark lesions on tomato leaves.",
        "Spider mites cause tiny yellow or white spots on tomato leaves.",
        "Target spot forms dark, concentric lesions on tomato leaves.",
        "Tomato Yellow Leaf Curl Virus causes yellowing and curling of tomato leaves.",
        "Tomato Mosaic Virus causes mottled, yellowish patterns on tomato leaves.",
        "Healthy tomato plant with no disease signs."
    )

    private val diseaseTreatment = arrayOf(
        "Treat with fungicide sprays during wet weather.",
        "Control with fungicide and proper pruning of infected branches.",
        "Remove nearby cedar trees and apply fungicides.",
        "No treatment required.",
        "No treatment required.",
        "Apply sulfur-based fungicides and ensure good air circulation.",
        "No treatment required.",
        "Rotate crops and apply resistant hybrids.",
        "Treat with fungicides and use resistant hybrids.",
        "Apply fungicides and use resistant varieties.",
        "No treatment required.",
        "Use fungicides and remove infected plant debris.",
        "Control by pruning affected wood and applying fungicides.",
        "Control with fungicides and proper air circulation.",
        "No treatment required.",
        "Control the Asian Citrus Psyllid to prevent spread.",
        "Apply copper-based bactericides.",
        "No treatment required.",
        "Treat with copper-based bactericides and remove infected leaves.",
        "No treatment required.",
        "Control with fungicides and crop rotation.",
        "Treat with fungicides and ensure proper drainage.",
        "No treatment required.",
        "No treatment required.",
        "No treatment required.",
        "Apply sulfur-based fungicides and improve air circulation.",
        "Provide adequate water and improve soil drainage.",
        "No treatment required.",
        "Treat with copper-based bactericides.",
        "Use fungicides and rotate crops.",
        "Treat with fungicides and remove infected plants.",
        "Control humidity and apply fungicides.",
        "Remove infected leaves and apply fungicides.",
        "Treat with miticides and improve humidity.",
        "Treat with fungicides and ensure good air circulation.",
        "Control whiteflies to prevent spread.",
        "Remove infected plants and control aphids.",
        "No treatment required."
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_disease)

        //initializeModel(applicationContext)

        imageView = findViewById(R.id.imageView)
        resultTextView = findViewById(R.id.resultTextView)
        selectImageButton = findViewById(R.id.selectImageButton)
        //captureImageButton = findViewById(R.id.captureImageButton)
        predictButton = findViewById(R.id.predictButton)
        textViewDiseaseDescription = findViewById<TextView>(R.id.textViewDiseaseDescription)
        textViewTreatment = findViewById<TextView>(R.id.textViewTreatment)
        textViewLink = findViewById<TextView>(R.id.textViewLink)
        textViewPest = findViewById<TextView>(R.id.textViewPest)
        translateButton = findViewById(R.id.translateButton)
        // Disable predict button initially
        predictButton.isEnabled = false

        // Load the TensorFlow Lite model
        tflite = Interpreter(loadModelFile(this,"plant_disease_model.tflite"))

        // Select image from gallery

        selectImageButton.setOnClickListener {
            // Reset all UI elements to their original state
            imageView.setImageResource(R.drawable.scan_leaf) // Clear the image
            resultTextView.text = "Prediction Result" // Clear the result text
            textViewDiseaseDescription.text = "Disease Description" // Clear the disease description
            textViewTreatment.text = "Treatment" // Clear the treatment text
            textViewLink.text = "Click here for more details" // Clear the link text
            textViewPest.text = "Pest Information" // Clear the pest text
            predictButton.isEnabled = false // Disable the predict button
            translateButton.visibility=View.GONE

            // Open the image selection dialog
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE)
        }

        // Capture image using the camera
//        captureImageButton.setOnClickListener {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAPTURE_IMAGE_REQUEST_CODE)
//            } else {
//                openCamera()
//            }
//        }

        // Predict the disease based on the selected or captured image
        predictButton.setOnClickListener {
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val predictedDisease = predictDisease(bitmap)
            resultTextView.text = "Predicted Disease: $predictedDisease"
            translateButton.visibility= View.VISIBLE
        }
    }

    private fun initializeModel(context: Context) {
        try {
            val tfliteModel = loadModelFile(context,"plant_disease_model.tflite")
            tflite = Interpreter(tfliteModel)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Open the camera to capture an image
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST_CODE)
    }

    // Handle the result from selecting or capturing an image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SELECT_IMAGE_REQUEST_CODE -> {
                    val uri = data?.data
                    imageView.setImageURI(uri)
                }
//                CAPTURE_IMAGE_REQUEST_CODE -> {
//                    val bitmap = data?.extras?.get("data") as Bitmap
//                    imageView.setImageBitmap(bitmap)
//                }
            }
            // Enable the predict button after an image is selected or captured
            predictButton.isEnabled = true
        }
    }

    // Function to load model from assets
//    private fun loadModelFile(modelFile: String): ByteBuffer {
//        val assetFileDescriptor = assets.openFd(modelFile)
//        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
//        val fileChannel = fileInputStream.channel
//        val startOffset = assetFileDescriptor.startOffset
//        val declaredLength = assetFileDescriptor.declaredLength
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
//    }

    private fun loadModelFile(context: Context, modelFile: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelFile)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Preprocess the image before prediction
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val inputImageBuffer = ByteBuffer.allocateDirect(1 * 150 * 150 * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true)
        val intValues = IntArray(150 * 150)
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        var pixel = 0
        for (i in 0 until 150) {
            for (j in 0 until 150) {
                val value = intValues[pixel++]
                inputImageBuffer.putFloat(((value shr 16 and 0xFF) / 255.0f))
                inputImageBuffer.putFloat(((value shr 8 and 0xFF) / 255.0f))
                inputImageBuffer.putFloat((value and 0xFF) / 255.0f)
            }
        }

        return inputImageBuffer
    }

    // Predict the disease from the image
    fun predictDisease(bitmap: Bitmap): String {
        val inputBuffer = preprocessImage(bitmap)

        // Allocate output buffer for 38 classes (4 bytes per float)
        val outputBuffer = ByteBuffer.allocateDirect(38 * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        // Run inference
        tflite.run(inputBuffer, outputBuffer)

        // Get the predicted class index
        outputBuffer.rewind()
        val resultArray = FloatArray(38)
        outputBuffer.asFloatBuffer().get(resultArray)

        // Log the raw model output
        Log.d("ModelOutput", resultArray.joinToString(", "))

        // Find the index of the highest probability
        val maxIndex = resultArray.indices.maxByOrNull { resultArray[it] } ?: -1

        // Check if the index is within bounds and handle unknown cases
        return if (maxIndex in diseaseClasses.indices) {
            val predictedDisease = diseaseClasses[maxIndex]
            val pestInfo = pestsCausingDisease[maxIndex]
            val description = diseaseDescription[maxIndex]
            val treatment = diseaseTreatment[maxIndex]

            val resultText = "Predicted Disease: $predictedDisease , Pest: $pestInfo , Description: $description , Treatment: $treatment"
            resultTextView.text = "Predicted Disease: $predictedDisease"
            textViewPest.text = "Pest: $pestInfo"
            textViewDiseaseDescription.text = "Description: $description"
            textViewTreatment.text = "Treatment: $treatment"

            val searchQuery = "$predictedDisease information"
            val url = "https://www.google.com/search?q=${Uri.encode(searchQuery)}"

            textViewLink.text = "Find out more about $predictedDisease"

            textViewLink.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }

            // Logging the result for debugging
            Log.d("DiseasePrediction", resultText)

            predictedDisease
            resultText
        } else {
            "Unknown disease"
        }
    }

    fun cpredictDisease(bitmap: Bitmap): String {
        val inputBuffer = preprocessImage(bitmap)

        // Allocate output buffer for 38 classes (4 bytes per float)
        val outputBuffer = ByteBuffer.allocateDirect(38 * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        // Run inference
        tflite.run(inputBuffer, outputBuffer)

        // Get the predicted class index
        outputBuffer.rewind()
        val resultArray = FloatArray(38)
        outputBuffer.asFloatBuffer().get(resultArray)

        // Log the raw model output
        Log.d("ModelOutput", resultArray.joinToString(", "))

        // Find the index of the highest probability
        val maxIndex = resultArray.indices.maxByOrNull { resultArray[it] } ?: -1

        // Check if the index is within bounds and handle unknown cases
        return if (maxIndex in diseaseClasses.indices) {
            val predictedDisease = diseaseClasses[maxIndex]
            val pestInfo = pestsCausingDisease[maxIndex]
            val description = diseaseDescription[maxIndex]
            val treatment = diseaseTreatment[maxIndex]

            val searchQuery = "$predictedDisease information"
            val url = "https://www.google.com/search?q=${Uri.encode(searchQuery)}"

            val resultText = "The image you have passed is processed and the results are as follows - \nPredicted Disease: $predictedDisease \nPest: $pestInfo \nDescription: $description \nTreatment: $treatment"
//            resultTextView.text = "Predicted Disease: $predictedDisease"
//            textViewPest.text = "Pest: $pestInfo"
//            textViewDiseaseDescription.text = "Description: $description"
//            textViewTreatment.text = "Treatment: $treatment"



//            textViewLink.text = "Find out more about $predictedDisease"
//
//            textViewLink.setOnClickListener {
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                startActivity(intent)
//            }

            // Logging the result for debugging
            Log.d("DiseasePrediction", resultText)

            predictedDisease
            resultText
        } else {
            "Unknown disease"
        }
    }


}