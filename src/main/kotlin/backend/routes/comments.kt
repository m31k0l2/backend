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
        dao.createComment(Comment(articleId = articleId, author = call.user().name, text = text, date = LocalDateTime.now()))
        val comment = dao.lastComment()!!
//        val note = dao.noteById(comment.articleId)
//        if (note == null || !note.published) throw Exception("Комментарий к статье не возможен")
        call.respond(CommentResponse(RespondComment(comment.id!!, comment.articleId, comment.author, comment.text, comment.date)))
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
    call.respond(dao.comments(it.articleId))
}

fun Route.commentsGetByUser() = get<CommentsGetByUser> {
    call.respond(dao.comments(it.name))
}