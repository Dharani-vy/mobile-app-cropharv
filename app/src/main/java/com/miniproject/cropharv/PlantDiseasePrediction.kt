package com.miniproject.cropharv

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
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
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.IOException
import java.nio.MappedByteBuffer


class PlantDiseasePrediction : AppCompatActivity() {

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
    private lateinit var translateTextView: TextView
    private lateinit var tflite: Interpreter
    private lateinit var bottomNavigationView: BottomNavigationView

    // Constants for selecting and capturing images
    private val SELECT_IMAGE_REQUEST_CODE = 100
   // private val CAPTURE_IMAGE_REQUEST_CODE = 101


//    init {
//        initializeModel(context)
//    }

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

    private val diseaseTranslations = mapOf(
        "Apple___Apple_scab" to "ஆப்பிள்___ஆப்பிள் செப்பு",
        "Apple___Black_rot" to "ஆப்பிள்___கருப்பு சிதைவு",
        "Apple___Cedar_apple_rust" to "ஆப்பிள்___சிடார் ஆப்பிள் குழம்பு",
        "Apple___healthy" to "ஆப்பிள்___ஆரோக்கியம்",
        "Blueberry___healthy" to "புளியக்காய்___ஆரோக்கியம்",
        "Cherry_(including_sour)___Powdery_mildew" to "செர்ரி_(கசப்பில் உட்பட)___புட்டுகுழல் மழை",
        "Cherry_(including_sour)___healthy" to "செர்ரி_(கசப்பில் உட்பட)___ஆரோக்கியம்",
        "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot" to "மக்காச்சோளம்_(மக்கா)___செர்கோஸ்போரை இலைக் கசப்பினம் சாம்பு இலைக் கசப்பினம்",
        "Corn_(maize)___Common_rust_" to "மக்காச்சோளம்_(மக்கா)___பொதுவான குழம்பு",
        "Corn_(maize)___Northern_Leaf_Blight" to "மக்காச்சோளம்_(மக்கா)___வடக்கு இலை துரும்பு",
        "Corn_(maize)___healthy" to "மக்காச்சோளம்_(மக்கா)___ஆரோக்கியம்",
        "Grape___Black_rot" to "தேன்___கருப்பு சிதைவு",
        "Grape___Esca_(Black_Measles)" to "தேன்___எஸ்கா_(கருப்பு மாசு)",
        "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)" to "தேன்___இலை துரும்பு_(இசாரியோப்சிஸ் இலைக் கசப்பினம்)",
        "Grape___healthy" to "தேன்___ஆரோக்கியம்",
        "Orange___Haunglongbing_(Citrus_greening)" to "கொய்யா___ஹுங்லோங்பிங்_(சிட்ரஸ் பச்சை)",
        "Peach___Bacterial_spot" to "பீச்___பாக்டீரியா கசப்பு",
        "Peach___healthy" to "பீச்___ஆரோக்கியம்",
        "Pepper,_bell___Bacterial_spot" to "மிளகாய்,_அகழி___பாக்டீரியா கசப்பு",
        "Pepper,_bell___healthy" to "மிளகாய்,_அகழி___ஆரோக்கியம்",
        "Potato___Early_blight" to "உருளைக்கிழங்கு___முதலாம் துரும்பு",
        "Potato___Late_blight" to "உருளைக்கிழங்கு___இறுதியில் துரும்பு",
        "Potato___healthy" to "உருளைக்கிழங்கு___ஆரோக்கியம்",
        "Raspberry___healthy" to "ராஸ்பெர்ரி___ஆரோக்கியம்",
        "Soybean___healthy" to "சோயா பழம்___ஆரோக்கியம்",
        "Squash___Powdery_mildew" to "புட்டினைக்காய்___புட்டுகுழல் மழை",
        "Strawberry___Leaf_scorch" to "ஸ்ட்ராபெர்ரி___இலை எரிச்சல்",
        "Strawberry___healthy" to "ஸ்ட்ராபெர்ரி___ஆரோக்கியம்",
        "Tomato___Bacterial_spot" to "தக்காளி___பாக்டீரியா கசப்பு",
        "Tomato___Early_blight" to "தக்காளி___முதலாம் துரும்பு",
        "Tomato___Late_blight" to "தக்காளி___இறுதியில் துரும்பு",
        "Tomato___Leaf_Mold" to "தக்காளி___இலை மொச்சு",
        "Tomato___Septoria_leaf_spot" to "தக்காளி___செப்டோரியா இலைக் கசப்பு",
        "Tomato___Spider_mites Two-spotted_spider_mite" to "தக்காளி___உச்சி துரும்புகள் இரண்டு பூச்சிகள்",
        "Tomato___Target_Spot" to "தக்காளி___இலக்கு கசப்பு",
        "Tomato___Tomato_Yellow_Leaf_Curl_Virus" to "தக்காளி___தக்காளி மஞ்சள் இலைச் சுற்று வைரஸ்",
        "Tomato___Tomato_mosaic_virus" to "தக்காளி___தக்காளி மொசைக்ஸ் வைரஸ்",
        "Tomato___healthy" to "தக்காளி___ஆரோக்கியம்"
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

    private val pestTranslations = mapOf(
        "Apple scab fungus" to "ஆப்பிள் செப்பு பூஞ்சை",
        "Black rot fungus" to "கருப்பு சிதைவு பூஞ்சை",
        "Cedar apple rust fungus" to "சிடார் ஆப்பிள் குழம்பு பூஞ்சை",
        "None" to "எதுவுமில்லை",
        "Powdery mildew fungus" to "புட்டுகுழல் மழை பூஞ்சை",
        "Cercospora fungus" to "செர்கோஸ்போரா பூஞ்சை",
        "Common rust fungus" to "பொதுவான குழம்பு பூஞ்சை",
        "Northern Leaf Blight fungus" to "வடக்கு இலை துரும்பு பூஞ்சை",
        "Black rot fungus" to "கருப்பு சிதைவு பூஞ்சை",
        "Esca fungus" to "எஸ்கா பூஞ்சை",
        "Isariopsis Leaf Spot fungus" to "இசாரியோப்சிஸ் இலைக் கசப்பு பூஞ்சை",
        "Asian Citrus Psyllid" to "ஆசிய சிட்ரஸ் பசிலிட்",
        "Bacterial pathogen" to "பாக்டீரியா நோய்வாயு",
        "Alternaria fungus" to "ஆல்டெர்னரியா பூஞ்சை",
        "Phytophthora infestans" to "பைட்டோப்தோரா இன்ஃபெஸ்டன்ஸ்",
        "Leaf mold fungus" to "இலை மொச்சு பூஞ்சை",
        "Septoria fungus" to "செப்டோரியா பூஞ்சை",
        "Spider mites" to "உச்சி துரும்புகள்",
        "Target Spot fungus" to "இலக்கு கசப்பு பூஞ்சை",
        "Tomato Yellow Leaf Curl Virus (spread by whiteflies)" to "தக்காளி மஞ்சள் இலைச் சுற்று வைரஸ் (வெள்ளைபற்களைப் பரப்புகிறது)",
        "Tomato Mosaic Virus" to "தக்காளி மொசைக்ஸ் வைரஸ்"
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

    private val diseaseDescriptionTranslations = mapOf(
        "Apple scab shows as dark, scabby lesions on leaves and fruit." to "ஆப்பிள் செப்பு காய்களும், பழங்களிலும் இருண்ட, செப்பாகிய காயங்களாகத் தெரிவிக்கிறது.",
        "Black rot appears as concentric rings of rot on apples." to "கருப்பு சிதைவு ஆப்பிள் மீதும் சுற்றி வரும்போது முளைக்கும் வளையங்களாகத் தெரிவிக்கிறது.",
        "Cedar apple rust causes bright orange spots on leaves." to "சிடார் ஆப்பிள் குழம்பு இலைகளில் பிரகாசமான ஆரஞ்சு கண்ணிககளை உருவாக்குகிறது.",
        "Healthy apple with no disease signs." to "ரோக அறிகுறிகள் இல்லாத ஆரோக்கியமான ஆப்பிள்.",
        "Healthy blueberry plant with no disease signs." to "ரோக அறிகுறிகள் இல்லாத ஆரோக்கியமான நீலப்பூசணி.",
        "Powdery mildew shows white, powdery fungus on leaves." to "புட்டுகுழல் மழை இலைகளில் வெள்ளை, புட்டு பூஞ்சை காட்டுகிறது.",
        "Healthy cherry plant with no disease signs." to "ரோக அறிகுறிகள் இல்லாத ஆரோக்கியமான செங்கதிர்.",
        "Gray leaf spot appears as small, rectangular lesions on corn leaves." to "மஞ்சள் இலைக் கசப்பு மக்காச்சோள இலைகளில் சிறிய, சதுரமாக இருக்கும் காயங்களாகத் தெரிவிக்கிறது.",
        "Common rust causes pustules on corn leaves." to "பொதுவான குழம்பு மக்காச்சோள இலைகளில் பாதிப்புகளை உருவாக்குகிறது.",
        "Northern Leaf Blight forms long, elliptical lesions on corn leaves." to "வடக்கு இலை துரும்பு மக்காச்சோள இலைகளில் நீளமான, அண்டத்தின் காயங்களை உருவாக்குகிறது.",
        "Healthy corn plant with no disease signs." to "ரோக அறிகுறிகள் இல்லாத ஆரோக்கியமான மக்காச்சோள்.",
        "Black rot forms black fruiting bodies on grape leaves and fruits." to "கருப்பு சிதைவு திராட்சை இலைகளில் மற்றும் பழங்களில் கருப்பு விளைவுகளை உருவாக்குகிறது.",
        "Esca shows as brownish streaks on grape wood and fruits." to "எஸ்கா திராட்சையின் மரத்திலும் பழங்களிலும் பழுப்ப நிற ஓட்டங்களாகத் தெரிவிக்கிறது.",
        "Leaf blight appears as brown spots surrounded by yellow halos." to "இலை சிதைவு மஞ்சள் முளைப்பூங்கொட்டுடன் சுருங்கிய பழுப்பு கசப்புகளாகத் தெரிவிக்கிறது.",
        "Healthy grape plant with no disease signs." to "ரோக அறிகுறிகள் இல்லாத ஆரோக்கியமான திராட்சை.",
        "Citrus greening causes yellowing of leaves and deformed fruits." to "சிட்ரஸ் பச்சை இலைகளின் மஞ்சலாகும் மற்றும் வஞ்சனை பழங்கள் உருவாக்குகிறது.",
        "Bacterial spot causes small, dark, water-soaked lesions on leaves and fruit." to "பாக்டீரியா கசப்பு இலைகளிலும் பழங்களிலும் சிறிய, இருண்ட, நீரிழிந்த காயங்களை உருவாக்குகிறது.",
        "Healthy peach plant with no disease signs." to "ரோக அறிகுறிகள் இல்லாத ஆரோக்கியமான பீச்சு.",
        "Bacterial spot causes small, dark lesions on leaves and fruits." to "பாக்டீரியா கசுப்பு இலைகளிலும் பழங்களிலும் சிறிய, இருண்ட காயங்களை உருவாக்குகிறது.",
        "Healthy pepper plant with no disease signs." to "ரோக அறிகுறிகள் இல்லாத ஆரோக்கியமான மிளகு.",
        "Early blight causes brown spots with concentric rings on potato leaves." to "முதல் சிதைவு உருளைக்கிழங்கு இலைகளில் சுற்று வளையங்களுடன் பழுப்பு கசப்புகளை உருவாக்குகிறது.",
        "Late blight causes dark lesions on leaves and tubers." to "தாமத சிதைவு இலைகளிலும் வேர் கிழங்குகளில் இருண்ட காயங்களை உருவாக்குகிறது.",
        "Healthy potato plant with no disease signs." to "ரோக அறிகுறிகள் இல்லாத ஆரோக்கியமான உருளைக்கிழங்கு.",
        "Healthy raspberry plant with no disease signs." to "ரோக அறிகுறிகள் இல்லாத ஆரோக்கியமான ராஸ்பெரி.",
        "Healthy soybean plant with no disease signs." to "ரோக அறிகுறிகள் இல்லாத ஆரோக்கியமான சோயா.",
        "Powdery mildew shows white, powdery fungus on squash leaves." to "புட்டுகுழல் மழை குக்கூசுக்கு வெள்ளை, புட்டு பூஞ்சை காட்டுகிறது.",
        "Leaf scorch appears as reddish-brown edges on strawberry leaves." to "இலைக் கசப்பு சொற்கையைப் போல வெண்ணிற மற்றும் பழுப்பு கசப்பாகத் தெரிவிக்கிறது.",
        "Healthy strawberry plant with no disease signs." to "ரோக அறிகுறிகள் இல்லாத ஆரோக்கியமான ஸ்ட்ராபெரி.",
        "Bacterial spot causes small, dark lesions on tomato leaves and fruits." to "பாக்டீரியா கசப்பு தக்காளி இலைகளிலும் பழங்களிலும் சிறிய, இருண்ட காயங்களை உருவாக்குகிறது.",
        "Early blight causes brown spots with concentric rings on tomato leaves." to "முதல் சிதைவு தக்காளி இலைகளில் சுற்று வளையங்களுடன் பழுப்பு கசப்புகளை உருவாக்குகிறது.",
        "Late blight causes dark lesions on tomato leaves and fruits." to "தாமத சிதைவு தக்காளி இலைகளிலும் பழங்களில் இருண்ட காயங்களை உருவாக்குகிறது.",
        "Leaf mold appears as pale green spots that turn brown on tomato leaves." to "இலை மொச்சு தக்காளி இலைகளில் பழுப்பு நிறமாக மாறும் மஞ்சள் நிறம் தோன்றுகிறது.",
        "Septoria leaf spot causes small, dark lesions on tomato leaves." to "செப்டோரியா இலைக் கசப்பு தக்காளி இலைகளில் சிறிய, இருண்ட காயங்களை உருவாக்குகிறது.",
        "Spider mites cause tiny yellow or white spots on tomato leaves." to "உச்சி துரும்புகள் தக்காளி இலைகளில் சிறிய மஞ்சள் அல்லது வெள்ளை புள்ளிகளை உருவாக்குகின்றன.",
        "Target spot forms dark, concentric lesions on tomato leaves." to "இலக்கு கசப்பு தக்காளி இலைகளில் இருண்ட, சுற்று காயங்களை உருவாக்குகிறது.",
        "Tomato Yellow Leaf Curl Virus causes yellowing and curling of tomato leaves." to "தக்காளி மஞ்சள் இலைச் சுற்று வைரஸ் தக்காளி இலைகளில் மஞ்சலாகும் மற்றும் சுற்றுகின்றது.",
        "Tomato Mosaic Virus causes mottled, yellowish patterns on tomato leaves." to "தக்காளி மொசைக்ஸ் வைரஸ் தக்காளி இலைகளில் மஞ்சள் நிறம் ஆவாகக் கொள்வது.",
        "Healthy tomato plant with no disease signs." to "ரோக அறிகுறிகள் இல்லாத ஆரோக்கியமான தக்காளி."
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

    private val diseaseTreatmentTranslations = mapOf(
        "Treat with fungicide sprays during wet weather." to "மழையாலான காலத்தில் பூஞ்சிகரப்புகளை அடித்து கையாளவும்.",
        "Control with fungicide and proper pruning of infected branches." to "பூஞ்சிகரப்புகள் மற்றும் பாதிக்கப்பட்ட கிளைகளை சரியாகக் கட்டுப்படுத்தவும்.",
        "Remove nearby cedar trees and apply fungicides." to "அருகிலுள்ள சிடார் மரங்களை அகற்று மற்றும் பூஞ்சிகரப்புகளைப் பயன்படுத்தவும்.",
        "No treatment required." to "சிகிச்சை தேவை இல்லை.",
        "No treatment required." to "சிகிச்சை தேவை இல்லை.",
        "Apply sulfur-based fungicides and ensure good air circulation." to "சல்பர் அடிப்படையிலான பூஞ்சிகரப்புகளைப் பயன்படுத்தி, நல்ல காற்றோட்டத்தை உறுதி செய்யவும்.",
        "No treatment required." to "சிகிச்சை தேவை இல்லை.",
        "Rotate crops and apply resistant hybrids." to "பண்ணையை சுழற்று மற்றும் எதிர்ப்பு உயிரினங்களைப் பயன்படுத்தவும்.",
        "Treat with fungicides and use resistant hybrids." to "பூஞ்சிகரப்புகளைப் பயன்படுத்தி எதிர்ப்பு உயிரினங்களைப் பயன்படுத்தவும்.",
        "Apply fungicides and use resistant varieties." to "பூஞ்சிகரப்புகளைப் பயன்படுத்தி எதிர்ப்பு வகைகளைப் பயன்படுத்தவும்.",
        "No treatment required." to "சிகிச்சை தேவை இல்லை.",
        "Use fungicides and remove infected plant debris." to "பூஞ்சிகரப்புகளைப் பயன்படுத்தி பாதிக்கப்பட்ட உற்பத்திகளை அகற்று.",
        "Control by pruning affected wood and applying fungicides." to "பாதிக்கப்பட்ட மரங்களைப் பயிர் செய்து பூஞ்சிகரப்புகளைப் பயன்படுத்தவும்.",
        "Control with fungicides and proper air circulation." to "பூஞ்சிகரப்புகளைப் பயன்படுத்தி மற்றும் நல்ல காற்றோட்டத்தை உறுதி செய்யவும்.",
        "No treatment required." to "சிகிச்சை தேவை இல்லை.",
        "Control the Asian Citrus Psyllid to prevent spread." to "அசியா சிட்ரஸ் பிச்சி பரவுவதைத் தடுப்பதற்கு கட்டுப்படுத்தவும்.",
        "Apply copper-based bactericides." to "காப்பர் அடிப்படையிலான பாக்டீரியா எதிர்ப்புகளைப் பயன்படுத்தவும்.",
        "No treatment required." to "சிகிச்சை தேவை இல்லை.",
        "Treat with copper-based bactericides and remove infected leaves." to "காப்பர் அடிப்படையிலான பாக்டீரியா எதிர்ப்புகளைப் பயன்படுத்தி பாதிக்கப்பட்ட இலைகளை அகற்று.",
        "No treatment required." to "சிகிச்சை தேவை இல்லை.",
        "Control with fungicides and crop rotation." to "பூஞ்சிகரப்புகளைப் பயன்படுத்தி மற்றும் பயிர் சுழற்பத்தியை உறுதி செய்யவும்.",
        "Treat with fungicides and ensure proper drainage." to "பூஞ்சிகரப்புகளைப் பயன்படுத்தி சரியான நீர் வெளியேற்றத்தை உறுதி செய்யவும்.",
        "No treatment required." to "சிகிச்சை தேவை இல்லை.",
        "No treatment required." to "சிகிச்சை தேவை இல்லை.",
        "No treatment required." to "சிகிச்சை தேவை இல்லை.",
        "Apply sulfur-based fungicides and improve air circulation." to "சல்பர் அடிப்படையிலான பூஞ்சிகரப்புகளைப் பயன்படுத்தி காற்றோட்டத்தை மேம்படுத்தவும்.",
        "Provide adequate water and improve soil drainage." to "சரியான நீரை வழங்கி, மண்ணின் வெளியேற்றத்தை மேம்படுத்தவும்.",
        "No treatment required." to "சிகிச்சை தேவை இல்லை.",
        "Treat with copper-based bactericides." to "காப்பர் அடிப்படையிலான பாக்டீரியா எதிர்ப்புகளைப் பயன்படுத்தவும்.",
        "Use fungicides and rotate crops." to "பூஞ்சிகரப்புகளைப் பயன்படுத்தி மற்றும் பயிர்களை மாற்றவும்.",
        "Treat with fungicides and remove infected plants." to "பூஞ்சிகரப்புகளைப் பயன்படுத்தி பாதிக்கப்பட்ட செடிகளை அகற்று.",
        "Control humidity and apply fungicides." to "ஊட்டச்சத்து கட்டுப்படுத்து மற்றும் பூஞ்சிகரப்புகளைப் பயன்படுத்தவும்.",
        "Remove infected leaves and apply fungicides." to "பாதிக்கப்பட்ட இலைகளை அகற்று மற்றும் பூஞ்சிகரப்புகளைப் பயன்படுத்தவும்.",
        "Treat with miticides and improve humidity." to "மிடிசிட்களைப் பயன்படுத்தி மற்றும் ஈரத்தை மேம்படுத்தவும்.",
        "Treat with fungicides and ensure good air circulation." to "பூஞ்சிகரப்புகளைப் பயன்படுத்தி மற்றும் நல்ல காற்றோட்டத்தை உறுதி செய்யவும்.",
        "Control whiteflies to prevent spread." to "பரவலாகக் கட்டுப்படுத்தவும்.",
        "Remove infected plants and control aphids." to "பாதிக்கப்பட்ட செடிகளை அகற்று மற்றும் ஆபிட்களை கட்டுப்படுத்தவும்.",
        "No treatment required." to "சிகிச்சை தேவை இல்லை."
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_disease)

        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")
        val phoneNumber = intent.getStringExtra("phoneNumber")
        bottomNavigationView = findViewById(R.id.bottomNavView)
        bottomNavigationView.selectedItemId = R.id.navItem4

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

        initializeModel(applicationContext)

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
        translateTextView=findViewById(R.id.translateTextView)
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
            translateTextView.visibility=View.GONE
            // Open the image selection dialog
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE)
        }

        // Predict the disease based on the selected or captured image
        predictButton.setOnClickListener {
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val predictedDisease = predictDisease(bitmap)
            resultTextView.text = "Predicted Disease: $predictedDisease"
            translateButton.visibility= View.VISIBLE
        }
        translateButton.setOnClickListener {
            // Extract the actual values by removing the prefixes
            val diseaseClass = resultTextView.text.toString().substringAfter(": ").trim() // Get the predicted disease
            val pestInfo = textViewPest.text.toString().substringAfter(": ").trim() // Get the pest info
            val description = textViewDiseaseDescription.text.toString().substringAfter(": ").trim() // Get the description
            val treatment = textViewTreatment.text.toString().substringAfter(": ").trim() // Get the treatment

            val translatedText = getTranslation(diseaseClass, pestInfo, description, treatment)
            val result = "Translated:\n $translatedText"
            translateTextView.text = result
            translateTextView.visibility = View.VISIBLE // Display the translated text in the TextView
            translateButton.visibility=View.GONE
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

    private fun getTranslation(diseaseClass: String, pestInfo: String, description: String, treatment: String): String {
        val translatedClass = diseaseTranslations[diseaseClass] ?: "தகவல் இல்லை."
        val translatedPestInfo = pestTranslations[pestInfo] ?: "தகவல் இல்லை."
        val translatedDescription = diseaseDescriptionTranslations[description] ?: "தகவல் இல்லை."
        val translatedTreatment = diseaseTreatmentTranslations[treatment] ?: "தகவல் இல்லை."

        val result = "நோய் வகை: $translatedClass \n" + " உள்ள விஷம்: $translatedPestInfo \n" +
                "விளக்கம்: $translatedDescription \n" + "சிகிச்சை: $translatedTreatment"

        return result.trimIndent()
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

            // Create styled text for each TextView
            val predictedDiseaseText = SpannableStringBuilder()
                .append("Predicted Disease: ", ForegroundColorSpan(Color.RED), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append(predictedDisease, ForegroundColorSpan(Color.BLACK), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            val pestInfoText = SpannableStringBuilder()
                .append("Pest: ", ForegroundColorSpan(Color.RED), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append(pestInfo, ForegroundColorSpan(Color.BLACK), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            val descriptionText = SpannableStringBuilder()
                .append("Description: ", ForegroundColorSpan(Color.RED), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append(description, ForegroundColorSpan(Color.BLACK), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            val treatmentText = SpannableStringBuilder()
                .append("Treatment: ", ForegroundColorSpan(Color.RED), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                .append(treatment, ForegroundColorSpan(Color.BLACK), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Set the styled text to TextViews
            resultTextView.text = predictedDiseaseText
            textViewPest.text = pestInfoText
            textViewDiseaseDescription.text = descriptionText
            textViewTreatment.text = treatmentText

            val searchQuery = "$predictedDisease information"
            val url = "https://www.google.com/search?q=${Uri.encode(searchQuery)}"

            textViewLink.text = "Find out more about $predictedDisease"

            textViewLink.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }

            // Logging the result for debugging
            Log.d("DiseasePrediction", "Predicted Disease: $predictedDisease, Pest: $pestInfo, Description: $description, Treatment: $treatment")

            predictedDisease
        } else {
            "Unknown disease"
        }
    }


}