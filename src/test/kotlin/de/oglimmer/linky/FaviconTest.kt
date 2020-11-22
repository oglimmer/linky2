package de.oglimmer.linky

import de.oglimmer.linky.logic.Favicon
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier

@SpringBootTest
class FaviconTest(@Autowired val favicon: Favicon) {

    @Test
    fun simple() {
        StepVerifier
                .create(favicon.loadFavicon("https://geizhals.de/"))
                .expectNext("https://geizhals.de/favicon.ico")
                .verifyComplete()
    }

}