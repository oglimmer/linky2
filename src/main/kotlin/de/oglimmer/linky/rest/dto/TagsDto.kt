package de.oglimmer.linky.rest.dto

data class TagsDto(
    val id: String,
    val children: Map<String, Any>,
    val userid: String
)
