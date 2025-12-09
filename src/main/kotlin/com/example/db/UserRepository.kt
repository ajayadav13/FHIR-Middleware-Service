package com.example.db

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class User(
    val email: String,
    val passwordHash: String,
    val resourcesCreated: Int,
    val lastAccessed: LocalDateTime
)

class UserRepository {
    suspend fun createUser(email: String, passwordHash: String): Boolean =
        DatabaseFactory.dbQuery {
            val existing = Users.select { Users.email eq email }.count() > 0
            if (existing) {
                false
            } else {
                Users.insert {
                    it[Users.email] = email
                    it[Users.password] = passwordHash
                    it[resourcesCreated] = 0
                    it[lastAccessed] = LocalDateTime.now()
                }
                true
            }
        }

    suspend fun findByEmail(email: String): User? =
        DatabaseFactory.dbQuery {
            Users.select { Users.email eq email }
                .limit(1)
                .firstOrNull()
                ?.toUser()
        }

    suspend fun updateLastAccessed(email: String) =
        DatabaseFactory.dbQuery {
            Users.update({ Users.email eq email }) {
                it[lastAccessed] = LocalDateTime.now()
            }
        }

    suspend fun incrementResources(email: String) =
        DatabaseFactory.dbQuery {
            Users.update({ Users.email eq email }) {
                with(SqlExpressionBuilder) {
                    it.update(resourcesCreated, resourcesCreated + 1)
                }
                it[lastAccessed] = LocalDateTime.now()
            }
        }

    private fun ResultRow.toUser(): User =
        User(
            email = this[Users.email],
            passwordHash = this[Users.password],
            resourcesCreated = this[Users.resourcesCreated],
            lastAccessed = this[Users.lastAccessed]
        )
}

