package de.oglimmer.linky.logic

import de.oglimmer.linky.dao.TagsCrudRepository
import de.oglimmer.linky.entity.Link
import de.oglimmer.linky.entity.Tags
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.util.*

@Service
@Transactional
class TagsService(private val repository: TagsCrudRepository) {

    fun loadAll(userId: String): Mono<Tags> = repository.findByUserid(userId)

    fun create(userId: String): Mono<Tags> = repository.save(
        Tags(
            id = UUID.randomUUID().toString(),
            children = mapOf("portal" to emptyMap<String, Any>(), "all" to emptyMap()),
            userid = userId
        )
    )

    fun processSavedLink(userId: String, savedLink: Link) = repository.findByUserid(userId)
        .defaultIfEmpty(
            Tags(
                id = generateId(),
                children = mapOf("portal" to emptyMap<String, Any>(), "all" to emptyMap()),
                userid = userId
            )
        )
        .map { addMissingTags(it, savedLink) }
        .flatMap { repository.save(it) }
        .subscribe()

    private fun addMissingTags(tags: Tags, savedLink: Link): Tags {
        val copyTag = tags.copy(children = tags.children.toMutableMap())
        savedLink.tags
            .filter { usedTagInLink -> !found(tags.children, usedTagInLink) }
            .forEach { missingTag ->
                (copyTag.children as MutableMap)[missingTag] = mapOf<String, Any>()
            }
        return copyTag
    }


    @Suppress("UNCHECKED_CAST")
    private fun found(tags: Map<String, Any>, usedTagInLink: String): Boolean = tags.containsKey(usedTagInLink) ||
            tags.any { it.value is Map<*, *> && found(it.value as Map<String, Any>, usedTagInLink) }


    private fun generateId(): String = UUID.randomUUID().toString()

}

