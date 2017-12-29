package backend

import backend.dao.DAOFacade
import backend.dao.DAOFacadeDatabase
import backend.model.User
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.sessions.sessions
import org.jetbrains.ktor.sessions.set
import java.util.*

data class UserSession(val userId: String, val secretKey: Int)

var dao: DAOFacade = DAOFacadeDatabase()

fun ApplicationCall.newSession(user: User) {
    val secretKey = Random(1_000_000).nextInt()
    val sessionId = user.name + secretKey
    dao.newSession(sessionId, user.name, user.passwordHash)
    sessions.set(UserSession(user.name, secretKey))
}