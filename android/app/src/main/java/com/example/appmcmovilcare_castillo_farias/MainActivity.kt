package com.example.appmcmovilcare_castillo_farias

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.appmcmovilcare_castillo_farias.ui.AccesoApp
import com.example.appmcmovilcare_castillo_farias.ui.theme.AppMCmovilCare_Castillo_FariasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppMCmovilCare_Castillo_FariasTheme { AccesoApp() } }
    }
}
