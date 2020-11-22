package de.oglimmer.linky

import de.oglimmer.linky.entity.User
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import reactor.test.StepVerifier
import java.util.*

class MongoDBContainerTest(@Autowired private var mongo: ReactiveMongoTemplate) : AbstractMongoIntegrationTest() {

    @Test
    fun testConnection() {
        val uuid = UUID.randomUUID().toString()
        val user = User(id = uuid, email = "direct@nodomain.com", password = "fake")
        StepVerifier
                .create(mongo.save(user).flatMapMany { mongo.findById(uuid, user.javaClass) })
                .expectNext(user)
                .verifyComplete()
    }

}
