package de.oglimmer.linky

import de.oglimmer.linky.logic.Favicon
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class FaviconTest(@Autowired val favicon: Favicon) {

    @Test
    fun simple() {
        favicon.loadFavicon("https://geizhals.de/").subscribe { println("DONE!!!${it}") }
        Thread.sleep(5000)
    }

}