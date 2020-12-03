package de.oglimmer.linky.rest

import de.oglimmer.linky.entity.Link
import de.oglimmer.linky.logic.LinkService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/links")
class LinkController(val linkService: LinkService) {

    @GetMapping
    fun loadAll(@AuthenticationPrincipal jwt: Jwt?): Flux<Link> =
            linkService.loadAllLinks(jwt!!.subject)

    @GetMapping("/by-tags/{tag}")
    fun getByTag(@AuthenticationPrincipal jwt: Jwt, @PathVariable tag: String): Flux<Link> =
            linkService.loadLinksByUserAndTags(jwt.subject, arrayOf(tag))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@AuthenticationPrincipal jwt: Jwt, @RequestBody linkCreate: LinkCreate): Mono<Link> =
            linkService.createLink(linkCreate, jwt.subject)

    @PutMapping("/{linkId}")
    @PreAuthorize("hasPermission(#linkId, 'LINK')")
    fun update(@AuthenticationPrincipal jwt: Jwt,
               @PathVariable linkId: String,
               @RequestBody linkCreate: LinkCreate): Mono<Link> =
            linkService.updateLink(jwt.subject, linkId, linkCreate)

    @GetMapping("/{linkId}")
    @PreAuthorize("hasPermission(#linkId, 'LINK')")
    fun loadOne(@AuthenticationPrincipal jwt: Jwt,
                @PathVariable linkId: String): Mono<Link> =
            linkService.loadOneLink(jwt.subject, linkId)

    @DeleteMapping("/{linkId}")
    @PreAuthorize("hasPermission(#linkId, 'LINK')")
    fun deleteLink(@PathVariable linkId: String) = linkService.deleteLink(linkId)

    @GetMapping("/{linkId}/redirect")
    @PreAuthorize("hasPermission(#linkId, 'LINK')")
    fun redirect(@AuthenticationPrincipal jwt: Jwt,
                 @PathVariable linkId: String): Mono<ResponseEntity<String>> = linkService
            .loadAndUpdateForRedirect(jwt.subject, linkId)
            .map {
                ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                        .header("Location", it.linkUrl)
                        .body(it.linkUrl)
            }

}

data class LinkCreate(
        val linkUrl: String,
        val tags: List<String>,
        val rssUrl: String?,
        val pageTitle: String?,
        val notes: String?
)
