package com.example

import com.example.db.DatabaseFactory
import com.example.db.UserRepository
import com.example.fhir.FhirService
import com.example.routes.authRoutes
import com.example.routes.patientRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.Principal
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.plugins.swagger.*
import kotlinx.serialization.json.Json

data class UserSession(val email: String) : Principal

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)
    val userRepository = UserRepository()
    val fhirService = FhirService(environment.config)

    install(CallLogging)
    install(ContentNegotiation) {
        json(Json { prettyPrint = true; isLenient = true })
    }
    install(Sessions) {
        cookie<UserSession>("USER_SESSION") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60 * 60 * 24
            cookie.httpOnly = true
        }
    }

    routing {
        // Serve static OpenAPI YAML from resources/openapi
        staticResources("/openapi", "openapi")

        // Serve static OpenAPI/Swagger docs
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        authRoutes(userRepository)
        patientRoutes(userRepository, fhirService)
    }
}

