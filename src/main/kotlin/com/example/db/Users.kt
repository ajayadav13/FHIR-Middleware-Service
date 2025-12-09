package com.example.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Users : Table("users") {
    val email = varchar("email", length = 255)
    val password = varchar("password", length = 255)
    val resourcesCreated = integer("resources_created").default(0)
    val lastAccessed = datetime("last_accessed")

    override val primaryKey = PrimaryKey(email)
}

