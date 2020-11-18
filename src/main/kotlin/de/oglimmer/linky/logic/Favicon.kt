package de.oglimmer.linky.logic

import mu.KotlinLogging
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.netty.http.client.HttpClient
import java.net.URL

private val logger = KotlinLogging.logger {}

object Favicon {
    const val defaultIconUrl = "DEFAULT.ico"

    fun loadFavicon(url: String): Mono<String> = HttpClient.create()
            .followRedirect(false)
            .get()
            .uri(buildUrl(url, true))
            .response()
            .filter { it.status().code() < 400 }
            .map {
                var mappedUrl = ""
                if (it.status().code() < 300) {
                    logger.debug { "Looking for $url and got ${it.status().code()}" }
                    mappedUrl = buildUrl(url, true)
                } else if (it.status().code() < 400) {
                    logger.debug { "Looking for $url and got ${it.status().code()} using ${it.responseHeaders().get("location")}" }
                    mappedUrl = "REDIRECT:${it.responseHeaders().get("location")}"
                }
                mappedUrl
            }
            .flatMap {
                if (it.startsWith("REDIRECT:"))
                    checkFavicon(it.substring("REDIRECT:".length))
                else
                    Mono.just(it)
            }
            .switchIfEmpty { loadFavionFromHtml(url) }
            .onErrorContinue { _, _ -> defaultIconUrl }

    private fun checkFavicon(url: String): Mono<String> = HttpClient.create()
            .followRedirect(false)
            .get()
            .uri(buildUrl(url, true))
            .response()
            .filter { it.status().code() < 300 }
            .map { if (it.requestHeaders()["content-type"].contains("image")) url else "" }
            .filter { it.isNotBlank() }

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

    private fun loadFavionFromHtml(url: String): Mono<String> = HttpClient.create()
            .followRedirect(true)
            .get()
            .uri(buildUrl(url, false))
            .responseSingle { _, bytes -> bytes.asString() }
            .map { FaviconHtmlScraper.getFaviconFromHtml(it) }


}

object FaviconHtmlScraper {

    private val linkRegEx = "<link (.*)>".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private val relRegEx = "rel=[\"'][^\"]*icon[^\"']*[\"']".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private val hrefRegEx = "href=[\"']([^\"']*)[\"']".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))

    fun getFaviconFromHtml(body: String): String {
        linkRegEx.findAll(body).forEach { matchLink ->
            matchLink.groupValues.forEach { matchLinkSub ->
                if (relRegEx.containsMatchIn(matchLinkSub)) {
                    val matchHrefWithIcon = hrefRegEx.find(matchLinkSub)
                    if (matchHrefWithIcon?.groupValues?.size == 2) {
                        return matchHrefWithIcon.groupValues[1]
                    }
                }
            }
        }
        return Favicon.defaultIconUrl
    }
}