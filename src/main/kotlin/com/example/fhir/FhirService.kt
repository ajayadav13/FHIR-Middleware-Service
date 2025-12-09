package com.example.fhir

import ca.uhn.fhir.rest.api.MethodOutcome
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Patient

class FhirService(config: io.ktor.server.config.ApplicationConfig) {
    private val provider = FhirClientProvider(config)
    private val client = provider.client

    /**
     * Create a Patient on the public HAPI server.
     * Returns the created resource id when successful.
     */
    fun createPatient(firstName: String, lastName: String, gender: String?): MethodOutcome {
        val patient = Patient().apply {
            addName().setFamily(lastName).addGiven(firstName)
            gender?.let {
                val normalized = it.lowercase()
                this.gender = when (normalized) {
                    "male" -> Enumerations.AdministrativeGender.MALE
                    "female" -> Enumerations.AdministrativeGender.FEMALE
                    "other" -> Enumerations.AdministrativeGender.OTHER
                    else -> Enumerations.AdministrativeGender.UNKNOWN
                }
            }
        }

        // HAPI client handles serialization and HTTP for us.
        return client.create()
            .resource(patient)
            .execute()
    }

    /**
     * Fetch a Patient by id from the public HAPI server and return pretty JSON.
     */
    fun fetchPatientAsJson(id: String): String {
        val patient = client
            .read()
            .resource(Patient::class.java)
            .withId(id)
            .execute()

        return provider.jsonParser.encodeResourceToString(patient)
    }
}

