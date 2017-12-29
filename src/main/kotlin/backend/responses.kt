package backend

import backend.model.Note
import backend.model.RespondComment
import backend.model.RespondUser

interface RpcData

data class LoginResponse(val user: RespondUser? = null, val error: String? = null) : RpcData
data class NoteResponse(val note: Note? = null, val error: String? = null) : RpcData
data class CommentResponse(val comment: RespondComment? = null, val error: String? = null) : RpcData