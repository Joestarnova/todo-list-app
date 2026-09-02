import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.javalin.Javalin
import io.javalin.json.JavalinJackson

private val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

fun main() {
    val app = Javalin.create() { config ->
        config.jsonMapper(JavalinJackson(objectMapper))
    }.start(Config.port)

    app.get("/health") { ctx ->
        ctx.json(mapOf("status" to "ok"))
    }
}
