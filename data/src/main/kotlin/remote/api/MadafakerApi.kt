package remote.api

import com.bbuddies.gotogether.common_domain.model.Message
import com.bbuddies.gotogether.common_domain.model.Reply
import com.bbuddies.gotogether.common_domain.model.User
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    fun getCurrentUser(): User

    @Headers(CONTENT_TYPE)
    @GET("/api/message/current/incoming")
    fun getIncomingMassage(): Message

    @Headers(CONTENT_TYPE)
    @GET("/api/message/current/outcoming")
    fun getOutcomingMassage(): Message

    @Headers(CONTENT_TYPE)
    @GET("/api/reply/:{id}")
    fun getReplyById(@Path("id") id: String): Reply


    ///UPDATE

    @Headers(CONTENT_TYPE)
    @PATCH("/api/user/current")
    fun updateCurrentUser(@Body name: String)

    @Headers(CONTENT_TYPE)
    @PATCH("/api/reply")
    fun updateReply(@Body id: String, isPublic: Boolean)

    ////CREATE

    @Headers(CONTENT_TYPE)
    @POST("/api/user")
    fun createUser(@Body name: String)

    @Headers(CONTENT_TYPE)
    @POST("/api/message")
    fun createMessage(@Body body: String, mode: String)

    @Headers(CONTENT_TYPE)
    @POST("/api/reply")
    fun createReply(@Body body: String? = null, isPublic: Boolean, parentId: String? = null)//TODO
}

object RetrofitInstance {
    private const val BASE_URL = ""

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val madafakerWebService: MadafakerApi by lazy {
        retrofit.create(MadafakerApi::class.java)
    }
}