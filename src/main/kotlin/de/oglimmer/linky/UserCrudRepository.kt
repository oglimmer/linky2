package de.oglimmer.linky

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono


@Repository
interface UserCrudRepository : ReactiveCrudRepository<User?, String?> {
    fun findByEmail(email: String): Mono<User>
}