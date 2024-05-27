package net.glenmazza.splashclient.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun createObjectMapper(): ObjectMapper? {
    val timeModule = JavaTimeModule()

    // https://stackoverflow.com/q/51527794/1207540
    timeModule.addDeserializer(
        LocalDate::class.java,
        LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    )
    timeModule.addSerializer(
        LocalDate::class.java,
        LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    )
    timeModule.addDeserializer(
        LocalDateTime::class.java,
        LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME)
    )
    return JsonMapper.builder() // allow "Name" in JSON to map to "name" in class
        .configure(
            MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES,
            true
        ) // timestamps to Instant (https://stackoverflow.com/q/45762857/1207540)
        .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
        // Splash JSON uses e.g. event_start, Java eventStart, so using SnakeCaseStrategy
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build().registerModule(timeModule)
}
