package de.oglimmer.linky.logic

import de.oglimmer.linky.dao.TagsCrudRepository
import de.oglimmer.linky.entity.Link
import de.oglimmer.linky.entity.Tags
import de.oglimmer.linky.util.IdGen
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
            id = IdGen.generateId(),
            children = mapOf("portal" to emptyMap<String, Any>(), "all" to emptyMap()),
            userid = userId
        )
    )

    fun processSavedLink(savedLink: Link) = repository.findByUserid(savedLink.userid)
        .defaultIfEmpty(
            Tags(
                id = IdGen.generateId(),
                children = mapOf("portal" to emptyMap<String, Any>(), "all" to emptyMap()),
                userid = savedLink.userid
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


}

