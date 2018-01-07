package backend.model

import java.time.LocalDateTime

data class Note(var id: Int? = null, var title: String, var snippet: String, var content: String, var ref: String,
                var published: Boolean=false, var parentId: Int=0, val date: LocalDateTime = LocalDateTime.now())

data class User(
        val id: String,
        val name: String,
        val email: String? = null,
        val imageURL: String? = null,
        val passwordHash: String? = null,
        val isAdmin: Boolean = false
)

data class Header(val id: Int, val title: String, val ref: String, val parentId: Int, val published: Boolean)

data class Comment(val id: Int? = null, val articleId: Int, val authorId: String, val text: String, val date: LocalDateTime)

data class RespondUser(
        val name: String? = null,
        val imageUrl: String? = null,
        val isAdmin: Boolean? = null
)

data class RespondComment(val id: Int, val articleId: Int, val author: String, val avatar: String?, val text: String, val date: LocalDateTime)