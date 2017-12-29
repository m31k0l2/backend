package backend.routes

import backend.*
import backend.model.Note
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.locations.get
import org.jetbrains.ktor.locations.post
import org.jetbrains.ktor.request.PartData
import org.jetbrains.ktor.request.receive
import org.jetbrains.ktor.request.receiveMultipart
import org.jetbrains.ktor.response.respond
import org.jetbrains.ktor.routing.Route
import org.jetbrains.ktor.util.ValuesMap

fun ApplicationCall.notes(): List<Note> {
    val notes = dao.notes()
    val user = userOrNull()
    if (user == null || !user.isAdmin) {
        return notes.filter { it.published }
    }
    return notes
}

fun Route.noteCreate() = post<NoteCreate> {
    try {
        val user = call.user()
        if (!user.isAdmin) throw Exception("Пользователь не админ")
        val multipart = call.receiveMultipart()
        val post = multipart.parts.mapNotNull {
            if (it is PartData.FormItem) {
                it.partName!! to it.value
            } else null
        }.toList().toMap()
        val ref = post["ref"]!!
        val title = post["title"]!!
        val snippet = post["snippet"]!!
        val text = post["text"]!!
        dao.createNote(Note(title = title, snippet = snippet, content = text, ref = ref))
        val note = dao.note(ref)!!
        call.respond(NoteResponse(note))
    } catch (e: Exception) {
        call.respond(NoteResponse(error = e.message))
    }
}

fun Route.noteUpdate() = post<NoteUpdate> {
    try {
        val user = call.user()
        if (!user.isAdmin) throw Exception("Пользователь не админ")
        val multipart = call.receiveMultipart()
        val post = multipart.parts.mapNotNull {
            if (it is PartData.FormItem) {
                it.partName!! to it.value
            } else null
        }.toList().toMap()
        val id = post["id"]!!.toInt()
        val ref = post["ref"]!!
        val title = post["title"]!!
        val snippet = post["snippet"]!!
        val text = post["content"]!!
        val published = post["published"]!!.toBoolean()
        val parentId = post["parentId"]!!.toInt()
        val note = Note(id, title, snippet, text, ref, published, parentId)
        dao.updateNote(note)
        call.respond(NoteResponse(note))
    } catch (e: Exception) {
        call.respond(NoteResponse(error = e.message))
    }
}

fun Route.noteDelete() = get<NoteDelete> {
    try {
        val user = call.user()
        if (!user.isAdmin) throw Exception("Пользователь не админ")
        dao.deleteNote(it.id)
        dao.comments(it.id).forEach {
            dao.deleteComment(it.id!!)
        }
        call.respond(NoteResponse())
    } catch (e: Exception) {
        call.respond(NoteResponse(error = e.message))
    }
}

fun Route.notesGet() = get<NotesGet> {
    call.respond(call.notes())
}