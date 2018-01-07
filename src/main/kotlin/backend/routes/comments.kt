package backend.routes

import backend.*
import backend.model.Comment
import backend.model.RespondComment
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.locations.get
import org.jetbrains.ktor.locations.post
import org.jetbrains.ktor.request.receive
import org.jetbrains.ktor.response.respond
import org.jetbrains.ktor.routing.Route
import org.jetbrains.ktor.util.ValuesMap
import java.time.LocalDateTime

fun Route.commentAdd() = post<CommentAdd> {
    try {
        val post = call.receive<ValuesMap>()
        val articleId = post["articleId"]!!.toInt()
        val text = post["text"]!!
        if (text.isEmpty()) throw Exception("Сообщение пустое")
        dao.createComment(Comment(articleId = articleId, authorId = call.user().id, text = text, date = LocalDateTime.now()))
        val comment = dao.lastComment()!!
        val user = dao.user(comment.authorId)!!
        call.respond(CommentResponse(RespondComment(comment.id!!, comment.articleId, user.name, user.imageURL, comment.text, comment.date)))
    } catch (e: Exception) {
        call.respond(CommentResponse(error = e.message))
    }
}

fun Route.commentDelete() = get<CommentDelete> {
    try {
        val user = call.user()
        if (!user.isAdmin) throw Exception("Пользователь не админ")
        dao.deleteComment(it.id)
        call.respond(HttpStatusCode.OK)
    } catch (e: Exception) {
        call.respond(CommentResponse(error = e.message))
    }
}

fun Route.commentsGet() = get<CommentsGet> {
    call.respond(dao.comments(it.articleId).map {
        val user = dao.user(it.authorId)!!
        RespondComment(it.id!!, it.articleId, user.name, user.imageURL, it.text, it.date)
    })
}

fun Route.commentsGetByUser() = get<CommentsGetByUser> {
    call.respond(dao.comments(it.userId))
}