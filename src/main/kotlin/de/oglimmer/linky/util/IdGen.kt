package de.oglimmer.linky.util

import java.util.*

class IdGen {

    companion object {
        fun generateId(): String = UUID.randomUUID().toString()
    }

}