package remote.api

import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.common_domain.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

const val CONTENT_TYPE = "Content-Type: application/json"

interface MadafakerApi {

    ///GET
    @Headers(CONTENT_TYPE)
    @GET("/api/user/current")
    suspend fun getCurrentUser(): User

    @Headers(CONTENT_TYPE)
    @GET("/api/message/current/incoming")
    suspend fun getIncomingMassage(): List<Message>

    @Headers(CONTENT_TYPE)
    @GET("/api/message/current/outcoming")
    suspend fun getOutcomingMassage(): List<Message>

    @Headers(CONTENT_TYPE)
    @GET("/api/reply/:{id}")
    suspend fun getReplyById(@Path("id") id: String): Reply


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
    suspend fun createUser(@Body name: String): User

    @Headers(CONTENT_TYPE)
    @POST("/api/message")
    suspend fun createMessage(@Body body: String, mode: String): Message

    @Headers(CONTENT_TYPE)
    @POST("/api/reply")
    suspend fun createReply(@Body body: String? = null, isPublic: Boolean, parentId: String? = null)//TODO

    @Headers(CONTENT_TYPE)
    @GET("/api/user/check-name/{name}")
    suspend fun checkNameAvailability(@Path("name") name: String): Boolean

}