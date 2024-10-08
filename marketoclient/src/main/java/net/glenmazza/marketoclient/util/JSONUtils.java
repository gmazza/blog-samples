package net.glenmazza.marketoclient.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class JSONUtils {

    public static ObjectMapper createObjectMapper() {
        JavaTimeModule timeModule = new JavaTimeModule();

        // https://stackoverflow.com/q/51527794/1207540
        timeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        timeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return JsonMapper.builder()
                // allow "Name" in JSON to map to "name" in class
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // timestamps to Instant (https://stackoverflow.com/q/45762857/1207540)
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                .build().registerModule(timeModule);
    }
}
