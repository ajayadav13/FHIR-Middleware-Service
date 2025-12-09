package com.example.fhir

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.client.api.IGenericClient
import io.ktor.server.config.*

/**
 * Provides a singleton HAPI FHIR R4 client configured for the external HAPI test server.
 */
class FhirClientProvider(config: ApplicationConfig) {
    private val baseUrl: String = config.config("fhir").property("baseUrl").getString()
    private val ctx: FhirContext = FhirContext.forR4()

    val client: IGenericClient by lazy {
        ctx.newRestfulGenericClient(baseUrl).apply {
            // Enable pretty logging for easier debugging
            setPrettyPrint(true)
        }
    }

    val jsonParser = ctx.newJsonParser().apply { setPrettyPrint(true) }
}

