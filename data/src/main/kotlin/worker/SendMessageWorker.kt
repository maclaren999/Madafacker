package worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import local.MadafakerDao
import remote.api.MadafakerApi
import remote.api.request.CreateMessageRequest
import remote.asMessageDB
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@HiltWorker
class SendMessageWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val webService: MadafakerApi,
    private val preferenceManager: PreferenceManager,
    private val localDao: MadafakerDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_MESSAGE_BODY = "message_body"
        const val KEY_MESSAGE_MODE = "message_mode"
        const val KEY_RETRY_COUNT = "retry_count"
        const val KEY_ERROR_MESSAGE = "error_message"
        const val KEY_MESSAGE_ID = "message_id"
        const val KEY_SENT_AT = "sent_at"

        const val MAX_RETRY_ATTEMPTS = 5
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val messageBody = inputData.getString(KEY_MESSAGE_BODY)
            ?: return@withContext Result.failure(
                workDataOf(KEY_ERROR_MESSAGE to "Missing message body")
            )

        val messageMode = inputData.getString(KEY_MESSAGE_MODE)
            ?: return@withContext Result.failure(
                workDataOf(KEY_ERROR_MESSAGE to "Missing message mode")
            )

        val retryCount = inputData.getInt(KEY_RETRY_COUNT, 0)

        Timber.d("SendMessageWorker: Attempting to send message (attempt ${retryCount + 1}/$MAX_RETRY_ATTEMPTS)")

        try {
            // Create the message via API
            val newMessage = webService.createMessage(
                CreateMessageRequest(messageBody, messageMode)
            )

            // Store in local database
            localDao.insertMessage(newMessage.asMessageDB())

            // Clear the draft since message was sent successfully
            preferenceManager.clearUnsentDraft()

            Timber.d("SendMessageWorker: Message sent successfully - ID: ${newMessage.id}")

            // Return success with result data
            return@withContext Result.success(
                workDataOf(
                    KEY_MESSAGE_BODY to messageBody,
                    KEY_MESSAGE_ID to newMessage.id,
                    KEY_SENT_AT to System.currentTimeMillis().toString()
                )
            )

        } catch (exception: Exception) {
            Timber.e(exception, "SendMessageWorker: Failed to send message")

            val updatedRetryCount = retryCount + 1

            return@withContext when {
                // Max retries exceeded
                updatedRetryCount >= MAX_RETRY_ATTEMPTS -> {
                    Timber.w("SendMessageWorker: Max retries exceeded for message")
                    Result.failure(
                        workDataOf(
                            KEY_ERROR_MESSAGE to "Max retries exceeded: ${exception.localizedMessage}",
                            KEY_RETRY_COUNT to updatedRetryCount
                        )
                    )
                }

                // Network/server errors - retry with backoff
                isRetryableError(exception) -> {
                    Timber.d("SendMessageWorker: Retryable error, scheduling retry")
                    Result.retry()
                }

                // Non-retryable errors (e.g., validation errors)
                else -> {
                    Timber.w("SendMessageWorker: Non-retryable error: ${exception.localizedMessage}")
                    Result.failure(
                        workDataOf(
                            KEY_ERROR_MESSAGE to (exception.localizedMessage ?: "Unknown error"),
                            KEY_RETRY_COUNT to updatedRetryCount
                        )
                    )
                }
            }
        }
    }

    private fun isRetryableError(exception: Exception): Boolean {
        return when (exception) {
            is SocketTimeoutException,
            is UnknownHostException,
            is IOException -> true

            is retrofit2.HttpException -> {
                // Retry on server errors (5xx) but not client errors (4xx)
                exception.code() >= 500
            }

            else -> false
        }
    }
}