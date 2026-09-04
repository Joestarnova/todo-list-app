import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

fun createDataSource(): HikariDataSource {
    val config = HikariConfig()

    config.jdbcUrl = Config.dbUrl
    config.username = Config.dbUser
    config.password = Config.dbPassword
    config.maximumPoolSize = 5

    return HikariDataSource(config)
}

fun checkConnection(dataSource: HikariDataSource) {
    dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT 1").use { resultSet ->
                resultSet.next()
            }
        }
    }
}