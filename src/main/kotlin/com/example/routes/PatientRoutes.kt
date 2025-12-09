package com.example.routes

import com.example.UserSession
import com.example.db.UserRepository
import com.example.fhir.FhirService
import com.example.templates.layout
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import java.time.format.DateTimeFormatter

fun Route.patientRoutes(userRepository: UserRepository, fhirService: FhirService) {
    
    // Dashboard - shows user stats and quick actions
    get("/dashboard") {
        val session = call.sessions.get<UserSession>()
        if (session == null) {
            call.respondRedirect("/login")
            return@get
        }
        
        val user = userRepository.findByEmail(session.email)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")
        
        call.respondHtml {
            layout("Dashboard - FHIR Middleware", showNav = true, userEmail = session.email) {
                div(classes = "page-header") {
                    h1 { +"Dashboard" }
                    p { +"Welcome back! Manage your FHIR patient resources." }
                }
                
                // Stats Grid
                div(classes = "stat-grid") {
                    div(classes = "stat-card") {
                        h3 { +"Resources Created" }
                        div(classes = "value") { +"${user?.resourcesCreated ?: 0}" }
                    }
                    div(classes = "stat-card") {
                        h3 { +"Last Activity" }
                        div(classes = "value") {
                            style = "font-size: 1rem; color: var(--text-secondary);"
                            +(user?.lastAccessed?.format(formatter) ?: "N/A")
                        }
                    }
                    div(classes = "stat-card") {
                        h3 { +"FHIR Server" }
                        div(classes = "value") {
                            style = "font-size: 0.875rem; color: var(--accent-primary);"
                            +"hapi.fhir.org"
                            span(classes = "fhir-badge") { +"R4" }
                        }
                    }
                }
                
                // Quick Actions
                div(classes = "patient-card") {
                    h2 { +"Quick Actions" }
                    div(classes = "divider")
                    div {
                        style = "display: flex; gap: 1rem; flex-wrap: wrap;"
                        a(href = "/patient/new", classes = "btn btn-primary") {
                            style = "width: auto;"
                            +"+ Create New Patient"
                        }
                        a(href = "/patient/search", classes = "btn btn-secondary") {
                            +"Search Patient by ID"
                        }
                        a(href = "/swagger", classes = "btn btn-secondary", target = "_blank") {
                            rel = "noopener noreferrer"
                            +"View API Documentation"
                        }
                    }
                }
                
                // Info Card
                div(classes = "patient-card") {
                    h2 { +"About FHIR Middleware" }
                    div(classes = "divider")
                    p {
                        style = "color: var(--text-secondary); margin-bottom: 1rem;"
                        +"""This application serves as a secure middleware for FHIR healthcare data. 
                            It manages user authentication locally while delegating clinical data 
                            storage to the public HAPI FHIR R4 server."""
                    }
                    p {
                        style = "color: var(--text-muted); font-size: 0.875rem;"
                        +"Connected to: "
                        a(href = "https://hapi.fhir.org/baseR4", target = "_blank") {
                            style = "color: var(--accent-primary);"
                            +"https://hapi.fhir.org/baseR4"
                        }
                    }
                }
            }
        }
    }
    
    // Search Patient by ID form
    get("/patient/search") {
        val session = call.sessions.get<UserSession>()
        if (session == null) {
            call.respondRedirect("/login")
            return@get
        }
        
        call.respondHtml {
            layout("Search Patient - FHIR Middleware", showNav = true, userEmail = session.email) {
                div(classes = "auth-container") {
                    style = "min-height: calc(100vh - 200px);"
                    div(classes = "card") {
                        div(classes = "card-header") {
                            h1 { +"Search Patient" }
                            p { +"Enter a FHIR Patient ID to view details" }
                        }
                        form(action = "/patient/search", method = FormMethod.post) {
                            div(classes = "form-group") {
                                label { +"Patient ID" }
                                input(type = InputType.text, name = "patientId") {
                                    placeholder = "e.g., 12345"
                                    required = true
                                    autoFocus = true
                                }
                            }
                            button(type = ButtonType.submit, classes = "btn btn-primary") {
                                +"Search"
                            }
                        }
                        div(classes = "form-footer") {
                            a(href = "/patient/new") { +"Create a new patient instead" }
                        }
                    }
                }
            }
        }
    }
    
    post("/patient/search") {
        val session = call.sessions.get<UserSession>()
        if (session == null) {
            call.respondRedirect("/login")
            return@post
        }
        
        val params = call.receiveParameters()
        val patientId = params["patientId"]?.trim()
        
        if (patientId.isNullOrBlank()) {
            call.respondRedirect("/patient/search")
            return@post
        }
        
        call.respondRedirect("/patient/$patientId")
    }
    
    // Create new patient form
    get("/patient/new") {
        val session = call.sessions.get<UserSession>()
        if (session == null) {
            call.respondRedirect("/login")
            return@get
        }

        call.respondHtml {
            layout("Create Patient - FHIR Middleware", showNav = true, userEmail = session.email) {
                div(classes = "auth-container") {
                    style = "min-height: calc(100vh - 200px);"
                    div(classes = "card card-lg") {
                        div(classes = "card-header") {
                            h1 { 
                                +"Create Patient"
                                span(classes = "fhir-badge") { +"FHIR R4" }
                            }
                            p { +"Add a new patient to the HAPI FHIR server" }
                        }
                        form(action = "/patient/new", method = FormMethod.post) {
                            div {
                                style = "display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;"
                                div(classes = "form-group") {
                                    label { +"First Name" }
                                    input(type = InputType.text, name = "firstName") {
                                        placeholder = "John"
                                        required = true
                                        autoFocus = true
                                    }
                                }
                                div(classes = "form-group") {
                                    label { +"Last Name" }
                                    input(type = InputType.text, name = "lastName") {
                                        placeholder = "Doe"
                                        required = true
                                    }
                                }
                            }
                            div(classes = "form-group") {
                                label { +"Gender" }
                                select {
                                    name = "gender"
                                    option { 
                                        value = ""
                                        +"Select gender" 
                                    }
                                    option { 
                                        value = "male"
                                        +"Male" 
                                    }
                                    option { 
                                        value = "female"
                                        +"Female" 
                                    }
                                    option { 
                                        value = "other"
                                        +"Other" 
                                    }
                                    option { 
                                        value = "unknown"
                                        +"Unknown" 
                                    }
                                }
                            }
                            button(type = ButtonType.submit, classes = "btn btn-primary") {
                                +"Create Patient"
                            }
                        }
                        div(classes = "form-footer") {
                            +"Patient will be created on "
                            a(href = "https://hapi.fhir.org/baseR4", target = "_blank") {
                                +"HAPI FHIR R4 Server"
                            }
                        }
                    }
                }
            }
        }
    }

    post("/patient/new") {
        val session = call.sessions.get<UserSession>()
        if (session == null) {
            call.respondRedirect("/login")
            return@post
        }

        val params = call.receiveParameters()
        val firstName = params["firstName"]?.trim()
        val lastName = params["lastName"]?.trim()
        val gender = params["gender"]?.trim().takeIf { !it.isNullOrBlank() }

        if (firstName.isNullOrBlank() || lastName.isNullOrBlank()) {
            call.respondHtml(HttpStatusCode.BadRequest) {
                layout("Create Patient - FHIR Middleware", showNav = true, userEmail = session.email) {
                    div(classes = "auth-container") {
                        style = "min-height: calc(100vh - 200px);"
                        div(classes = "card card-lg") {
                            div(classes = "alert alert-error") {
                                +"First name and last name are required"
                            }
                            div(classes = "card-header") {
                                h1 { 
                                    +"Create Patient"
                                    span(classes = "fhir-badge") { +"FHIR R4" }
                                }
                                p { +"Add a new patient to the HAPI FHIR server" }
                            }
                            form(action = "/patient/new", method = FormMethod.post) {
                                div {
                                    style = "display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;"
                                    div(classes = "form-group") {
                                        label { +"First Name" }
                                        input(type = InputType.text, name = "firstName") {
                                            placeholder = "John"
                                            required = true
                                            value = firstName ?: ""
                                        }
                                    }
                                    div(classes = "form-group") {
                                        label { +"Last Name" }
                                        input(type = InputType.text, name = "lastName") {
                                            placeholder = "Doe"
                                            required = true
                                            value = lastName ?: ""
                                        }
                                    }
                                }
                                div(classes = "form-group") {
                                    label { +"Gender" }
                                    select {
                                        name = "gender"
                                        option { value = ""; +"Select gender" }
                                        option { value = "male"; +"Male" }
                                        option { value = "female"; +"Female" }
                                        option { value = "other"; +"Other" }
                                        option { value = "unknown"; +"Unknown" }
                                    }
                                }
                                button(type = ButtonType.submit, classes = "btn btn-primary") {
                                    +"Create Patient"
                                }
                            }
                        }
                    }
                }
            }
            return@post
        }

        try {
            val outcome = fhirService.createPatient(firstName, lastName, gender)
            val createdId = outcome.id?.idPart

            if (createdId != null) {
                userRepository.incrementResources(session.email)
                call.respondHtml {
                    layout("Patient Created - FHIR Middleware", showNav = true, userEmail = session.email) {
                        div(classes = "auth-container") {
                            style = "min-height: calc(100vh - 200px);"
                            div(classes = "card card-lg") {
                                div(classes = "success-container") {
                                    div(classes = "success-icon") {
                                        unsafe {
                                            +"""<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                                            </svg>"""
                                        }
                                    }
                                    h2 { +"Patient Created Successfully!" }
                                    p { +"The patient has been added to the HAPI FHIR server" }
                                    p {
                                        strong { +"$firstName $lastName" }
                                        gender?.let {
                                            +" · ${it.replaceFirstChar { c -> c.uppercase() }}"
                                        }
                                    }
                                    div(classes = "resource-id") {
                                        +"ID: $createdId"
                                    }
                                    div(classes = "action-buttons") {
                                        a(href = "/patient/$createdId", classes = "btn btn-primary") {
                                            style = "width: auto;"
                                            +"View Patient Details"
                                        }
                                        a(href = "/patient/new", classes = "btn btn-secondary") {
                                            +"Create Another"
                                        }
                                        a(href = "/dashboard", classes = "btn btn-outline") {
                                            +"Back to Dashboard"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                call.respondHtml(HttpStatusCode.InternalServerError) {
                    layout("Error - FHIR Middleware", showNav = true, userEmail = session.email) {
                        div(classes = "auth-container") {
                            style = "min-height: calc(100vh - 200px);"
                            div(classes = "card") {
                                div(classes = "alert alert-error") {
                                    +"Failed to create patient. The FHIR server did not return a valid ID."
                                }
                                div(classes = "action-buttons") {
                                    a(href = "/patient/new", classes = "btn btn-primary") {
                                        style = "width: auto;"
                                        +"Try Again"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            call.respondHtml(HttpStatusCode.InternalServerError) {
                layout("Error - FHIR Middleware", showNav = true, userEmail = session.email) {
                    div(classes = "auth-container") {
                        style = "min-height: calc(100vh - 200px);"
                        div(classes = "card") {
                            div(classes = "alert alert-error") {
                                +"Error creating patient: ${e.message ?: "Unknown error"}"
                            }
                            div(classes = "action-buttons") {
                                a(href = "/patient/new", classes = "btn btn-primary") {
                                    style = "width: auto;"
                                    +"Try Again"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    get("/patient/{id}") {
        val session = call.sessions.get<UserSession>()
        if (session == null) {
            call.respondRedirect("/login")
            return@get
        }

        val id = call.parameters["id"]
        if (id.isNullOrBlank()) {
            call.respondRedirect("/patient/search")
            return@get
        }

        try {
            val patientJson = fhirService.fetchPatientAsJson(id)
            
            call.respondHtml {
                layout("Patient $id - FHIR Middleware", showNav = true, userEmail = session.email) {
                    div(classes = "page-header") {
                        h1 { 
                            +"Patient Details"
                            span(classes = "fhir-badge") { +"FHIR R4" }
                        }
                        p { +"Viewing patient resource from HAPI FHIR server" }
                    }
                    
                    div(classes = "patient-card") {
                        div {
                            style = "display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;"
                            h2 { 
                                style = "margin: 0;"
                                +"Resource ID: "
                                span {
                                    style = "color: var(--accent-primary);"
                                    +id
                                }
                            }
                            div {
                                style = "display: flex; gap: 0.75rem;"
                                a(href = "https://hapi.fhir.org/baseR4/Patient/$id", target = "_blank", classes = "btn btn-outline") {
                                    style = "padding: 0.5rem 1rem; font-size: 0.875rem;"
                                    +"View on HAPI Server"
                                }
                            }
                        }
                        div(classes = "divider")
                        h3 {
                            style = "color: var(--text-secondary); font-size: 0.875rem; margin-bottom: 0.75rem;"
                            +"Raw JSON Response"
                        }
                        div(classes = "json-display") {
                            +patientJson
                        }
                    }
                    
                    div(classes = "action-buttons") {
                        style = "justify-content: flex-start;"
                        a(href = "/patient/new", classes = "btn btn-primary") {
                            style = "width: auto;"
                            +"+ Create New Patient"
                        }
                        a(href = "/patient/search", classes = "btn btn-secondary") {
                            +"Search Another"
                        }
                        a(href = "/dashboard", classes = "btn btn-outline") {
                            +"Back to Dashboard"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            call.respondHtml(HttpStatusCode.NotFound) {
                layout("Patient Not Found - FHIR Middleware", showNav = true, userEmail = session.email) {
                    div(classes = "auth-container") {
                        style = "min-height: calc(100vh - 200px);"
                        div(classes = "card") {
                            div(classes = "alert alert-error") {
                                +"Patient with ID '$id' was not found on the HAPI FHIR server"
                            }
                            p {
                                style = "color: var(--text-secondary); text-align: center; margin-bottom: 1.5rem;"
                                +"The patient may have been deleted or the ID might be incorrect."
                            }
                            div(classes = "action-buttons") {
                                a(href = "/patient/search", classes = "btn btn-primary") {
                                    style = "width: auto;"
                                    +"Search Again"
                                }
                                a(href = "/patient/new", classes = "btn btn-secondary") {
                                    +"Create New Patient"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
