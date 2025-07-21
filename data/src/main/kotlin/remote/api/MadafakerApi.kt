package remote.api

import com.bbuddies.madafaker.common_domain.model.User
import remote.api.dto.MessageDto
import remote.api.dto.ReplyDto
import remote.api.request.CreateMessageRequest
import remote.api.request.CreateReplyRequest
import remote.api.request.CreateUserRequest
import remote.api.response.NameAvailabilityResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

const val CONTENT_TYPE = "Content-Type: application/json"

interface MadafakerApi {

    ///GET
    @Headers(CONTENT_TYPE)
    @GET("/api/user/current")
    suspend fun getCurrentUser(): User

    @Headers(CONTENT_TYPE)
    @GET("/api/message/current/incoming")
    suspend fun getIncomingMessages(): List<MessageDto>

    @Headers(CONTENT_TYPE)
    @GET("/api/message/current/outcoming")
    suspend fun getOutcomingMessages(): List<MessageDto>

    @Headers(CONTENT_TYPE)
    @GET("/api/reply/{id}")
    suspend fun getReplyById(@Path("id") id: String): ReplyDto


    ///UPDATE
    @Headers(CONTENT_TYPE)
    @PATCH("/api/user/current")
    suspend fun updateCurrentUser(@Body name: String): User

    @Headers(CONTENT_TYPE)
    @PATCH("/api/reply")
    suspend fun updateReply(@Body id: String, isPublic: Boolean)

    ////CREATE
    @Headers(CONTENT_TYPE)
    @POST("/api/user")
    suspend fun createUser(@Body request: CreateUserRequest): User

    @Headers(CONTENT_TYPE)
    @POST("/api/message")
    suspend fun createMessage(@Body request: CreateMessageRequest): MessageDto

    @Headers(CONTENT_TYPE)
    @POST("/api/reply")
    suspend fun createReply(@Body request: CreateReplyRequest): ReplyDto

    @Headers(CONTENT_TYPE)
    @GET("/api/user/check-name-availability")
    suspend fun checkNameAvailability(@Query("name") name: String): NameAvailabilityResponse
}