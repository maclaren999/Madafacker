package remote.api.interceptors

import com.bbuddies.madafaker.common_domain.AppConfig
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@Suppress("KotlinConstantConditions")
class MockInterceptor @Inject constructor() : Interceptor {

    private val moshi = Moshi.Builder().build()

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!AppConfig.USE_MOCK_API) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()
        val url = request.url.toString()
        val method = request.method

        return when {
            url.contains("/api/user/current") && method == "GET" -> {
                mockResponse(request, 200, mockCurrentUser())
            }

            url.contains("/api/user") && method == "POST" -> {
                mockResponse(request, 201, mockCreateUser())
            }

            url.contains("/api/user/current") && method == "PATCH" -> {
                mockResponse(request, 200, mockCurrentUser())
            }

            url.contains("/api/user/check-name-availability") -> {
                mockResponse(request, 200, """{"nameIsAvailable": true}""")
            }

            url.contains("/api/message/current/incoming") -> {
                mockResponse(request, 200, mockIncomingMessages())
            }

            url.contains("/api/message/current/outcoming") -> {
                mockResponse(request, 200, mockOutcomingMessages())
            }

            url.contains("/api/message") && method == "POST" -> {
                mockResponse(request, 201, mockCreateMessage())
            }

            url.contains("/api/reply") && method == "POST" -> {
                mockResponse(request, 201, mockCreateReply())
            }

            url.contains("/api/reply/") && method == "GET" -> {
                mockResponse(request, 200, mockGetReply())
            }

            else -> {
                // Fallback to real API for unmocked endpoints
                chain.proceed(request)
            }
        }
    }

    private fun mockResponse(request: okhttp3.Request, code: Int, body: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_2)
            .code(code)
            .message("OK")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun mockCurrentUser(): String {
        return """
            {
                "id": "mock-user-123",
                "name": "MockUser",
                "registrationToken": "mock-fcm-token",
                "coins": 42,
                "createdAt": "2024-01-01T00:00:00.000Z",
                "updatedAt": "2024-01-01T00:00:00.000Z"
            }
        """.trimIndent()
    }

    private fun mockCreateUser(): String {
        return mockCurrentUser()
    }

    private fun mockIncomingMessages(): String {
        return """
            [
                {
                    "id": "msg-1",
                    "body": "Hey there! Hope you're having a great day ☀️",
                    "mode": "light",
                    "createdAt": "2024-01-15T10:30:00.000Z",
                    "author": {
                        "id": "user-456",
                        "name": "MockSender1",
                        "createdAt": "2024-01-01T00:00:00.000Z"
                    },
                    "ratingStats": {
                        "likes": 5,
                        "dislikes": 0,
                        "superLikes": 1
                    }
                },
                {
                    "id": "msg-2",
                    "body": "Sometimes the best therapy is a long drive with good music",
                    "mode": "light", 
                    "createdAt": "2024-01-15T09:15:00.000Z",
                    "author": {
                        "id": "user-789",
                        "name": "MockSender2",
                        "createdAt": "2024-01-01T00:00:00.000Z"
                    },
                    "ratingStats": {
                        "likes": 3,
                        "dislikes": 1,
                        "superLikes": 0
                    }
                },
                {
                    "id": "msg-3",
                    "body": "Why do we call it 'rush hour' when nobody's moving?",
                    "mode": "dark",
                    "createdAt": "2024-01-15T08:45:00.000Z",
                    "author": {
                        "id": "user-321",
                        "name": "MockSender3",
                        "createdAt": "2024-01-01T00:00:00.000Z"
                    },
                    "ratingStats": {
                        "likes": 10,
                        "dislikes": 2,
                        "superLikes": 3
                    }
                }
            ]
        """.trimIndent()
    }

    private fun mockOutcomingMessages(): String {
        return """
            [
                {
                    "id": "my-msg-1",
                    "body": "Sending positive vibes to everyone out there! 🌟",
                    "mode": "light",
                    "createdAt": "2024-01-14T16:20:00.000Z",
                    "updatedAt": "2024-01-14T16:20:00.000Z",
                    "author": {
                        "id": "mock-user-123",
                        "name": "MockUser",
                        "createdAt": "2024-01-01T00:00:00.000Z"
                    },
                    "ratingStats": {
                        "likes": 2,
                        "dislikes": 0,
                        "superLikes": 1
                    }
                },
                {
                    "id": "my-msg-2",
                    "body": "Coffee is just bean soup and I'm okay with that",
                    "mode": "dark",
                    "createdAt": "2024-01-14T14:30:00.000Z",
                    "updatedAt": "2024-01-14T14:30:00.000Z",
                    "author": {
                        "id": "mock-user-123",
                        "name": "MockUser",
                        "createdAt": "2024-01-01T00:00:00.000Z"
                    },
                    "ratingStats": {
                        "likes": 8,
                        "dislikes": 1,
                        "superLikes": 2
                    }
                }
            ]
        """.trimIndent()
    }

    private fun mockCreateMessage(): String {
        return """
            {
                "id": "new-msg-${UUID.randomUUID()}",
                "body": "Your new message",
                "mode": "light",
                "createdAt": "${Date()}",
                "updatedAt": "${Date()}",
                "author": {
                    "id": "mock-user-123",
                    "name": "MockUser",
                    "createdAt": "2024-01-01T00:00:00.000Z"
                },
                "ratingStats": {
                    "likes": 0,
                    "dislikes": 0,
                    "superLikes": 0
                }
            }
        """.trimIndent()
    }

    private fun mockCreateReply(): String {
        return """
            {
                "id": "reply-${UUID.randomUUID()}",
                "body": "Great message!",
                "mode": "light",
                "createdAt": "${Date()}",
                "author": {
                    "id": "mock-user-123",
                    "name": "MockUser",
                    "createdAt": "2024-01-01T00:00:00.000Z"
                }
            }
        """.trimIndent()
    }

    private fun mockGetReply(): String {
        return mockCreateReply()
    }
}