package de.oglimmer.linky.rest.dto

data class LinkModifyDto(
    val linkUrl: String,
    val tags: List<String>,
    val rssUrl: String?,
    val pageTitle: String?,
    val notes: String?
)