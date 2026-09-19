import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import kotlin.system.exitProcess
import org.slf4j.LoggerFactory
import io.javalin.http.NotFoundResponse

private val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

private val logger = LoggerFactory.getLogger("Main")

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

    val todoRepository = TodoRepository(dataSource)

    val app = Javalin.create() { config ->
        config.jsonMapper(JavalinJackson(objectMapper))

        config.bundledPlugins.enableCors { cors ->
            cors.addRule { rule ->
                rule.allowHost("http://localhost:5173")
            }
        }

        config.requestLogger.http { ctx, ms ->
            logger.info("${ctx.method()} ${ctx.path()} -> ${ctx.status()} (${ms.toInt()}ms)")
        }
    }.start(Config.port)

    app.exception(ValidationException::class.java) { e, ctx ->
        ctx.status(400).json(mapOf("error" to (e.message ?: "invalid request")))
    }

    app.exception(Exception::class.java) { e, ctx ->
        logger.error("Unhandled exception: ${ctx.method()} ${ctx.path()}", e)
        ctx.status(500).json(mapOf("error" to "internal server error"))
    }

    app.exception(NotFoundResponse::class.java) { e, ctx ->
        ctx.status(404).json(mapOf("error" to (e.message ?: "not found")))
    }

    app.get("/health") { ctx ->
        ctx.json(mapOf("status" to "ok"))
    }

    TodoRoutes.register(app, todoRepository)
}
