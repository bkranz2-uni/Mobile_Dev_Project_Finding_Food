package com.example.mobile_dev_project_finding_food

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.mobile_dev_project_finding_food.ui.debug.SupabaseDebugScreen
import com.example.mobile_dev_project_finding_food.ui.theme.Mobile_Dev_Project_Finding_FoodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mobile_Dev_Project_Finding_FoodTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Temporary: debug screen to verify Supabase. Replace with the map screen.
                    SupabaseDebugScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
