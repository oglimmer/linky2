package de.oglimmer.linky.util

// From https://medium.com/@kezhenxu94/how-to-build-your-own-cache-in-kotlin-1b0e86005591

interface Cache {
    val size: Int

    operator fun set(key: Any, value: Any)

    operator fun get(key: Any): Any?

    fun remove(key: Any): Any?

    fun clear()
}

class PerpetualCache : Cache {
    private val cache = HashMap<Any, Any>()

    override val size: Int
        get() = cache.size

    override fun set(key: Any, value: Any) {
        this.cache[key] = value
    }

    override fun remove(key: Any) = this.cache.remove(key)

    override fun get(key: Any) = this.cache[key]

    override fun clear() = this.cache.clear()
}

class LRUCache(private val delegate: Cache, private val maxSize: Int = DEFAULT_SIZE) : Cache {
    private val keyMap = object : LinkedHashMap<Any, Any>(maxSize, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Any, Any>): Boolean {
            val tooManyCachedItems = size > maxSize
            if (tooManyCachedItems) eldestKeyToRemove = eldest.key
            return tooManyCachedItems
        }
    }

    private var eldestKeyToRemove: Any? = null

    override val size: Int
        get() = delegate.size

    override fun set(key: Any, value: Any) {
        delegate[key] = value
        cycleKeyMap(key)
    }

    override fun remove(key: Any) = delegate.remove(key)

    override fun get(key: Any): Any? {
        keyMap[key]
        return delegate[key]
    }

    override fun clear() {
        keyMap.clear()
        delegate.clear()
    }

    private fun cycleKeyMap(key: Any) {
        keyMap[key] = PRESENT
        eldestKeyToRemove?.let { delegate.remove(it) }
        eldestKeyToRemove = null
    }

    companion object {
        private const val DEFAULT_SIZE = 100
        private const val PRESENT = true
    }
}