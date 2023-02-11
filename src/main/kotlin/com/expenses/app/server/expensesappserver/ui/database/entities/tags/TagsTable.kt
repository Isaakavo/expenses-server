package com.expenses.app.server.expensesappserver.ui.database.entities.tags

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

data class Tags(
    val id: Int? = null,
    val tagName: String,
    val dateAdded: LocalDateTime
)

object TagsTable: IntIdTable("tag") {
    val tagName: Column<String> = varchar("tag_name", 25).uniqueIndex()
    val dateAdded: Column<LocalDateTime> = datetime("date_added")
}

class TagEntity(
    id: EntityID<Int>
): IntEntity(id) {
    companion object: IntEntityClass<TagEntity>(TagsTable)

    var tagName by TagsTable.tagName
    var dateAdded by TagsTable.dateAdded

    fun toTags() = Tags(
        id.value,
        tagName,
        dateAdded
    )
}