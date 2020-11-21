package de.oglimmer.linky.logic

import de.oglimmer.linky.dao.TagsCrudRepository
import de.oglimmer.linky.entity.Tags
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.util.*

@Service
@Transactional
class TagsService(private var repository: TagsCrudRepository) {

    fun loadAll(subject: String): Mono<Tags> = repository.findByUserid(subject)

    fun create(subject: String): Mono<Tags> = repository.save(Tags(id = UUID.randomUUID().toString(),
            children = mapOf("portal" to emptyMap<String, Any>(), "all" to emptyMap()),
            userid = subject))

}

