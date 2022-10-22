package com.expenses.app.server.expensesappserver.ui.database.connection

import org.jetbrains.exposed.sql.Database

object DatabaseConnectionSettings {
    val db by lazy {
        Database.connect("jdbc:postgresql://localhost:5432/compose-postgres", driver = "org.postgresql.Driver",
            user = "compose-postgres", password = "compose-postgres")
    }

}