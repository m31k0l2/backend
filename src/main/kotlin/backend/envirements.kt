package backend

import backend.dao.DAOFacade
import backend.dao.DAOFacadeDatabase
import backend.model.User
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.sessions.sessions
import org.jetbrains.ktor.sessions.set
import java.util.*

data class UserSession(val sid: String)

var dao: DAOFacade = DAOFacadeDatabase()

fun ApplicationCall.newSession(user: User, token: String?=null) {
    val salt = Random(1_000_000).nextInt()
    val sessionId = "${(token ?: user.id).hashCode()}$salt"
    dao.newSession(sessionId, user.id)
    sessions.set(UserSession(sessionId))
}