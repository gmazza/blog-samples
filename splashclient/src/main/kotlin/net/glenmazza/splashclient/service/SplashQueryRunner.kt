package net.glenmazza.splashclient.service

import SingleItemResponse
import com.fasterxml.jackson.databind.JavaType
import net.glenmazza.splashclient.model.Contact
import net.glenmazza.splashclient.model.Event
import net.glenmazza.splashclient.model.EventAttendee
import net.glenmazza.splashclient.model.EventAttendeesRequest
import net.glenmazza.splashclient.model.EventExtraDetails
import net.glenmazza.splashclient.model.EventRequest
import net.glenmazza.splashclient.model.Item
import net.glenmazza.splashclient.model.MultiItemResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class SplashQueryRunner(webClient1: WebClient, private val apiUrl: String) : AbstractRESTService(webClient1) {

    private val LOGGER: Logger =
        LoggerFactory.getLogger(SplashQueryRunner::class.java)

    private val singleItemResponseParametricTypes: MutableMap<Class<out Item?>, JavaType> =
        HashMap()

    private val multiItemResponseParametricTypes: MutableMap<Class<out Item?>, JavaType> =
        HashMap()

    // request single event
    fun getEvent(eventId: String): SingleItemResponse<Event> {
        val type = singleItemResponseParametricTypes.computeIfAbsent(
            Event::class.java
        ) { recordType: Class<out Item?>? ->
            createParametricJavaType(
                SingleItemResponse::class.java,
                recordType!!
            )
        }

        val jsonResult = webClient
            .get()
            .uri("$apiUrl/events/$eventId")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(String::class.java)
            .retry(1)
            .block()!!
        return objectMapper!!.readValue(jsonResult, type)
    }

    fun getExtraDetailsForEvent(eventId: String): SingleItemResponse<EventExtraDetails> {
        val type = singleItemResponseParametricTypes.computeIfAbsent(
            EventExtraDetails::class.java
        ) { recordType: Class<out Item?>? ->
            createParametricJavaType(
                SingleItemResponse::class.java,
                recordType!!
            )
        }

        val jsonResult = webClient
            .get()
            .uri("$apiUrl/events/$eventId/settings")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(String::class.java)
            .retry(1)
            .block()!!

        return objectMapper!!.readValue(jsonResult, type)
    }

    fun getMultipleEvents(eventsRequest: EventRequest): MultiItemResponse<Event> {
        if (eventsRequest.page == 0) {
            // 0 and 1 return same data, throw error to guard against 0 and 1 both being requested
            throw IllegalArgumentException("Minimum value of page is 1")
        }

        val type = multiItemResponseParametricTypes.computeIfAbsent(
            Event::class.java
        ) { recordType: Class<out Item?>? ->
            createParametricJavaType(
                MultiItemResponse::class.java,
                recordType!!
            )
        }

        val queryParams: String = String.format(
            "page=%d&past=%s&limit=%d&event_start_after=%s&event_start_before=%s",
            eventsRequest.page,
            eventsRequest.past,
            eventsRequest.limit,
            eventsRequest.eventStartAfter,
            eventsRequest.eventStartBefore
        )

        val jsonResult = webClient
            .get()
            .uri("$apiUrl/events?$queryParams")
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON_VALUE
            )
            .retrieve()
            .bodyToMono(String::class.java)
            .retry(1)
            .block()!!
        return objectMapper!!.readValue(jsonResult, type)
    }

    fun getEventAttendees(request: EventAttendeesRequest): MultiItemResponse<EventAttendee> {
        if (request.page == 0) {
            // 0 and 1 return same data, throw error to guard against 0 and 1 both being requested
            throw IllegalArgumentException("Minimum value of page is 1")
        }

        val type = multiItemResponseParametricTypes.computeIfAbsent(
            EventAttendee::class.java
        ) { recordType: Class<out Item?>? ->
            createParametricJavaType(
                MultiItemResponse::class.java,
                recordType!!
            )
        }

        val queryParams: String = String.format(
            "event_id=%s&page=%d&limit=%d",
            request.eventId,
            request.page,
            request.limit
        )

        val jsonResult = webClient
            .get()
            .uri("$apiUrl/groupcontacts?$queryParams")
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON_VALUE
            )
            .retrieve()
            .bodyToMono(String::class.java)
            .retry(1)
            .block()!!
        return objectMapper!!.readValue(jsonResult, type)
    }

    fun getContact(contactId: String): SingleItemResponse<Contact> {
        val type = singleItemResponseParametricTypes.computeIfAbsent(Contact::class.java) { recordType: Class<out Item?>? ->
            createParametricJavaType(
                SingleItemResponse::class.java,
                recordType!!
            )
        }

        val jsonResult = webClient
            .get()
            .uri("$apiUrl/contacts/$contactId")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(String::class.java)
            .retry(1)
            .block()!!

        return objectMapper!!.readValue(jsonResult, type)
    }

    private fun createParametricJavaType(parentClass: Class<*>, recordType: Class<out Item?>): JavaType {
        val jt = objectMapper!!.typeFactory.constructParametricType(
            parentClass,
            recordType
        )
        LOGGER.info(
            "Created new JavaType for recordType {}",
            recordType.name
        )
        return jt
    }
}
