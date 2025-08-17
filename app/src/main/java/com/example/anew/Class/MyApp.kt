package com.example.anew.Class

import android.app.Application
import com.backendless.Backendless

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Backendless.initApp(this,
            "C245AEDC-3AC3-46DD-8E1B-0FB56EF88B1E",  // Replace with your Backendless App ID
            "7A1CBB89-53E1-42A6-AA8B-B3D51250D8DB"  // Replace with your Backendless API Key
        )
    }
}