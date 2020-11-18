package de.oglimmer.linky

import de.oglimmer.linky.logic.Favicon
import org.junit.jupiter.api.Test

class FaviconTest {

    @Test
    fun simple() {
        Favicon.loadFavicon("https://geizhals.de/").subscribe { println("DONE!!!${it}") }
        Thread.sleep(5000)
    }

}