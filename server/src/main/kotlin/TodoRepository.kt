import java.sql.ResultSet
import java.time.OffsetDateTime
import javax.sql.DataSource

class TodoRepository(private val dataSource: DataSource) {
    fun findAll(): List<Todo> {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT * FROM todos ORDER BY created_at").use { statement ->
                statement.executeQuery().use { resultSet ->
                    val todos = mutableListOf<Todo>()
                    while (resultSet.next()) {
                        todos.add(resultSet.toTodo())
                    }
                    todos
                }
            }
        }
    }

    fun create(text: String): Todo {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO todos (text) VAlUES (?) RETURNING *").use { statement ->
                statement.setString(1, text)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.toTodo()
                }
            }
        }
    }
}

private fun ResultSet.toTodo(): Todo = Todo(
    id = getLong("id"),
    text = getString("text"),
    done = getBoolean("done"),
    createdAt = getObject("created_at", OffsetDateTime::class.java)
)


