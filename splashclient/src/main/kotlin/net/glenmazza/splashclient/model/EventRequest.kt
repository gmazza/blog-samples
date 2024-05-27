package net.glenmazza.splashclient.model

import java.time.LocalDate

// Requesting multiple events: https://api-docs.splashthat.com/#a761b9f1-dfa4-41fc-8ec5-22125597760c
class EventRequest {
    var eventStartAfter: LocalDate? = null
    var eventStartBefore: LocalDate? = null
    var limit: Int = 100
    var page: Int = 1
    var past: Boolean = true
}
