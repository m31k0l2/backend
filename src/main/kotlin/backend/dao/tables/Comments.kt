package backend.dao.tables

import org.jetbrains.squash.definition.*

object Comments : TableDefinition() {
    val id = integer("id").autoIncrement().primaryKey()
    val articleId = integer("articleId")
    val author = varchar("user_id", 20).index()
    val text = varchar("text", 1024)
    val date = datetime("date")
}