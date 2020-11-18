package de.oglimmer.linky.conf

import org.springframework.data.mongodb.repository.config.EnableMongoRepositories


//@EnableReactiveMongoRepositories
//class MongoConfiguration : AbstractReactiveMongoConfiguration() {
//    @Bean
//    fun mongoClient(): MongoClient {
//        return MongoClients.create()
//    }
//
//    override fun getDatabaseName(): String {
//        return "reactive"
//    }
//}

@EnableMongoRepositories(basePackages = ["de.oglimmer.linky.dao"])
class MongoConfiguration
