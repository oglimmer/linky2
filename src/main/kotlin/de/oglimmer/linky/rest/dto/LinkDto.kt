package de.oglimmer.linky.rest.dto

import java.time.Instant

data class LinkDto(
    val id: String,
    val linkUrl: String,
    val callCounter: Long,
    val lastCalled: Instant,
    val createdDate: Instant,
    val tags: List<String>,
    val rssUrl: String?,
    val pageTitle: String,
    val notes: String?,
    val faviconUrl: String?,
    val userid: String
)