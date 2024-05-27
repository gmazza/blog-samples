package net.glenmazza.splashclient.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class MultiItemResponse<T : Item?> {
    var meta: Meta? = null

    // page is 1-based, of max size limit (Warning: page 0 returns same data as page 1)
    var page: Int = 1
    var limit: Int = 0

    // count = total number of records available (not just the subset returned in this query)
    var count: Int = 0

    // pages not always provided (appears to be for attendees but not events), equals count/limit rounded up
    var pages: Int? = null
    var data: MutableList<T> = ArrayList()
}
