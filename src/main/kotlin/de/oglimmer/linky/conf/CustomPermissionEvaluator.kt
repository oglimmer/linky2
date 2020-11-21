package de.oglimmer.linky.conf


import de.oglimmer.linky.dao.LinkCrudRepository
import de.oglimmer.linky.util.LRUCache
import de.oglimmer.linky.util.PerpetualCache
import mu.KotlinLogging
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.io.Serializable

private val logger = KotlinLogging.logger {}

@Service
class CustomPermissionEvaluator(private val linkCrudRepository: LinkCrudRepository) : PermissionEvaluator {

    // each key-value has less that 100 bytes. max size is 50k elements => 5M in total for this cache
    private val cache = LRUCache(PerpetualCache(), 50_000)

    override fun hasPermission(auth: Authentication?, targetDomainObject: Any?, permission: Any): Boolean =
            if (auth == null || targetDomainObject == null || targetDomainObject !is String)
                false
            else
                hasPrivilege(auth, targetDomainObject)


    override fun hasPermission(auth: Authentication?, targetId: Serializable?, targetType: String?, permission: Any)
            : Boolean =
            if (auth == null || targetId == null || targetId !is String)
                false
            else
                hasPrivilege(auth, targetId)


    private fun hasPrivilege(auth: Authentication, linkId: String): Boolean {
        if (auth.principal !is Jwt) {
            return false
        }
        val userId = (auth.principal as Jwt).subject
        val key = "$userId:$linkId"
        if (cache[key] == null) {
            cache[key] = linkCrudRepository.findByIdAndUserid(id = linkId, userId = userId).blockOptional().isPresent
            logger.debug { "Load permission for $key: ${cache[key]}" }
        }
        return cache[key] == true
    }
}