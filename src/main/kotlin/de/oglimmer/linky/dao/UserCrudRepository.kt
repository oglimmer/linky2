package de.oglimmer.linky.dao

import de.oglimmer.linky.entity.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface UserCrudRepository : ReactiveCrudRepository<User?, String?> {

    fun findByEmail(email: String): Mono<User>

}