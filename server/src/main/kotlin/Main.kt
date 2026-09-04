import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import kotlin.system.exitProcess

private val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

fun main() {
    val dataSource = try {
        val ds = createDataSource()
        checkConnection(ds)
        ds
    } catch (e: Exception) {
        System.err.println("FATAL: could not connect to database")
        System.err.println(" URL: ${Config.dbUrl}")
        System.err.println(" User: ${Config.dbUser}")
        System.err.println(" Cause : ${e.message}")
        System.err.println("Is Postgres running? Try: docker compose up -d")
        exitProcess(1)
    }
    val app = Javalin.create() { config ->
        config.jsonMapper(JavalinJackson(objectMapper))
    }.start(Config.port)

    app.get("/health") { ctx ->
        ctx.json(mapOf("status" to "ok"))
    }
}
