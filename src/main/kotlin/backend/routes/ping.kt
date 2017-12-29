package backend.routes

import backend.Ping
import org.jetbrains.ktor.locations.get
import org.jetbrains.ktor.response.respond
import org.jetbrains.ktor.routing.Route

fun Route.ping() = get<Ping> {
    call.respond("ping - OK")
}