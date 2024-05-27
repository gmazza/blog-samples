package net.glenmazza.splashclient.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Splash has a regular Event details API (Event object) and another for "extra" or extended details.
 * This object stores instances of the latter.
 * API: https://api-docs.splashthat.com/#2d121a2e-3c73-4276-9acc-70278208b91c
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class EventExtraDetails : Item() {
    val title: String? = null
    val domain: String? = null
    val type: String? = null
    val eventHost: String? = null
    val status: String? = null
    val tags: Set<String>? = null
    val facebookTitle: String? = null
    val facebookDescription: String? = null
    val twitterDefault: String? = null
    val linkedinTitle: String? = null
    val linkedinDescription: String? = null
    val hashtag: String? = null
}
