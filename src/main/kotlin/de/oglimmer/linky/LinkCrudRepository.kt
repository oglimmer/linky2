package de.oglimmer.linky

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@Repository
interface LinkCrudRepository : ReactiveCrudRepository<Link?, String?> {

    fun findByUserid(userId: String): Flux<Link>

    fun findByIdAndUserid(id: String, userId: String): Mono<Link>

}