package app.fine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.fine.ui.FineApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as FineApplication
        val repository = app.container.expenseRepository

        setContent {
            FineApp(repository = repository)
        }
    }
}
