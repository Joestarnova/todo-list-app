import io.javalin.Javalin
import io.javalin.http.bodyAsClass
import io.javalin.http.NotFoundResponse
import io.javalin.http.BadRequestResponse

object TodoRoutes {
    fun register(app: Javalin, repository: TodoRepository) {
        app.get("/api/todos") { ctx ->
            ctx.json(repository.findAll())
        }

        app.post("/api/todos") { ctx ->
            val request = ctx.bodyAsClass<CreateTodoRequest>()
            val todo = repository.create(request.text)
            ctx.status(201).json(todo)
        }

        app.put("/api/todos/{id}") { ctx ->
            val id = ctx.pathParam("id").toLong()
            val request = ctx.bodyAsClass<UpdateTodoRequest>()
            val todo = repository.update(id, request.text, request.done)
                ?: throw NotFoundResponse("No todo with id $id")
            ctx.json(todo)
        }

        app.delete("/api/todos/{id}") { ctx ->
            val id = ctx.pathParam("id").toLong()
            if (!repository.delete(id)) {
                throw NotFoundResponse("No todo with id $id")
            }
            ctx.status(204)
        }

        app.delete("/api/todos") { ctx ->
            if (ctx.queryParam("done") != "true") {
                throw BadRequestResponse("Expected ?done=true")
            }
            ctx.json(mapOf("deleted" to repository.deleteCompleted()))
        }
    }
}