package de.oglimmer.linky.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "tags")
data class Tags(
        @Id val id: String,
        val children: Map<String, Any>,
        val userid: String
)
