package de.oglimmer.linky.logic

import de.oglimmer.linky.entity.Link
import de.oglimmer.linky.rest.dto.LinkModifyDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Service
@Transactional
class LinkServiceFacade(
    private val favicon: Favicon,
    private val titleProducer: TitleProducer,
    private val linkService: LinkService
) {
    fun createLink(linkModifyDto: LinkModifyDto, subject: String): Mono<Link> =
        titleProducer.buildTitle(linkModifyDto).flatMap { title ->
            favicon.loadFavicon(linkModifyDto.linkUrl)
                .flatMap { faviconUrl ->
                    linkService.create(
                        newLink(linkModifyDto, title, faviconUrl, subject),
                        subject
                    )
                }
        }

    private fun newLink(
        linkModifyDto: LinkModifyDto,
        title: String,
        faviconUrl: String?,
        subject: String
    ) = Link(
        id = generateId(),
        linkUrl = completeProtocol(linkModifyDto.linkUrl),
        callCounter = 0,
        createdDate = Instant.now(),
        lastCalled = Instant.now(),
        tags = copyTags(linkModifyDto.tags),
        rssUrl = linkModifyDto.rssUrl,
        pageTitle = title,
        notes = linkModifyDto.notes,
        faviconUrl = faviconUrl,
        userid = subject
    )


    fun updateLink(subject: String, linkId: String, linkModifyDto: LinkModifyDto): Mono<Link> =
        linkService.update(subject, linkId) {
            it.copy(
                linkUrl = completeProtocol(linkModifyDto.linkUrl),
                tags = copyTags(linkModifyDto.tags),
                rssUrl = linkModifyDto.rssUrl,
                pageTitle = linkModifyDto.pageTitle ?: it.pageTitle,
                notes = linkModifyDto.notes
            )
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