import backend.UserSession
import backend.dao
import backend.routes.*
import org.jetbrains.ktor.application.Application
import org.jetbrains.ktor.application.install
import org.jetbrains.ktor.config.ApplicationConfig
import org.jetbrains.ktor.content.TextContent
import org.jetbrains.ktor.features.CORS
import org.jetbrains.ktor.features.CallLogging
import org.jetbrains.ktor.features.DefaultHeaders
import org.jetbrains.ktor.features.StatusPages
import org.jetbrains.ktor.gson.GsonSupport
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.Locations
import org.jetbrains.ktor.response.respond
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.routing
import org.jetbrains.ktor.sessions.Sessions
import org.jetbrains.ktor.sessions.cookie
import java.time.Duration

lateinit var backendConfig: ApplicationConfig

fun Application.main() {
    backendConfig = environment.config.config("backend")
    dao.init(backendConfig.config("db"))
    install(DefaultHeaders)
    install(CallLogging)
    install(GsonSupport)
    install(CORS) {
        method(HttpMethod.Get)
        header(HttpHeaders.XForwardedProto)
        anyHost()
        allowCredentials = true
        maxAge = Duration.ofDays(1)
    }
    install(StatusPages){
        status(HttpStatusCode.NotFound) {
            call.respond(TextContent("${it.value} ${it.description}", ContentType.Text.Plain.withCharset(Charsets.UTF_8), it))
        }
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    install(Sessions) {
        cookie<UserSession>("SESSION") {
            cookie.path = "/"
        }
    }
    install(Locations)
    routing {
        userGet()
        usersGet()
        userDelete()
        contentsGet()
        notesGet()
        commentsGet()
        register()
        login()
        oauthGoogle()
        oauthGoogleCode()
        logout()
        noteCreate()
        noteUpdate()
        noteDelete()
        commentAdd()
        commentDelete()
        ping()
        get("/{query}") {
            val query = call.parameters["query"]
            query?.let { call.respondText("query: $query") } ?: call.respondText("?")
        }
        get("/") {
            call.respondText("ok")
        }
    }
}