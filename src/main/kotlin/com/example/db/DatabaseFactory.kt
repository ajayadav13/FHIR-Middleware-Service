package com.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val dbConfig = config.config("database")
        val url = dbConfig.propertyOrNull("url")?.getString()
            ?: dbConfig.propertyOrNull("defaultUrl")?.getString()
            ?: error("Database URL not configured")
        val user = dbConfig.propertyOrNull("user")?.getString()
            ?: dbConfig.propertyOrNull("defaultUser")?.getString()
            ?: "ktor"
        val password = dbConfig.propertyOrNull("password")?.getString()
            ?: dbConfig.propertyOrNull("defaultPassword")?.getString()
            ?: "ktorpass"
        val maxPool = dbConfig.propertyOrNull("maximumPoolSize")?.getString()?.toIntOrNull()
            ?: dbConfig.propertyOrNull("defaultPoolSize")?.getString()?.toIntOrNull()
            ?: 5

        val dataSource = HikariDataSource(
            HikariConfig().apply {
                driverClassName = dbConfig.propertyOrNull("driver")?.getString() ?: "org.postgresql.Driver"
                jdbcUrl = url
                username = user
                this.password = password
                maximumPoolSize = maxPool
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            }
        )

        Database.connect(dataSource)
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

        transaction {
            SchemaUtils.createMissingTablesAndColumns(Users)
        }
    }

    fun <T> dbQuery(block: Transaction.() -> T): T =
        transaction { block() }
}

