package net.glenmazza.splashclient.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.Date

// Event Details API: https://api-docs.splashthat.com/#0b1ef42d-dd69-4298-97e2-df7d55cb56c2
@JsonIgnoreProperties(ignoreUnknown = true)
class Event : Item() {
    var id: String? = null
    var title: String? = null
    var eventStart: Date? = null
    var eventEnd: Date? = null
    var venueName: String? = null
    var city: String? = null
    var state: String? = null
    var zipCode: String? = null
    var country: String? = null
    var domain: String? = null
    var eventType: EventType? = null
    var eventSetting: EventSetting? = null
}

@JsonIgnoreProperties(ignoreUnknown = true)
class EventType {
    var name: String? = null
}

@JsonIgnoreProperties(ignoreUnknown = true)
class EventSetting {
    var eventHashtag: String? = null
}
