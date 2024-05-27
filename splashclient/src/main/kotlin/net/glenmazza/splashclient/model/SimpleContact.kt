package net.glenmazza.splashclient.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * A Splash Contact is a person who goes to zero or more Events, stored at the organization
 * level.  They are also stored at the Event-level (EventAttendee), with different data.
 * https://api-docs.splashthat.com/#45ea8ba2-6e48-400c-8785-8a435a3184bf
 */
@JsonIgnoreProperties(ignoreUnknown = true)
open class SimpleContact : Item() {
    var id: String? = null
    var lastName: String? = null
    var firstName: String? = null
    var primaryEmail: String? = null
    var title: String? = null
    var notes: String? = null
    var organizationName: String? = null
    var phone: String? = null
    var facebookUrl: String? = null
    var linkedinUrl: String? = null
    var instagramUrl: String? = null
    var salesforceId: String? = null
}
