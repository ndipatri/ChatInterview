package com.example.chatinterview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatinterview.ui.theme.ChatInterviewTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID


/*
This is a interview question.. I'm asked to use the following specifications and from this,
create a chat program....

I started with the vanilla 'New Project' in Android Studio...

I will write all of this in this single file for simplicity....

The ViewModel is instantiated manually, as opposed to using dependency injection or viewModelOf(), for simplicity.

I've created a fake message repository for demonstration purposes. It flips a coin to decide if outgoing message
succeeds or now.  If it succeeds, it replies with an incoming message.

I've created an abstraction layer for the messages.  Messages from the repo are converted to
ChatMessages in the ViewModel.  This is so the OutgoingMessages can be easily rendered in the
chat history along with their send status for 'echo' purposes.

*/

// *** START SPECIFICATIONS ***
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
// *** STOP SPECIFICATIONS ***










class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ChatViewModel()

        enableEdgeToEdge()
        setContent {

            val coroutineScope = rememberCoroutineScope()

            val uiState = viewModel.uiState.collectAsState()

            ChatInterviewTheme() {
                Scaffold { padding ->
                    Column(Modifier.padding(padding)
                                   .background(Color.Black)
                    ) {
                        if (uiState.value.isEmpty()) {
                            Text(
                                modifier = Modifier.weight(.50f),
                                text = "Empty Conversation",
                                color = Color.White
                            )
                        } else {
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .weight(.5f)
                                    .padding(20.dp)
                                    .verticalScroll(scrollState)
                            ) {
                                for (message in uiState.value) {
                                    when (message) {
                                        is ChatViewModel.ChatMessage.IncomingImageChatMessage -> {
                                            Text(
                                                text = "Image Message",
                                                color = Color(message.color)
                                            )
                                        }

                                        is ChatViewModel.ChatMessage.IncomingTextChatMessage -> {
                                            val textMessage =
                                                message as ChatViewModel.ChatMessage.IncomingTextChatMessage

                                            Text(text = textMessage.message, color = Color.White)
                                        }

                                        is ChatViewModel.ChatMessage.OutgoingChatMessage -> {
                                            val textMessage =
                                                message as ChatViewModel.ChatMessage.OutgoingChatMessage

                                            Row(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = ">  ",
                                                    color = Color.White
                                                )

                                                Text(
                                                    text = textMessage.message,
                                                    color = when (message.status) {
                                                        ChatViewModel.STATUS.FAILURE -> Color.Red
                                                        ChatViewModel.STATUS.SUCCESS -> Color.Green
                                                        ChatViewModel.STATUS.PENDING -> Color.White
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // As a new message arrives, scroll to it
                            LaunchedEffect(uiState.value) {
                                scrollState.scrollTo(scrollState.maxValue)
                            }
                        }

                        var sendText by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier
                                .weight(.5f)
                                .padding(20.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            TextField(
                                modifier = Modifier.weight(.70f),
                                value = sendText,
                                onValueChange = {
                                    sendText = it
                                })

                            Button(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .weight(.3f),
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.sendMessage(sendText)
                                    }
                                }
                            ) {
                                Text(text = "Send", maxLines = 1)
                            }
                        }
                        // As a new message arrives, clear text
                        LaunchedEffect(uiState.value) {
                            sendText = ""
                        }
                    }
                }
            }
        }
    }
}

class ChatViewModel : ViewModel() {

    val uiState = MutableStateFlow<List<ChatMessage>>(listOf())

    fun sendMessage(message: String) {
        val uuid = UUID.randomUUID()

        // Immediately echo back...
        uiState.update {
            it.toMutableList().apply {
                add(ChatMessage.OutgoingChatMessage(message, uuid))
            }
        }

        // .. and send to agent...
        viewModelScope.launch {
            messageRepository.sendMessage(OutgoingMessage(message, uuid))
        }
    }

    // For now, create fake MessageRepository that acts like a chat agent.
    private val messageRepository = object : MessagingRepository {

        override val incomingMessages = MutableSharedFlow<IncomingMessage>()
        override val messageResults = MutableSharedFlow<SendConfirmation>()

        override suspend fun sendMessage(message: OutgoingMessage) {

            // TODO - Instead of sending to a real agent, i'm just going to pretend locally...
            viewModelScope.launch {
                // simulate processing transmission delay
                delay(500)

                // random success
                val success = (0..1).random() == 1

                // Confirmation
                messageResults.emit(SendConfirmation(message.id, if (success) SendConfirmation.Result.Success else SendConfirmation.Result.Failure))

                if (success) {
                    incomingMessages.emit(TextualMessage("Wow, you are so smart for saying: ${message.message} "))
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            // Monitor incoming messages and convert to ChatMessage for UI
            messageRepository.incomingMessages.collect { incomingMessage ->

                var incomingChatMessage: ChatMessage = when (incomingMessage) {
                    is TextualMessage -> {
                         ChatMessage.IncomingTextChatMessage(incomingMessage.message)
                    }

                    is ImageMessage -> {
                        ChatMessage.IncomingImageChatMessage(incomingMessage.color)
                    }
                }

                uiState.update {
                    it.toMutableList().apply {
                        add(incomingChatMessage)
                    }
                }
            }
        }

        viewModelScope.launch {
            messageRepository.messageResults.collect { sendConfirmation ->

                // update existing message status
                // could have used a filter here ...
                uiState.value = uiState.value.map { chatMessage ->
                    chatMessage.let {
                        if (it is ChatMessage.OutgoingChatMessage && it.id == sendConfirmation.id) {

                            val success = sendConfirmation.result == SendConfirmation.Result.Success

                            it.copy(
                                message = if (success) it.message else "${it.message} (send FAILED)",
                                status = if (success) STATUS.SUCCESS else STATUS.FAILURE
                            )
                        } else {
                            it
                        }
                    }
                }
            }
        }
    }

    sealed class ChatMessage {
        data class OutgoingChatMessage(
            val message: String,
            val id: UUID,
            var status: STATUS = STATUS.PENDING
        ) : ChatMessage()

        data class IncomingImageChatMessage(@ColorInt val color: Int) : ChatMessage()
        data class IncomingTextChatMessage(val message: String) : ChatMessage()
    }

    enum class STATUS {
        PENDING,
        SUCCESS,
        FAILURE
    }
}
