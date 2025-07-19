package remote.api.request

data class CreateUserRequest(val name: String,val registrationToken: String)
data class CreateMessageRequest(val body: String, val mode: String)
data class CreateReplyRequest(val body: String, val public: Boolean, val parentId: String)
