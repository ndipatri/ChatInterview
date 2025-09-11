package com.example.chatinterview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.chatinterview.ui.theme.ChatInterviewTheme

import androidx.annotation.ColorInt
import kotlinx.coroutines.flow.Flow
import java.util.UUID




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatInterviewTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChatInterviewTheme {
        Greeting("Android")
    }
}








interface MessagingRepository {
    val incomingMessages: Flow<IncomingMessage>
    val messageResults: Flow<SendConfirmation>

    suspend fun sendMessage(message: OutgoingMessage)
}

sealed interface IncomingMessage
data class TextualMessage(val message: String) : IncomingMessage
data class ImageMessage(@ColorInt val color: Int) : IncomingMessage

data class OutgoingMessage(val message: String, val id: UUID)

data class SendConfirmation(val id: UUID, val result: Result) {
    sealed interface Result {
        data object Success : Result
        data object Failure : Result
    }
}