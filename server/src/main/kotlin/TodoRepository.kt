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
            connection.prepareStatement("INSERT INTO todos (text) VALUES (?) RETURNING *").use { statement ->
                statement.setString(1, text)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.toTodo()
                }
            }
        }
    }

    fun findById(id: Long): Todo? {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT * FROM todos WHERE id = ?").use { statement ->
                statement.setLong(1, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.toTodo() else null
                }
            }
        }
    }

    fun update(id: Long, text: String, done: Boolean): Todo? {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE todos SET text = ?, done = ? WHERE id = ? RETURNING *").use { statement ->
                statement.setString(1, text)
                statement.setBoolean(2, done)
                statement.setLong(3, id)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.toTodo() else null
                }
            }
        }
    }

    fun delete(id: Long): Boolean {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("DELETE FROM todos WHERE id = ?").use { statement ->
                statement.setLong(1, id)
                statement.executeUpdate() > 0
            }
        }
    }

    fun deleteCompleted(): Int {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("DELETE FROM todos WHERE done = TRUE").use { statement ->
                statement.executeUpdate()
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


