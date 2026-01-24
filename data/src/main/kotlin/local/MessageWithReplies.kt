package local

import androidx.room.Embedded
import androidx.room.Relation
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply

data class MessageWithReplies(
    @Embedded val message: Message,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentMessageId"
    )
    val replies: List<Reply>
)
