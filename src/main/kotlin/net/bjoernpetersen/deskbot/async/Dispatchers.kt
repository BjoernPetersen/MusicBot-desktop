package net.bjoernpetersen.deskbot.async

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

fun <T> Sequence<T>.defer(): Sequence<Deferred<T>> {
    return map {
        CompletableDeferred(it)
    }
}

suspend fun <T, U> Sequence<Deferred<T>>.pMap(transform: suspend (T) -> U): Sequence<Deferred<U>> {
    return coroutineScope {
        map {
            async {
                val t = it.await()
                transform(t)
            }
        }
    }
}

suspend fun <T, U> Sequence<Deferred<T?>>.pMapIfPresent(transform: suspend (T) -> U): Sequence<Deferred<U?>> {
    return coroutineScope {
        map {
            async {
                val t = it.await()
                t?.let { transform(t) }
            }
        }
    }
}

suspend fun <T> Sequence<Deferred<T>>.await(): Sequence<T> {
    return map {
        runBlocking {
            it.await()
        }
    }
}

