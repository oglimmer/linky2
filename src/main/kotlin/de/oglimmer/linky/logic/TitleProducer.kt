package de.oglimmer.linky.logic

import de.oglimmer.linky.rest.dto.LinkModifyDto
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.net.URL

@Service
class TitleProducer {

    private fun addProtocolIfMissing(url: String): String {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "http://$url"
        }
        return url
    }

    private fun getBaseUrl(url: String): String {
        val urlClass = URL(addProtocolIfMissing(url))
        return "${urlClass.protocol}://${urlClass.host}" + if (urlClass.defaultPort != urlClass.port && urlClass.port != -1) ":${urlClass.port}" else ""
    }

    fun buildTitle(linkModifyDto: LinkModifyDto): Mono<String> =
            if (linkModifyDto.pageTitle != null && linkModifyDto.pageTitle.isNotBlank())
                Mono.just(linkModifyDto.pageTitle)
            else
                HttpClient.create()
                        .followRedirect(true)
                        .get()
                        .uri(getBaseUrl(linkModifyDto.linkUrl))
                        .responseSingle { _, bytes -> bytes.asString() }
                        .map { TitleHtmlScraper.getTitleFromHtml(it) ?: linkModifyDto.linkUrl }

}

private object TitleHtmlScraper {

    private val linkRegEx = "<title>(.*)</title>".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))

    fun getTitleFromHtml(body: String): String? {
        return linkRegEx.find(body)?.groupValues?.get(1)
    }
}