package net.glenmazza.splashclient.model

/**
 * Requesting Attendees for an event:
 * https://api-docs.splashthat.com/#98d629dc-3435-4a84-a050-c21af8b976ff
 */
class EventAttendeesRequest {
    var eventId: String? = null
    var limit: Int = 100
    var page: Int = 1
}
