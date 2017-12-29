package backend.routes

import backend.ContentsGet
import backend.model.Header
import org.jetbrains.ktor.locations.get
import org.jetbrains.ktor.response.respond
import org.jetbrains.ktor.routing.Route

fun Route.contentsGet() = get<ContentsGet> {
    call.respond(call.notes().map { Header(it.id!!, it.title, it.ref, it.parentId, it.published) })
}










