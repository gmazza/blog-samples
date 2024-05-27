package net.glenmazza.splashclient.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.Date

/**
 * EventAttendees hold event-specific data for a Contact going to an Event
 * https://api-docs.splashthat.com/#45ea8ba2-6e48-400c-8785-8a435a3184bf
 * (also Contact data too like name and email)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class EventAttendee : Item() {
    var id: String? = null
    var contact: SimpleContact? = null
    var eventRsvp: EventRsvp? = null
    var answers: List<Answer> = ArrayList()
}

@JsonIgnoreProperties(ignoreUnknown = true)
class EventRsvp {
    var id: String? = null
    var eventId: String? = null
    var contactId: String? = null
    var ticketSale: String? = null
    var trackingLink: TrackingLink? = null
    var parentEventRsvp: EventRsvp? = null
    var checkedOut: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var guestName: String? = null
    var email: String? = null
    var plusOne: String? = null
    var created: Date? = null
    var modified: Date? = null
    var dateRsvped: Date? = null
    var ipAddress: String? = null
    var attending: Boolean = false
    var deleted: String? = null
    var checkedIn: Date? = null
    var unsubTag: String? = null
    var ticketNumber: String? = null
    var vip: Boolean = false
    var waitlist: Boolean = false
    var qrUrl: String? = null
    var unsubscribed: Boolean = false
}

class TrackingLink {
    var id: String? = null
    var code: String? = null
    var url: String? = null
    var uniqueViews: Int? = 0
    var created: Date? = null
    var active: Boolean = true
    var views: Int? = 0
}

class Answer {
    var questionId: Int? = null
    var answer: String? = null
}
