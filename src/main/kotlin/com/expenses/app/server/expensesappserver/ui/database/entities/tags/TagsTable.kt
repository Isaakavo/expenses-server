package com.expenses.app.server.expensesappserver.ui.database.entities.tags

import com.expenses.app.server.expensesappserver.utils.formatDate
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

data class Tags(
    val id: UUID? = null,
    val tagName: String,
    val dateAdded: LocalDateTime
)

data class TagsResponse(
    val id: UUID? = null,
    val tagName: String,
    val dateAdded: String
)

object TagsTable: UUIDTable("tag", "uuid") {
    val tagName: Column<String> = varchar("tag_name", 25).uniqueIndex()
    val dateAdded: Column<LocalDateTime> = datetime("date_added")
}

class TagEntity(
    id: EntityID<UUID>
): UUIDEntity(id) {
    companion object: UUIDEntityClass<TagEntity>(TagsTable)

    var tagName by TagsTable.tagName
    var dateAdded by TagsTable.dateAdded

    fun toTags() = TagsResponse(
        id.value,
        tagName,
        formatDate(dateAdded)
    )
}