package net.glenmazza.splashclient.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Contact : SimpleContact() {
    var street: String? = null
    var city: String? = null
    var state: String? = null
    var zip: String? = null
}
