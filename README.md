# CROPHARV - A Machine Learning-Based Approach for Crop Prediction for Harvesting

## Project Description

CROPHARV is a machine learning-driven digital platform designed to empower farmers with tools for effective crop management and decision-making. It provides precise predictions of crop yields, identifies plant diseases, and offers real-time weather forecasts and market price predictions. By integrating these capabilities, CROPHARV enables farmers to maximize productivity, make data-driven decisions, and adapt to changing weather conditions. The platform also features a user-friendly chatbot for quick access to vital agricultural information.

## Features

### 1. User Authentication
- Registration: New users can register using their name, phone number, and email address, creating a personalized account.
- Login: Existing users can log in using their email and password, securely accessing their dashboard.

### 2. Home Page
- Displays real-time weather forecasts, providing essential weather data that helps farmers plan their activities.
- Offers quick access to crop predictions, market price analysis, and the latest updates.

### 3. Crop Yield Prediction Page
- Utilizes machine learning models to predict crop yields based on input months and weather data.
- Helps farmers determine the best time for harvesting, allowing for efficient resource management and planning.

### 4. Plant Disease Prediction Page
- Allows farmers to take pictures of affected crops and upload them for analysis.
- Uses image recognition algorithms to diagnose crop diseases and suggests preventive measures or treatments.

### 5. Market Price Prediction
- Analyzes historical market data to forecast future crop prices.
- Provides insights on optimal selling times, helping farmers maximize profits by staying updated on market trends.

### 6. Chatbot
- Offers real-time assistance by answering common queries related to crop management, weather conditions, and market trends.
- Ensures farmers have easy access to information and support when needed.

### 7. Profile Page
- Allows users to manage their personal information and preferences.
- Includes a **Logout** option for secure account management.

## Technologies Used
- **Programming Language**: Python, Java (Android Development)
- **Machine Learning Frameworks**: TensorFlow, Keras
- **Database**: Firebase Firestore for real-time data management
- **Image Processing**: OpenCV for plant disease analysis
- **Backend**: Node.js for API management
- **Cloud Platform**: Google Cloud Platform for deployment
- **Android**: Developed as a mobile application for easy access by farmers

## Installation

### Prerequisites
- Python 3.8+
- Android Studio for mobile app development
- Firebase account for database integration
- Google Cloud Platform for deploying machine learning models

### Steps to Run the Project
1. Clone the repository:
   
   git clone https://github.com/dharshaasri/CropHarv.git
   cd CropHarv

2.Set up the Python environment and install dependencies:
 	bash

	pip install -r requirements.txt

 - Configure Firebase and add your google-services.json file to the Android project.

 - Train the machine learning models using the provided datasets and save them for deployment.

 - Deploy the backend services on Google Cloud Platform.

 - Build the Android app using Android Studio and run it on an emulator or a physical device.
