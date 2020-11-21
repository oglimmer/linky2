package de.oglimmer.linky.logic

import de.oglimmer.linky.dao.LinkCrudRepository
import de.oglimmer.linky.dao.TagsCrudRepository
import de.oglimmer.linky.entity.Link
import de.oglimmer.linky.entity.Tags
import de.oglimmer.linky.rest.LinkCreate
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class LinkService(private var repository: LinkCrudRepository, private var tagsCrudRepository: TagsCrudRepository) {

    fun createLink(linkCreate: LinkCreate, subject: String): Mono<Link> {
        return TitleProducer.buildTitle(linkCreate).flatMap { title ->
            Favicon.loadFavicon(linkCreate.linkUrl)
                    .flatMap { faviconUrl ->
                        repository.save(Link(id = generateId(),
                                linkUrl = completeProtocol(linkCreate.linkUrl),
                                callCounter = 0,
                                createdDate = Instant.now(),
                                lastCalled = Instant.now(),
                                tags = copyTags(linkCreate.tags),
                                rssUrl = linkCreate.rssUrl,
                                pageTitle = title,
                                notes = linkCreate.notes,
                                faviconUrl = faviconUrl,
                                userid = subject)
                        ).doOnSuccess { savedLink ->
                            tagsCrudRepository.findByUserid(subject)
                                    .defaultIfEmpty(Tags(id = generateId(),
                                            children = mapOf("portal" to emptyMap<String, Any>(), "all" to emptyMap()),
                                            userid = subject))
                                    .map { addMissingTags(it, savedLink) }
                                    .flatMap { tagsCrudRepository.save(it) }
                                    .subscribe()
                        }
                    }
        }
    }

    fun updateLink(subject: String, linkId: String, linkCreate: LinkCreate): Mono<Link> =
            repository.findByIdAndUserid(linkId, subject)
                    .map {
                        it.copy(linkUrl = completeProtocol(linkCreate.linkUrl),
                                tags = copyTags(linkCreate.tags),
                                rssUrl = linkCreate.rssUrl,
                                pageTitle = linkCreate.pageTitle ?: it.pageTitle,
                                notes = linkCreate.notes)
                    }
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


    @Suppress("UNCHECKED_CAST")
    private fun found(tags: Map<String, Any>, usedTagInLink: String): Boolean = tags.containsKey(usedTagInLink) ||
            tags.any { it.value is Map<*, *> && found(it.value as Map<String, Any>, usedTagInLink) }


    private fun addMissingTags(tags: Tags, savedLink: Link): Tags {
        val copyTag = tags.copy(children = tags.children.toMutableMap())
        savedLink.tags
                .filter { usedTagInLink -> !found(tags.children, usedTagInLink) }
                .forEach { missingTag ->
                    (copyTag.children as MutableMap)[missingTag] = mapOf<String, Any>()
                }
        return copyTag
    }

    private fun completeProtocol(linkUrl: String): String =
            if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://"))
                "http://$linkUrl"
            else
                linkUrl

    private fun copyTags(tags: List<String>): List<String> {
        val returnList = ArrayList(tags)
        if (returnList.isEmpty()) {
            returnList.add("portal")
        }
        if (!returnList.contains("all")) {
            returnList.add("all")
        }
        return returnList
    }

    private fun generateId(): String = UUID.randomUUID().toString()


}
