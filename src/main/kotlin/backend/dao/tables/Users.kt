package backend.dao.tables

import org.jetbrains.squash.definition.*

object Users : TableDefinition() {
    val id = varchar("id", 128).uniqueIndex().primaryKey()
    val name = varchar("name", 128)
    val email = varchar("email", 128).nullable()
    val imageURL = varchar("imageURL", 1024).nullable()
    val passwordHash = varchar("password_hash", 128).nullable()
    val isAdmin = bool("is_admin")
}