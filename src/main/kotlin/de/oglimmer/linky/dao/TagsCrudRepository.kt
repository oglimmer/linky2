package de.oglimmer.linky.dao

import de.oglimmer.linky.entity.Tags
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface TagsCrudRepository : ReactiveCrudRepository<Tags?, String?> {

    fun findByUserid(userId: String): Mono<Tags>

}