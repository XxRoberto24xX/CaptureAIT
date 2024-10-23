plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.captureait.captureait"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.captureait.captureait"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    /* ADD LIBRARIES */

    /* Firebase */
    implementation(libs.play.services.auth)     //allows us to use google service to authenticate users using their accounts
    implementation(platform(libs.firebase.bom)) //include the same firebase libraries for all the different databases we are using
    implementation(libs.firebase.auth)          //authentication using firebase
    implementation(libs.firebase.firestore)     //user data using firebase
    implementation(libs.firebase.database)      //rooms code database

    /* User photos */
    implementation(libs.circleimageview)        //circular images
    implementation(libs.glide)                  //takes google user image

    /* Animations */
    implementation("com.airbnb.android:lottie:4.1.0")       //allows to user lottie animations as jason

    /* Game rooms */
    implementation("io.socket:socket.io-client:2.0.0")      //rooms to connect concurrent connected players

    /* Photo analysis */
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
}