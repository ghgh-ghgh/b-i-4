package com.example.bi4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.bi4.ui.StudentApp
import com.example.bi4.ui.theme.Bài4Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Bài4Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StudentApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
