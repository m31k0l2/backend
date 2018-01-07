package backend.routes

import backend.*
import backend.model.RespondUser
import backend.model.User
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.locations.get
import org.jetbrains.ktor.locations.post
import org.jetbrains.ktor.request.PartData
import org.jetbrains.ktor.request.receiveMultipart
import org.jetbrains.ktor.response.respond
import org.jetbrains.ktor.routing.Route
import org.jetbrains.ktor.sessions.clear
import org.jetbrains.ktor.sessions.get
import org.jetbrains.ktor.sessions.sessions

class RegisterException(e: String) : Exception(e)

fun ApplicationCall.user(): User {
    val session = sessions.get<UserSession>() ?: throw Exception("Вход не произведён")
    val sid = session.sid
    val userId = dao.userIdBySid(sid) ?: run {
        sessions.clear<UserSession>()
        throw Exception("Сессия окончена")
    }
    return dao.user(userId) ?: throw Exception("Пользователь [$userId] не зарегистрирован")
}

fun ApplicationCall.userOrNull(): User? {
    val session = sessions.get<UserSession>() ?: return null
    val sid = session.sid
    val userId = dao.userIdBySid(sid) ?: run {
        sessions.clear<UserSession>()
        return null
    }
    return dao.user(userId)
}

fun Route.register() = get<Register> {
    try {
        val session = call.sessions.get<UserSession>()
        if (session != null) {
            dao.userIdBySid(session.sid)?.let { throw RegisterException("Вход уже осуществлен, нужно выйти") }
            call.sessions.clear<UserSession>()
        }
        val name = it.name
        val password = it.password
        if (name.length < 2) throw RegisterException("Логин должен быть длиннее двух символов")
        if (password.length < 6) throw RegisterException("Пароль должен быть длиннее шести символов")
        val hash = password.hashCode().toString()
        val id = "site${name.hashCode()}"
        val newUser = User(id, name, passwordHash = hash, isAdmin = dao.usersCount() == 0)
        dao.createUser(newUser)
        call.newSession(newUser)
        call.respond(LoginResponse(RespondUser(newUser.name, "", newUser.isAdmin)))
    } catch (e: RegisterException) {
        call.respond(LoginResponse(error = e.message))
    } catch (e: Exception) {
        call.respond(LoginResponse(error = "Пользователь с таким именем уже зарегистрирован"))
    }
}

fun Route.userDelete() = get<UserDelete> {
    try {
        val user = call.user()
        if (!user.isAdmin) throw Exception("Нет доступа")
        dao.comments(user.id).forEach { dao.deleteComment(it.id!!) }
        dao.deleteUser(it.userId)
        call.respond(HttpStatusCode.OK)
    } catch (e: Exception) {
        call.respond(LoginResponse(error = e.message))
    }
}

fun Route.login() = get<Login> {
    try {
        val session = call.sessions.get<UserSession>()
        if (session != null) {
            dao.userIdBySid(session.sid)?.let { throw Exception("Вход уже осуществлен, нужно выйти") }
            call.sessions.clear<UserSession>()
        }
        val id = "site${it.name.hashCode()}"
        val password = it.password
        val user = dao.user(id, password.hashCode().toString()) ?:
                throw Exception("Пользователь не зарегистрирован")
        call.newSession(user)
        call.respond(LoginResponse(RespondUser(user.name, user.imageURL, user.isAdmin)))
    } catch (e: Exception) {
        call.respond(LoginResponse(error = e.message))
    }
}

fun Route.oauth() = post<OAuth> {
    try {
        val multipart = call.receiveMultipart()
        val post = multipart.parts.mapNotNull {
            if (it is PartData.FormItem) {
                it.partName!! to it.value
            } else null
        }.toList().toMap()
        val userId = post["id"]!!
        val name = post["name"]!!
        val email = post["email"]!!
        val imageURL = post["imageURL"]!!
        val token = post["token"]!!
        var user = dao.user(userId)
        if (user == null) {
            user = User(userId, name, email, imageURL)
            dao.createUser(user)
        } else if (user.name != name || user.email != email || user.imageURL != imageURL) {
            dao.updateUser(user)
        }
        call.newSession(user, token)
        call.respond(LoginResponse(RespondUser(user.name, user.imageURL, user.isAdmin)))
    } catch (e: Exception) {
        call.respond(LoginResponse(error = e.message))
    }
}

fun Route.logout() = get<Logout> {
    val session = call.sessions.get<UserSession>()
    if (session != null) {
        dao.closeSession(session.sid)
        call.sessions.clear<UserSession>()
    }
    call.respond { HttpStatusCode.OK }
}

fun Route.userGet() = get<UserGet> {
    try {
        val user = call.user()
        call.respond(LoginResponse(RespondUser(user.name, user.imageURL, user.isAdmin)))
    } catch (e: Exception) {
        call.respond(LoginResponse(error = e.message))
    }
}

fun Route.usersGet() = get<UsersGet> {
    try {
        val user = call.user()
        if (!user.isAdmin) throw Exception("Нет доступа")
        call.respond(dao.users())
    } catch (e: Exception) {
        call.respond(LoginResponse(error = e.message))
    }
}

