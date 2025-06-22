package worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bbuddies.madafaker.common_domain.model.MessageState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import local.MadafakerDao
import remote.api.MadafakerApi
import remote.api.dto.toDomainModel
import remote.api.request.CreateMessageRequest
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@HiltWorker
class SendMessageWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val webService: MadafakerApi,
    private val localDao: MadafakerDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_MESSAGE_BODY = "message_body"
        const val KEY_MESSAGE_MODE = "message_mode"
        const val KEY_TEMP_MESSAGE_ID = "temp_message_id"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val messageBody = inputData.getString(KEY_MESSAGE_BODY) ?: return@withContext Result.failure()
        val messageMode = inputData.getString(KEY_MESSAGE_MODE) ?: return@withContext Result.failure()
        val tempMessageId = inputData.getString(KEY_TEMP_MESSAGE_ID) ?: return@withContext Result.failure()

        try {
            // Send to server using existing API
            val serverMessage = webService.createMessage(
                CreateMessageRequest(messageBody, messageMode)
            )

            // Replace temp message with server message
            localDao.deleteMessage(tempMessageId)
            localDao.insertMessage(
                serverMessage.toDomainModel().copy(
                    localState = MessageState.SENT,
                    localCreatedAt = System.currentTimeMillis(),
                    needsSync = false
                )
            )

            Result.success()

        } catch (exception: Exception) {
            when {
                isRetryableError(exception) -> Result.retry()
                else -> {
                    // Mark as failed
                    val tempMessage = localDao.getMessageById(tempMessageId)
                    if (tempMessage != null) {
                        localDao.updateMessage(tempMessage.copy(localState = MessageState.FAILED))
                    }
                    Result.failure()
                }
            }
        }
    }

    private fun isRetryableError(exception: Exception): Boolean {
        return when (exception) {
            is SocketTimeoutException, is UnknownHostException, is IOException -> true
            is retrofit2.HttpException -> exception.code() >= 500
            else -> false
        }
    }
}