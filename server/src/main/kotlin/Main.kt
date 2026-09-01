import io.javalin.Javalin

fun main() {
    val app = Javalin.create().start(7070)

    app.get("/health") { ctx ->
        ctx.json(mapOf("status" to "ok"))
    }
}
