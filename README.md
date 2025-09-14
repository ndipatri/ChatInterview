# ChatInterview

This is a interview question.. I'm asked to use the following specifications and from this,
create a chat program....

I started with the vanilla 'New Project' in Android Studio...

I will write all of this in this single file for simplicity....

The ViewModel is instantiated manually, as opposed to using dependency injection or viewModelOf(), for simplicity.

I've created a fake message repository for demonstration purposes. It flips a coin to decide if outgoing message
succeeds or now.  If it succeeds, it replies with an incoming message.

I've created an abstraction layer for the messages.  Messages from the repo are converted to
ChatMessages in the ViewModel.  This is for 'echo' purposes: the OutgoingMessages can be easily rendered in the
chat history along with their send status.

```
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
```


![chatInterview](https://github.com/user-attachments/assets/dc63fb25-ef8c-4fd0-951d-9e0365fd5581)

