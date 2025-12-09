package com.example.templates

import kotlinx.html.*

/**
 * Shared layout template for consistent, modern styling across all pages.
 * Uses a clean, medical-themed design appropriate for a FHIR healthcare app.
 */
fun HTML.layout(pageTitle: String, showNav: Boolean = false, userEmail: String? = null, content: MAIN.() -> Unit) {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
        title { +pageTitle }
        link(href = "https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap", rel = "stylesheet")
        style {
            unsafe {
                raw("""
                    :root {
                        --bg-primary: #0a0f1a;
                        --bg-secondary: #111827;
                        --bg-card: #1a2332;
                        --bg-input: #0d1219;
                        --accent-primary: #10b981;
                        --accent-secondary: #059669;
                        --accent-glow: rgba(16, 185, 129, 0.15);
                        --text-primary: #f1f5f9;
                        --text-secondary: #94a3b8;
                        --text-muted: #64748b;
                        --border-color: #1e293b;
                        --border-focus: #10b981;
                        --error: #ef4444;
                        --success: #10b981;
                        --warning: #f59e0b;
                    }
                    
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    
                    body {
                        font-family: 'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif;
                        background: var(--bg-primary);
                        color: var(--text-primary);
                        min-height: 100vh;
                        line-height: 1.6;
                    }
                    
                    /* Animated gradient background */
                    body::before {
                        content: '';
                        position: fixed;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        background: 
                            radial-gradient(ellipse at 20% 20%, rgba(16, 185, 129, 0.08) 0%, transparent 50%),
                            radial-gradient(ellipse at 80% 80%, rgba(5, 150, 105, 0.06) 0%, transparent 50%),
                            radial-gradient(ellipse at 50% 50%, rgba(16, 185, 129, 0.03) 0%, transparent 70%);
                        pointer-events: none;
                        z-index: -1;
                    }
                    
                    nav {
                        background: var(--bg-secondary);
                        border-bottom: 1px solid var(--border-color);
                        padding: 1rem 2rem;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        backdrop-filter: blur(10px);
                        position: sticky;
                        top: 0;
                        z-index: 100;
                    }
                    
                    .nav-brand {
                        display: flex;
                        align-items: center;
                        gap: 0.75rem;
                        font-weight: 700;
                        font-size: 1.25rem;
                        color: var(--text-primary);
                        text-decoration: none;
                    }
                    
                    .nav-brand svg {
                        width: 32px;
                        height: 32px;
                        color: var(--accent-primary);
                    }
                    
                    .nav-links {
                        display: flex;
                        gap: 1.5rem;
                        align-items: center;
                    }
                    
                    .nav-links a {
                        color: var(--text-secondary);
                        text-decoration: none;
                        font-weight: 500;
                        padding: 0.5rem 1rem;
                        border-radius: 8px;
                        transition: all 0.2s ease;
                    }
                    
                    .nav-links a:hover {
                        color: var(--text-primary);
                        background: var(--bg-card);
                    }
                    
                    .nav-user {
                        display: flex;
                        align-items: center;
                        gap: 1rem;
                        color: var(--text-secondary);
                        font-size: 0.875rem;
                    }
                    
                    .nav-user span {
                        padding: 0.5rem 1rem;
                        background: var(--bg-card);
                        border-radius: 20px;
                        border: 1px solid var(--border-color);
                    }
                    
                    main {
                        padding: 2rem;
                        max-width: 1200px;
                        margin: 0 auto;
                    }
                    
                    .auth-container {
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: calc(100vh - 4rem);
                        padding: 2rem;
                    }
                    
                    .card {
                        background: var(--bg-card);
                        border: 1px solid var(--border-color);
                        border-radius: 16px;
                        padding: 2.5rem;
                        width: 100%;
                        max-width: 440px;
                        box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.4);
                    }
                    
                    .card-lg {
                        max-width: 600px;
                    }
                    
                    .card-header {
                        text-align: center;
                        margin-bottom: 2rem;
                    }
                    
                    .card-header h1 {
                        font-size: 1.75rem;
                        font-weight: 700;
                        margin-bottom: 0.5rem;
                        background: linear-gradient(135deg, var(--text-primary), var(--accent-primary));
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        background-clip: text;
                    }
                    
                    .card-header p {
                        color: var(--text-secondary);
                        font-size: 0.9375rem;
                    }
                    
                    .form-group {
                        margin-bottom: 1.5rem;
                    }
                    
                    .form-group label {
                        display: block;
                        margin-bottom: 0.5rem;
                        font-weight: 500;
                        color: var(--text-secondary);
                        font-size: 0.875rem;
                    }
                    
                    .form-group input,
                    .form-group select {
                        width: 100%;
                        padding: 0.875rem 1rem;
                        background: var(--bg-input);
                        border: 1px solid var(--border-color);
                        border-radius: 10px;
                        color: var(--text-primary);
                        font-size: 1rem;
                        font-family: inherit;
                        transition: all 0.2s ease;
                    }
                    
                    .form-group input:focus,
                    .form-group select:focus {
                        outline: none;
                        border-color: var(--border-focus);
                        box-shadow: 0 0 0 3px var(--accent-glow);
                    }
                    
                    .form-group input::placeholder {
                        color: var(--text-muted);
                    }
                    
                    .form-group select option {
                        background: var(--bg-secondary);
                        color: var(--text-primary);
                    }
                    
                    .btn {
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        gap: 0.5rem;
                        padding: 0.875rem 1.5rem;
                        font-size: 1rem;
                        font-weight: 600;
                        font-family: inherit;
                        border: none;
                        border-radius: 10px;
                        cursor: pointer;
                        transition: all 0.2s ease;
                        text-decoration: none;
                    }
                    
                    .btn-primary {
                        width: 100%;
                        background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
                        color: white;
                        box-shadow: 0 4px 15px rgba(16, 185, 129, 0.3);
                    }
                    
                    .btn-primary:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
                    }
                    
                    .btn-secondary {
                        background: var(--bg-secondary);
                        color: var(--text-primary);
                        border: 1px solid var(--border-color);
                    }
                    
                    .btn-secondary:hover {
                        background: var(--bg-card);
                        border-color: var(--accent-primary);
                    }
                    
                    .btn-outline {
                        background: transparent;
                        color: var(--accent-primary);
                        border: 1px solid var(--accent-primary);
                    }
                    
                    .btn-outline:hover {
                        background: var(--accent-glow);
                    }
                    
                    .form-footer {
                        text-align: center;
                        margin-top: 1.5rem;
                        color: var(--text-secondary);
                        font-size: 0.875rem;
                    }
                    
                    .form-footer a {
                        color: var(--accent-primary);
                        text-decoration: none;
                        font-weight: 500;
                    }
                    
                    .form-footer a:hover {
                        text-decoration: underline;
                    }
                    
                    .alert {
                        padding: 1rem 1.25rem;
                        border-radius: 10px;
                        margin-bottom: 1.5rem;
                        display: flex;
                        align-items: center;
                        gap: 0.75rem;
                    }
                    
                    .alert-error {
                        background: rgba(239, 68, 68, 0.1);
                        border: 1px solid rgba(239, 68, 68, 0.3);
                        color: #fca5a5;
                    }
                    
                    .alert-success {
                        background: rgba(16, 185, 129, 0.1);
                        border: 1px solid rgba(16, 185, 129, 0.3);
                        color: #6ee7b7;
                    }
                    
                    .page-header {
                        margin-bottom: 2rem;
                    }
                    
                    .page-header h1 {
                        font-size: 2rem;
                        font-weight: 700;
                        margin-bottom: 0.5rem;
                    }
                    
                    .page-header p {
                        color: var(--text-secondary);
                    }
                    
                    .patient-card {
                        background: var(--bg-card);
                        border: 1px solid var(--border-color);
                        border-radius: 16px;
                        padding: 2rem;
                        margin-bottom: 1.5rem;
                    }
                    
                    .patient-card h2 {
                        font-size: 1.25rem;
                        margin-bottom: 1rem;
                        color: var(--text-primary);
                    }
                    
                    .json-display {
                        background: var(--bg-input);
                        border: 1px solid var(--border-color);
                        border-radius: 10px;
                        padding: 1.5rem;
                        overflow-x: auto;
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.8125rem;
                        line-height: 1.7;
                        color: var(--text-secondary);
                        white-space: pre-wrap;
                        word-break: break-word;
                    }
                    
                    .success-container {
                        text-align: center;
                        padding: 2rem;
                    }
                    
                    .success-icon {
                        width: 80px;
                        height: 80px;
                        background: var(--accent-glow);
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        margin: 0 auto 1.5rem;
                        color: var(--accent-primary);
                    }
                    
                    .success-icon svg {
                        width: 40px;
                        height: 40px;
                    }
                    
                    .success-container h2 {
                        font-size: 1.5rem;
                        margin-bottom: 0.75rem;
                    }
                    
                    .success-container p {
                        color: var(--text-secondary);
                        margin-bottom: 0.5rem;
                    }
                    
                    .resource-id {
                        display: inline-block;
                        background: var(--bg-input);
                        padding: 0.5rem 1rem;
                        border-radius: 8px;
                        font-family: 'JetBrains Mono', monospace;
                        font-size: 0.875rem;
                        color: var(--accent-primary);
                        margin: 1rem 0;
                    }
                    
                    .action-buttons {
                        display: flex;
                        gap: 1rem;
                        justify-content: center;
                        margin-top: 1.5rem;
                        flex-wrap: wrap;
                    }
                    
                    .stat-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                        gap: 1.5rem;
                        margin-bottom: 2rem;
                    }
                    
                    .stat-card {
                        background: var(--bg-card);
                        border: 1px solid var(--border-color);
                        border-radius: 12px;
                        padding: 1.5rem;
                    }
                    
                    .stat-card h3 {
                        color: var(--text-secondary);
                        font-size: 0.875rem;
                        font-weight: 500;
                        margin-bottom: 0.5rem;
                    }
                    
                    .stat-card .value {
                        font-size: 2rem;
                        font-weight: 700;
                        color: var(--accent-primary);
                    }
                    
                    .divider {
                        height: 1px;
                        background: var(--border-color);
                        margin: 1.5rem 0;
                    }
                    
                    /* FHIR Logo animation */
                    @keyframes pulse {
                        0%, 100% { opacity: 1; }
                        50% { opacity: 0.7; }
                    }
                    
                    .fhir-badge {
                        display: inline-flex;
                        align-items: center;
                        gap: 0.5rem;
                        padding: 0.25rem 0.75rem;
                        background: rgba(255, 87, 34, 0.1);
                        border: 1px solid rgba(255, 87, 34, 0.3);
                        border-radius: 20px;
                        font-size: 0.75rem;
                        font-weight: 600;
                        color: #ff7043;
                        margin-left: 0.5rem;
                    }
                    
                    .fhir-badge::before {
                        content: '🔥';
                        animation: pulse 2s infinite;
                    }
                """.trimIndent())
            }
        }
    }
    body {
        if (showNav) {
            nav {
                a(href = "/dashboard", classes = "nav-brand") {
                    // Medical cross icon
                    unsafe {
                        +"""<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"/>
                        </svg>"""
                    }
                    +"FHIR Middleware"
                }
                div(classes = "nav-links") {
                    a(href = "/dashboard") { +"Dashboard" }
                    a(href = "/patient/new") { +"Create Patient" }
                    a(href = "/swagger", target = "_blank") {
                        rel = "noopener noreferrer"
                        +"API Docs"
                    }
                }
                div(classes = "nav-user") {
                    if (userEmail != null) {
                        span { +userEmail }
                    }
                    a(href = "/logout", classes = "btn btn-outline") { +"Logout" }
                }
            }
        }
        main {
            content()
        }
    }
}

/**
 * Simple auth layout without navigation bar
 */
fun HTML.authLayout(pageTitle: String, content: DIV.() -> Unit) {
    layout(pageTitle, showNav = false) {
        div(classes = "auth-container") {
            content()
        }
    }
}

