import io.javalin.testtools.JavalinTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

class TodoApiTest {
    companion object {
        private val dataSource = createDataSource()
        val repository = TodoRepository(dataSource)
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())

        fun clearTodos() {
            dataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("TRUNCATE todos RESTART IDENTITY")
                }
            }
        }
    }

    @BeforeEach
    fun resetDatabase() {
        clearTodos()
    }

    @Test
    fun `GET returns an empty list when there are no todos`() {
        JavalinTest.test(createApp(repository)) {_, client ->
            val response = client.get("/api/todos")

            assertEquals(200, response.code)
            assertEquals("[]", response.body?.string())
        }
    }

    @Test
    fun `POST creates a todo and returns it with an id`() {
        JavalinTest.test(createApp(repository)) { _, client ->
            val response = client.post("/api/todos", mapOf("text" to "buy milk"))

            assertEquals(201, response.code)

            val created: Todo = mapper.readValue(response.body!!.string())
            assertEquals("buy milk", created.text)
            assertEquals(false, created.done)

            assertEquals(1, repository.findAll().size)
        }
    }

    @Test
    fun `POST rejects blank text with 400`() {
        JavalinTest.test(createApp(repository)) { _, client ->
            val response = client.post("/api/todos", mapOf("text" to "   "))

            assertEquals(400, response.code)
            assertEquals(0, repository.findAll().size)
        }
    }

    @Test
    fun `DELETE with an unknown id returns 404`() {
        JavalinTest.test(createApp(repository)) { _, client ->
            val response = client.delete("/api/todos/999999")

            assertEquals(404, response.code)
        }
    }
}