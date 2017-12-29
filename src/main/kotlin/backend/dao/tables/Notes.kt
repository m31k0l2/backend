package backend.dao.tables

import org.jetbrains.squash.definition.*

object Notes : TableDefinition() {
    val id = integer("id").autoIncrement().primaryKey()
    val title = varchar("title", 128)
    val snippet = varchar("snippet", 256)
    val content = varchar("content", 1024)
    val ref = varchar("ref", 64).uniqueIndex()
    val published = bool("published")
    val parentId = integer("parentId")
    val date = datetime("date")
}