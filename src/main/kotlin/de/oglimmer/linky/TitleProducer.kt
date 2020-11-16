package de.oglimmer.linky

import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.net.URL

object TitleProducer {

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

    private fun buildUrl(url: String, urlIsFavicon: Boolean): String {
        if (urlIsFavicon) {
            return "${getBaseUrl(url)}/favicon.ico"
        }
        return addProtocolIfMissing(url)
    }

    fun buildTitle(linkCreate: LinkCreate): Mono<String> =
            if (linkCreate.pageTitle != null && linkCreate.pageTitle.isNotBlank())
                Mono.just(linkCreate.pageTitle)
            else
                HttpClient.create()
                        .followRedirect(true)
                        .get()
                        .uri(buildUrl(linkCreate.linkUrl, false))
                        .responseSingle { _, bytes -> bytes.asString() }
                        .map { TitleHtmlScraper.getTitleFromHtml(it) ?: linkCreate.linkUrl }

}

object TitleHtmlScraper {

    private val linkRegEx = "<title>(.*)</title>".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))

    fun getTitleFromHtml(body: String): String? {
        return linkRegEx.find(body)?.groupValues?.get(1)
    }
}