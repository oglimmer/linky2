package de.oglimmer.linky.logic

import de.oglimmer.linky.dao.LinkCrudRepository
import de.oglimmer.linky.entity.Link
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.function.Function

private val logger = KotlinLogging.logger {}


@Service
@Transactional
class LinkService(
    private val repository: LinkCrudRepository,
    private val tagsService: TagsService
) {

    fun create(link: Link, subject: String): Mono<Link> = repository
        .save(link)
        .doOnSuccess { savedLink -> tagsService.processSavedLink(subject, savedLink) }


    fun update(subject: String, linkId: String, mapNewData: Function<Link, Link>): Mono<Link> =
        repository.findByIdAndUserid(linkId, subject)
            .map { mapNewData.apply(it) }
            .flatMap { repository.save(it) }


    fun loadAllLinks(subject: String): Flux<Link> = repository.findByUserid(subject)

    fun loadLinksByUserAndTags(subject: String, tags: Array<String>): Flux<Link> =
        repository.findByUserIdAndTags(subject, tags)

    fun loadOneLink(subject: String, linkId: String): Mono<Link> = repository.findByIdAndUserid(linkId, subject)

    fun deleteLink(linkId: String) = repository.deleteById(linkId)

    fun loadAndUpdateForRedirect(subject: String, linkId: String): Mono<Link> =
        repository.findByIdAndUserid(linkId, subject)
            .map { it.copy(callCounter = it.callCounter + 1, lastCalled = Instant.now()) }
            .flatMap { repository.save(it) }


}
