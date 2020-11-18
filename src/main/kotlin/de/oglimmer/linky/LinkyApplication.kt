package de.oglimmer.linky


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class LinkyApplication

fun main(args: Array<String>) {
    runApplication<LinkyApplication>(*args)
}