package de.oglimmer.linky.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "links")
data class Link(
        @Id val id: String,
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