package backend.dao.tables

import org.jetbrains.squash.definition.*

object Users : TableDefinition() {
    val name = varchar("name", 128).uniqueIndex().primaryKey()
    val passwordHash = varchar("password_hash", 128)
    val isAdmin = bool("is_admin")
}