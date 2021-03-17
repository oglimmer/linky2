package de.oglimmer.linky.rest

import de.oglimmer.linky.logic.TagsService
import de.oglimmer.linky.rest.dto.TagsDto
import de.oglimmer.linky.rest.mapping.TagsMapper
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/tags")
class TagsController(private val tagsService: TagsService, private val tagsMapper: TagsMapper) {

    @GetMapping
    fun loadAll(@AuthenticationPrincipal jwt: Jwt): Mono<TagsDto> =
        tagsService.loadAll(jwt.subject).map { tagsMapper.tagsToTagsDto(it) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@AuthenticationPrincipal jwt: Jwt): Mono<TagsDto> =
        tagsService.create(jwt.subject).map { tagsMapper.tagsToTagsDto(it) }

}

