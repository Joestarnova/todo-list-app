import java.time.OffsetDateTime

data class Todo(
    val id: Long,
    val text: String,
    val done: Boolean,
    val createdAt: OffsetDateTime
)

data class CreateTodoRequest(
    val text: String,
)

data class UpdateTodoRequest(
    val text: String? = null,
    val done: Boolean? = null,
)