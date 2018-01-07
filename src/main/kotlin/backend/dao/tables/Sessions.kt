package backend.dao.tables

import org.jetbrains.squash.definition.*

object Sessions : TableDefinition() {
    val sid = varchar("sid", 128).uniqueIndex().primaryKey()
    val id = varchar("id", 128)
}