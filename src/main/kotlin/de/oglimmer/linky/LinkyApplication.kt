package de.oglimmer.linky


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories


@SpringBootApplication
@EnableMongoRepositories(basePackages = ["de.oglimmer.linky"])
class LinkyApplication


fun main(args: Array<String>) {
    runApplication<LinkyApplication>(*args)
}