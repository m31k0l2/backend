package backend.dao

import backend.dao.tables.*
import backend.model.*
import org.jetbrains.ktor.config.ApplicationConfig
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.connection.transaction
import org.jetbrains.squash.dialects.h2.H2Connection
import org.jetbrains.squash.dialects.postgres.PgConnection
import org.jetbrains.squash.expressions.eq
import org.jetbrains.squash.query.from
import org.jetbrains.squash.query.orderBy
import org.jetbrains.squash.query.select
import org.jetbrains.squash.query.where
import org.jetbrains.squash.results.get
import org.jetbrains.squash.statements.*
import java.io.Closeable
import java.io.File

interface DAOFacade : Closeable {
    fun init(config: ApplicationConfig)
    fun deleteNote(id: Int)
    fun deleteUser(userId: String)
    fun user(id: String, passwordHash: String?=null): User?
    fun users(): List<RespondUser>
    fun createUser(user: User)
    fun updateUser(user: User)
    fun notes(): List<Note>
    fun note(ref: String): Note?
    fun noteById(id: Int): Note?
    fun createNote(note: Note)
    fun updateNote(note: Note)
    fun usersCount(): Int
    fun notesCount(): Int
    fun createComment(comment: Comment)
    fun comments(articleId: Int?=null): List<Comment>
    fun comments(userId: String): List<Comment>
    fun deleteComment(id: Int)
    fun lastComment(): Comment?
    fun newSession(newSid: String, newUserId: String)
    fun userIdBySid(sid: String): String?
    fun closeSession(sid: String)
}

class DAOFacadeDatabase : DAOFacade {
    private lateinit var db: DatabaseConnection

    override fun init(config: ApplicationConfig) = with(config) {
        val db = when (property("driver").getString()) {
            "h2" -> h2Connect(property("dir").getString())
            "pg" -> pgConnect(
                    property("host").getString(),
                    property("user").getString(),
                    property("password").getString())
            "heroku" -> herokuConnect()
            else -> memConnect()
        }
        build(db)
    }

    fun build(db: DatabaseConnection) {
        this.db = db
        createSchema()
    }

    fun h2Connect(dir: String) = H2Connection.create("jdbc:h2:file:${File(dir).canonicalFile.absolutePath}")
    fun pgConnect(host: String, user: String, password: String) = PgConnection.create(host, user, password)
    fun herokuConnect() = PgConnection.create(System.getenv("JDBC_DATABASE_URL"))
    fun memConnect() = H2Connection.createMemoryConnection()
    fun createSchema() = db.transaction { databaseSchema().create(listOf(Users, Notes, Comments, Sessions)) }

    override fun newSession(newSid: String, newUserId: String) = db.transaction {
        deleteFrom(Sessions).where { Sessions.id eq newUserId }.execute()
        insertInto(Sessions).values {
            it[sid] = newSid
            it[id] = newUserId
        }.execute()
    }

    override fun closeSession(sid: String) = db.transaction {
        deleteFrom(Sessions).where { Sessions.sid eq sid }.execute()
    }

    override fun userIdBySid(sid: String) = db.transaction {
        select().from(Sessions).execute().map {
            val id = it[Sessions.id]
            val ses_id = it[Sessions.sid]
            "$id $ses_id"
        }.forEach {
            println(it)
        }
        select().from(Sessions).where { Sessions.sid eq sid }.execute().singleOrNull()?.let { it[Sessions.id] }
    }

    override fun user(id: String, passwordHash: String?) = db.transaction {
        select().from(Users).execute().map {
            "${it[Users.id]}, ${it[Users.name]}, ${it[Users.email]}, ${it[Users.imageURL]}, ${it[Users.passwordHash]}, ${it[Users.isAdmin]}"
        }.forEach {
            println(it)
        }
        var query = select().from(Users).where { Users.id eq id }
        if (passwordHash != null) {
            query = query.where(Users.passwordHash eq passwordHash)
        }
        query.execute().singleOrNull()?.let {
            User(   id,
                    it[Users.name],
                    it[Users.email],
                    it[Users.imageURL],
                    it[Users.passwordHash],
                    it[Users.isAdmin]
            )
        }
    }

    override fun createUser(user: User) = db.transaction {
        insertInto(Users).values {
            it[id] = user.id
            it[name] = user.name
            it[email] = user.email
            it[imageURL] = user.imageURL
            it[passwordHash] = user.passwordHash
            it[isAdmin] = usersCount() == 0
        }.execute()
    }

    override fun updateUser(user: User) = db.transaction {
        update(Notes).where { Users.id eq user.id }.set {
            it[Users.name] = user.name
            it[Users.email] = user.email
            it[Users.imageURL] = user.imageURL
            it[Users.passwordHash] = user.passwordHash
            it[Users.isAdmin] = false
        }.execute()
    }

    override fun deleteUser(userId: String) = db.transaction {
        deleteFrom(Users).where { Users.id eq userId }.execute()
    }

    override fun usersCount() = db.transaction {
        select().from(Users).execute().count()
    }

    override fun users() = db.transaction {
        select().from(Users).execute().map {
            RespondUser(it[Users.name], it[Users.imageURL], it[Users.isAdmin])
        }.toList().filter { !it.isAdmin!! }
    }

    override fun notes() = db.transaction {
        select().from(Notes).execute().map {
            Note(it[Notes.id], it[Notes.title], it[Notes.snippet], it[Notes.content], it[Notes.ref], it[Notes.published], it[Notes.parentId], it[Notes.date])
        }.toList()
    }

    override fun note(ref: String) = db.transaction {
        select().from(Notes).where { Notes.ref eq ref }.execute().map {
            Note(it[Notes.id], it[Notes.title], it[Notes.snippet], it[Notes.content], it[Notes.ref], it[Notes.published], it[Notes.parentId], it[Notes.date])
        }.singleOrNull()
    }

    override fun noteById(id: Int) = db.transaction {
        select().from(Notes).where { Notes.id eq id }.execute().map {
            Note(it[Notes.id], it[Notes.title], it[Notes.snippet], it[Notes.content], it[Notes.ref], it[Notes.published], it[Notes.parentId], it[Notes.date])
        }.singleOrNull()
    }

    override fun notesCount() = db.transaction {
        select().from(Notes).execute().count()
    }

    override fun createNote(note: Note) = db.transaction {
        insertInto(Notes).values {
            it[title] = note.title
            it[snippet] = note.snippet
            it[content] = note.content
            it[ref] = note.ref
            it[published] = false
            it[parentId] = 0
            it[date] = note.date
        }.execute()
    }

    override fun updateNote(note: Note) = db.transaction {
        update(Notes).where { Notes.id eq note.id!! }.set {
            it[Notes.title] = note.title
            it[Notes.snippet] = note.snippet
            it[Notes.content] = note.content
            it[Notes.ref] = note.ref
            it[Notes.parentId] = note.parentId
            it[Notes.published] = note.published
        }.execute()
    }

    override fun deleteNote(id: Int) = db.transaction {
        deleteFrom(Notes).where { Notes.id eq id }.execute()
    }

    override fun createComment(comment: Comment) = db.transaction {
        insertInto(Comments).values {
            it[articleId] = comment.articleId
            it[authorId] = comment.authorId
            it[text] = comment.text
            it[date] = comment.date
        }.execute()
    }

    override fun comments(articleId: Int?) = db.transaction {
        var query = select().from(Comments)
        articleId?.let { query = query.where { Comments.articleId eq it } }
        query.execute().map {
            Comment(it[Comments.id], it[Comments.articleId], it[Comments.authorId], it[Comments.text], it[Comments.date])
        }.toList()
    }

    override fun comments(userId: String) = db.transaction {
        select().from(Comments).where { Comments.authorId eq userId }.execute().map {
            Comment(it[Comments.id], it[Comments.articleId], it[Comments.authorId], it[Comments.text], it[Comments.date])
        }.toList()
    }

    override fun deleteComment(id: Int) = db.transaction {
        deleteFrom(Comments).where { Comments.id eq id }.execute()
    }

    override fun lastComment() = db.transaction {
        select().from(Comments).orderBy { Comments.id }.execute().lastOrNull()?.let {
            Comment(it[Comments.id], it[Comments.articleId], it[Comments.authorId], it[Comments.text], it[Comments.date])
        }
    }

    override fun close() {
    }
}