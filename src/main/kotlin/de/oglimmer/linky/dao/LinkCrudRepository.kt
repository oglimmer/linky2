package de.oglimmer.linky.dao

import de.oglimmer.linky.entity.Link
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface LinkCrudRepository : ReactiveCrudRepository<Link?, String?> {

    fun findByUserid(userId: String): Flux<Link>

    fun findByIdAndUserid(id: String, userId: String): Mono<Link?>

    @Query(value = "{'userid': ?0, 'tags': {\$all : ?1 }}")
    fun findByUserIdAndTags(userId: String, tags: Array<String>): Flux<Link>

}