package backend.dao.tables

import org.jetbrains.squash.definition.*

object Comments : TableDefinition() {
    val id = integer("id").autoIncrement().primaryKey()
    val articleId = integer("articleId")
    val authorId = varchar("user_id", 128).index()
    val text = varchar("text", 1024)
    val date = datetime("date")
}