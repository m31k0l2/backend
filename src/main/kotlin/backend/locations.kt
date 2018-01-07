package backend

import org.jetbrains.ktor.locations.location

@location("/user/register") class Register(val name: String, val password: String)
@location("/user/login") class Login(val name: String, val password: String)
@location("/user/oauth") class OAuth
@location("/user/logout") class Logout
@location("/user/get") class UserGet
@location("/users/get") class UsersGet
@location("/user/delete") class UserDelete(val userId: String)
@location("/note/create") class NoteCreate
@location("/note/update") class NoteUpdate
@location("/note/delete") class NoteDelete(val id: Int)
@location("/notes/get") class NotesGet
@location("/ping") class Ping
@location("/comment/add") class CommentAdd
@location("/comment/delete") class CommentDelete(val id: Int)
@location("/contents/get") class ContentsGet
@location("/comments/get") class CommentsGet(val articleId: Int?=null)
@location("/comments/get") class CommentsGetByUser(val userId: String)