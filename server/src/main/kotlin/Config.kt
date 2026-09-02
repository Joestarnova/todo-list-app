object Config {
    val dbUrl: String = System.getenv("DB_URL")
        ?: "jdbc:postgresql://localhost:5432/todo"
    val dbUser: String = System.getenv("DB_USER") ?: "todo"
    val dbPassword: String = System.getenv("DB_PASSWORD") ?: "todo"
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 7070
}