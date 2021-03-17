package de.oglimmer.linky.rest

import de.oglimmer.linky.logic.LinkService
import de.oglimmer.linky.logic.LinkServiceFacade
import de.oglimmer.linky.rest.dto.LinkDto
import de.oglimmer.linky.rest.dto.LinkModifyDto
import de.oglimmer.linky.rest.mapping.LinkMapper
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
class LinkController(private val linkService: LinkService,
                     private val linkServiceFacade: LinkServiceFacade,
                     private val linkMapper: LinkMapper) {

    @GetMapping
    fun loadAll(@AuthenticationPrincipal jwt: Jwt?): Flux<LinkDto> =
        linkService.loadAllLinks(jwt!!.subject).map { linkMapper.linkToLinkDto(it) }

    @GetMapping("/by-tags/{tag}")
    fun getByTag(@AuthenticationPrincipal jwt: Jwt, @PathVariable tag: String): Flux<LinkDto> =
        linkService.loadLinksByUserAndTags(jwt.subject, arrayOf(tag)).map { linkMapper.linkToLinkDto(it) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@AuthenticationPrincipal jwt: Jwt, @RequestBody linkDto: LinkModifyDto): Mono<LinkDto> =
        linkServiceFacade.createLink(linkDto, jwt.subject).map { linkMapper.linkToLinkDto(it) }

    @PutMapping("/{linkId}")
    @PreAuthorize("hasPermission(#linkId, 'LINK')")
    fun update(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable linkId: String,
        @RequestBody linkDto: LinkModifyDto
    ): Mono<LinkDto> =
        linkServiceFacade.updateLink(jwt.subject, linkId, linkDto).map { linkMapper.linkToLinkDto(it) }

    @GetMapping("/{linkId}")
    @PreAuthorize("hasPermission(#linkId, 'LINK')")
    fun loadOne(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable linkId: String
    ): Mono<LinkDto> =
        linkService.loadOneLink(jwt.subject, linkId).map { linkMapper.linkToLinkDto(it) }

    @DeleteMapping("/{linkId}")
    @PreAuthorize("hasPermission(#linkId, 'LINK')")
    fun deleteLink(@PathVariable linkId: String) = linkService.deleteLink(linkId)

    @GetMapping("/{linkId}/redirect")
    @PreAuthorize("hasPermission(#linkId, 'LINK')")
    fun redirect(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable linkId: String
    ): Mono<ResponseEntity<String>> = linkService
        .loadAndUpdateForRedirect(jwt.subject, linkId)
        .map {
            ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .header("Location", it.linkUrl)
                .body(it.linkUrl)
        }

}





