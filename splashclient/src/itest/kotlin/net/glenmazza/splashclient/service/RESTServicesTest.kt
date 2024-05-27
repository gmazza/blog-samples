package net.glenmazza.splashclient.service

import SingleItemResponse
import net.glenmazza.splashclient.TestApplication
import net.glenmazza.splashclient.model.Event
import net.glenmazza.splashclient.model.EventAttendee
import net.glenmazza.splashclient.model.EventAttendeesRequest
import net.glenmazza.splashclient.model.EventExtraDetails
import net.glenmazza.splashclient.model.EventRequest
import net.glenmazza.splashclient.model.EventRsvp
import net.glenmazza.splashclient.model.MultiItemResponse
import net.glenmazza.splashclient.model.ServiceException
import net.glenmazza.splashclient.model.SimpleContact
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

// Test values below are dummy data, will need to be replaced with actual values from Splash.
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(
    classes = [TestApplication::class],
    initializers = [ConfigDataApplicationContextInitializer::class]
)
@Disabled("testing requires config setup as given in application.properties")
class RESTServicesTest(
    @Autowired val queryRunner: SplashQueryRunner
) {

    // Note Splash API client returns 429 error codes if more than 2 (or another number) calls per second,
    // adding Thread.sleep(1000) to allow all tests to run successfully.

    @Test
    fun eventByEventIdAndCheckTooManyRequests() {
        Thread.sleep(1000)
        val eventId = "123456789"
        var numRegular: Int = 0
        var numException: Int = 0
        for (i: Int in 0..6) {
            try {
                val response: SingleItemResponse<Event> = queryRunner.getEvent(eventId)
                numRegular++
                Assertions.assertNotNull(response)
                assertEquals(200, response.meta!!.code)
                assertTrue(response.success)
                confirmEvent(response.data!!)
            } catch (e: ServiceException) {
                numException++
                assertEquals(429, e.statusCode)
            }
        }
        assertTrue(numRegular > 0)
        assertTrue(numException > 0)
    }

    @Test
    fun testEventExtraDetails() {
        Thread.sleep(1000)
        val eventId = "123456789"
        val response: SingleItemResponse<EventExtraDetails> = queryRunner.getExtraDetailsForEvent(eventId)
        Assertions.assertNotNull(response)
        assertEquals(200, response.meta!!.code)
        assertTrue(response.success)
        val eventExtraDetails = response.data!!
        assertEquals("open", eventExtraDetails.status)
        assertEquals("awesomeevent", eventExtraDetails.domain)
        assertEquals("Mr. Event Host", eventExtraDetails.eventHost)
        assertEquals("#awesomeevent", eventExtraDetails.hashtag)
    }

    @Test
    fun multipleEventsByStartAndEndDate() {
        Thread.sleep(1000)
        val request = EventRequest()
        request.eventStartAfter = LocalDate.of(2024, 1, 1)
        request.eventStartBefore = LocalDate.of(2024, 2, 20)
        request.limit = 5

        val response: MultiItemResponse<Event> = queryRunner.getMultipleEvents(request)
        Assertions.assertNotNull(response)
        assertEquals(200, response.meta!!.code)
        assertEquals(5, response.limit)
        confirmEvent(response.data[3])
    }

    @Test
    fun testGetEventAttendees() {
        Thread.sleep(1000)
        val request = EventAttendeesRequest()
        request.eventId = "123456789"
        request.limit = 10

        val response: MultiItemResponse<EventAttendee> = queryRunner.getEventAttendees(request)
        Assertions.assertNotNull(response)
        assertEquals(200, response.meta!!.code)
        assertEquals(10, response.limit)

        val ea: EventAttendee = response.data[2]
        val eaContact: SimpleContact = ea.contact!!
        val eaRsvp: EventRsvp = ea.eventRsvp!!
        assertEquals("234567890", ea.id)
        assertEquals("Maria", eaContact.firstName)
        // hide last name & email
        assertEquals(5, eaContact.lastName!!.length)
        assertEquals(14, eaContact.primaryEmail!!.length)
        assertEquals("Writer", eaContact.title)
        assertEquals("Notes for guest", eaContact.notes)
        assertEquals("Acme Inc.", eaContact.organizationName)
        assertEquals("345678901", eaRsvp.id)
        assertEquals("456789012", eaRsvp.eventId)
        assertEquals("567890123", eaRsvp.contactId)
        assertEquals(eaContact.firstName, eaRsvp.firstName)
        assertEquals(eaContact.lastName, eaRsvp.lastName)
        assertEquals(eaContact.primaryEmail, eaRsvp.email)
        assertEquals("0", eaRsvp.plusOne)
        assertEquals(true, eaRsvp.attending)
        assertEquals(
            "2024-02-24T18:37:02",
            ISO_LOCAL_DATE_TIME.format(eaRsvp.dateRsvped!!.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime())
        )
        assertEquals(
            "2024-03-06T15:52:22",
            ISO_LOCAL_DATE_TIME.format(eaRsvp.checkedIn!!.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime())
        )
        val trackingLink = eaRsvp.trackingLink!!
        assertEquals("123456", trackingLink.id)
        assertEquals("Email: Personal Invite 1.23", trackingLink.code)
        assertEquals("PersonalInvite", trackingLink.url)
        assertEquals(9, ea.answers.size)
        val singleAnswer = ea.answers[7]
        assertEquals(1226789, singleAnswer.questionId)
        assertEquals("Remote", singleAnswer.answer)
    }

    @Test
    fun testGetContact() {
        Thread.sleep(1000)
        val response = queryRunner.getContact("345678901")
        Assertions.assertNotNull(response)
        assertEquals(200, response.meta!!.code)
        assertTrue(response.success)
        val contact = response.data!!
        assertEquals("345678901", contact.id)
        assertEquals(6, contact.lastName!!.length)
        assertEquals("Michael", contact.firstName)
    }

    private fun confirmEvent(event: Event) {
        assertEquals("456789012", event.id)
        assertEquals("Event on Solving Problems", event.title)
        assertEquals(
            "2024-01-22T21:30:00",
            ISO_LOCAL_DATE_TIME.format(event.eventStart!!.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime())
        )
        assertEquals(
            "2024-01-23T00:30:00",
            ISO_LOCAL_DATE_TIME.format(event.eventEnd!!.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime())
        )
        assertEquals("City Armory", event.venueName)
        assertEquals("Bigtown", event.city)
        assertEquals("VA", event.state)
        assertEquals("12345", event.zipCode)
        assertEquals("United States", event.country)
        assertEquals("solvingproblems", event.domain)
        assertEquals("#SolvingProblems", event.eventSetting!!.eventHashtag)
        assertEquals("Event", event.eventType!!.name)
    }
}
