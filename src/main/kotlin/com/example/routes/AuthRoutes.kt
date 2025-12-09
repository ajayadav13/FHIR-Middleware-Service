package com.example.routes

import com.example.UserSession
import com.example.db.UserRepository
import com.example.security.PasswordHasher
import com.example.templates.authLayout
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

fun Route.authRoutes(userRepository: UserRepository) {
    // Home route - redirect based on auth status
    get("/") {
        val session = call.sessions.get<UserSession>()
        if (session != null) {
            call.respondRedirect("/dashboard")
        } else {
            call.respondRedirect("/login")
        }
    }

    get("/register") {
        val session = call.sessions.get<UserSession>()
        if (session != null) {
            call.respondRedirect("/dashboard")
            return@get
        }
        
        call.respondHtml {
            authLayout("Register - FHIR Middleware") {
                div(classes = "card") {
                    div(classes = "card-header") {
                        h1 { +"Create Account" }
                        p { +"Join FHIR Middleware to manage patient data" }
                    }
                    form(action = "/register", method = FormMethod.post) {
                        div(classes = "form-group") {
                            label { +"Email Address" }
                            input(type = InputType.email, name = "email") {
                                placeholder = "you@example.com"
                                required = true
                                autoFocus = true
                            }
                        }
                        div(classes = "form-group") {
                            label { +"Password" }
                            input(type = InputType.password, name = "password") {
                                placeholder = "Enter a secure password"
                                required = true
                                minLength = "6"
                            }
                        }
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Create Account"
                        }
                    }
                    div(classes = "form-footer") {
                        +"Already have an account? "
                        a(href = "/login") { +"Sign in" }
                    }
                }
            }
        }
    }

    post("/register") {
        val params = call.receiveParameters()
        val email = params["email"]?.trim()
        val password = params["password"]

        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            call.respondHtml(HttpStatusCode.BadRequest) {
                authLayout("Register - FHIR Middleware") {
                    div(classes = "card") {
                        div(classes = "alert alert-error") {
                            +"Email and password are required"
                        }
                        div(classes = "card-header") {
                            h1 { +"Create Account" }
                            p { +"Join FHIR Middleware to manage patient data" }
                        }
                        form(action = "/register", method = FormMethod.post) {
                            div(classes = "form-group") {
                                label { +"Email Address" }
                                input(type = InputType.email, name = "email") {
                                    placeholder = "you@example.com"
                                    required = true
                                    value = email ?: ""
                                }
                            }
                            div(classes = "form-group") {
                                label { +"Password" }
                                input(type = InputType.password, name = "password") {
                                    placeholder = "Enter a secure password"
                                    required = true
                                    minLength = "6"
                                }
                            }
                            button(type = ButtonType.submit, classes = "btn btn-primary") {
                                +"Create Account"
                            }
                        }
                        div(classes = "form-footer") {
                            +"Already have an account? "
                            a(href = "/login") { +"Sign in" }
                        }
                    }
                }
            }
            return@post
        }

        val created = userRepository.createUser(email, PasswordHasher.hash(password))
        if (!created) {
            call.respondHtml {
                authLayout("Register - FHIR Middleware") {
                    div(classes = "card") {
                        div(classes = "alert alert-error") {
                            +"An account with this email already exists"
                        }
                        div(classes = "card-header") {
                            h1 { +"Create Account" }
                            p { +"Join FHIR Middleware to manage patient data" }
                        }
                        form(action = "/register", method = FormMethod.post) {
                            div(classes = "form-group") {
                                label { +"Email Address" }
                                input(type = InputType.email, name = "email") {
                                    placeholder = "you@example.com"
                                    required = true
                                }
                            }
                            div(classes = "form-group") {
                                label { +"Password" }
                                input(type = InputType.password, name = "password") {
                                    placeholder = "Enter a secure password"
                                    required = true
                                    minLength = "6"
                                }
                            }
                            button(type = ButtonType.submit, classes = "btn btn-primary") {
                                +"Create Account"
                            }
                        }
                        div(classes = "form-footer") {
                            +"Already have an account? "
                            a(href = "/login") { +"Sign in" }
                        }
                    }
                }
            }
        } else {
            call.respondRedirect("/login?registered=true")
        }
    }

    get("/login") {
        val session = call.sessions.get<UserSession>()
        if (session != null) {
            call.respondRedirect("/dashboard")
            return@get
        }
        
        val showRegistered = call.request.queryParameters["registered"] == "true"
        
        call.respondHtml {
            authLayout("Login - FHIR Middleware") {
                div(classes = "card") {
                    if (showRegistered) {
                        div(classes = "alert alert-success") {
                            +"Account created successfully! Please sign in."
                        }
                    }
                    div(classes = "card-header") {
                        h1 { +"Welcome Back" }
                        p { +"Sign in to your FHIR Middleware account" }
                    }
                    form(action = "/login", method = FormMethod.post) {
                        div(classes = "form-group") {
                            label { +"Email Address" }
                            input(type = InputType.email, name = "email") {
                                placeholder = "you@example.com"
                                required = true
                                autoFocus = true
                            }
                        }
                        div(classes = "form-group") {
                            label { +"Password" }
                            input(type = InputType.password, name = "password") {
                                placeholder = "Enter your password"
                                required = true
                            }
                        }
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Sign In"
                        }
                    }
                    div(classes = "form-footer") {
                        +"Don't have an account? "
                        a(href = "/register") { +"Create one" }
                    }
                }
            }
        }
    }

    post("/login") {
        val params = call.receiveParameters()
        val email = params["email"]?.trim()
        val password = params["password"]

        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            call.respondHtml(HttpStatusCode.BadRequest) {
                authLayout("Login - FHIR Middleware") {
                    div(classes = "card") {
                        div(classes = "alert alert-error") {
                            +"Email and password are required"
                        }
                        div(classes = "card-header") {
                            h1 { +"Welcome Back" }
                            p { +"Sign in to your FHIR Middleware account" }
                        }
                        form(action = "/login", method = FormMethod.post) {
                            div(classes = "form-group") {
                                label { +"Email Address" }
                                input(type = InputType.email, name = "email") {
                                    placeholder = "you@example.com"
                                    required = true
                                    value = email ?: ""
                                }
                            }
                            div(classes = "form-group") {
                                label { +"Password" }
                                input(type = InputType.password, name = "password") {
                                    placeholder = "Enter your password"
                                    required = true
                                }
                            }
                            button(type = ButtonType.submit, classes = "btn btn-primary") {
                                +"Sign In"
                            }
                        }
                        div(classes = "form-footer") {
                            +"Don't have an account? "
                            a(href = "/register") { +"Create one" }
                        }
                    }
                }
            }
            return@post
        }

        val user = userRepository.findByEmail(email)
        if (user == null || !PasswordHasher.verify(password, user.passwordHash)) {
            call.respondHtml(HttpStatusCode.Unauthorized) {
                authLayout("Login - FHIR Middleware") {
                    div(classes = "card") {
                        div(classes = "alert alert-error") {
                            +"Invalid email or password"
                        }
                        div(classes = "card-header") {
                            h1 { +"Welcome Back" }
                            p { +"Sign in to your FHIR Middleware account" }
                        }
                        form(action = "/login", method = FormMethod.post) {
                            div(classes = "form-group") {
                                label { +"Email Address" }
                                input(type = InputType.email, name = "email") {
                                    placeholder = "you@example.com"
                                    required = true
                                    value = email
                                }
                            }
                            div(classes = "form-group") {
                                label { +"Password" }
                                input(type = InputType.password, name = "password") {
                                    placeholder = "Enter your password"
                                    required = true
                                }
                            }
                            button(type = ButtonType.submit, classes = "btn btn-primary") {
                                +"Sign In"
                            }
                        }
                        div(classes = "form-footer") {
                            +"Don't have an account? "
                            a(href = "/register") { +"Create one" }
                        }
                    }
                }
            }
            return@post
        }

        userRepository.updateLastAccessed(email)
        call.sessions.set(UserSession(email))
        call.respondRedirect("/dashboard")
    }

    get("/logout") {
        call.sessions.clear<UserSession>()
        call.respondRedirect("/login")
    }
}
