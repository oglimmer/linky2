package de.oglimmer.linky

import org.testcontainers.containers.GenericContainer

class MongoDBContainer(image: String) : GenericContainer<MongoDBContainer>(image) {

    companion object {
        private const val PORT = 27017
    }

    val uri: String
        get() {
            val ip = this.containerIpAddress
            val port = this.getMappedPort(PORT)
            return "mongodb://$ip:$port/test"
        }

    init {
        addExposedPort(PORT)
    }

    override fun stop() {
        // JVM should handle shutdown
    }

}