package backend.dao.tables

import org.jetbrains.squash.definition.TableDefinition
import org.jetbrains.squash.definition.primaryKey
import org.jetbrains.squash.definition.uniqueIndex
import org.jetbrains.squash.definition.varchar

object Sessions : TableDefinition() {
    val sid = varchar("sid", 128).uniqueIndex().primaryKey()
    val login = varchar("name", 128)
    val hash = varchar("hash", 128)
}