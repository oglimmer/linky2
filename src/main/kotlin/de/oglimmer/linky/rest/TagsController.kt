package de.oglimmer.linky.rest

import de.oglimmer.linky.entity.Tags
import de.oglimmer.linky.logic.TagsService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/tags")
class TagsController(private var tagsService: TagsService) {

    @GetMapping
    fun loadAll(@AuthenticationPrincipal jwt: Jwt): Mono<Tags> = tagsService.loadAll(jwt.subject)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@AuthenticationPrincipal jwt: Jwt): Mono<Tags> = tagsService.create(jwt.subject)

}

