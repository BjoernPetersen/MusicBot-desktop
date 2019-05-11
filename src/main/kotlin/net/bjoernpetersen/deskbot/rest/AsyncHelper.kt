package net.bjoernpetersen.deskbot.rest

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext

interface AsyncBuilder<T> {
    infix fun success(handler: (T) -> Unit): SuccessAsyncBuilder<T>
    infix fun failure(failure: (Throwable) -> Unit): FailureAsyncBuilder<T>
}

interface SuccessAsyncBuilder<T> {
    infix fun failure(failure: (Throwable) -> Unit)
}

interface FailureAsyncBuilder<T> {
    infix fun success(success: (T) -> Unit)
}

private class AsyncBuilderImpl<T>(
    private val vertx: Vertx,
    private val run: () -> T
) : AsyncBuilder<T> {

    override fun success(handler: (T) -> Unit): SuccessAsyncBuilder<T> =
        SuccessAsyncBuilderImpl(vertx, run, handler)

    override fun failure(failure: (Throwable) -> Unit): FailureAsyncBuilder<T> =
        FailureAsyncBuilderImpl(vertx, run, failure)
}

private class SuccessAsyncBuilderImpl<T>(
    private val vertx: Vertx,
    private val run: () -> T,
    private val success: (T) -> Unit
) : SuccessAsyncBuilder<T> {

    override fun failure(failure: (Throwable) -> Unit) {
        FullAsyncBuilder(vertx, run, success, failure).start()
    }
}

private class FailureAsyncBuilderImpl<T>(
    private val vertx: Vertx,
    private val run: () -> T,
    private val failure: (Throwable) -> Unit
) : FailureAsyncBuilder<T> {

    override fun success(success: (T) -> Unit) {
        FullAsyncBuilder(vertx, run, success, failure).start()
    }
}

private data class FullAsyncBuilder<T>(
    private val vertx: Vertx,
    private val run: () -> T,
    private val success: (T) -> Unit,
    private val failure: (Throwable) -> Unit
) {

    fun start() {
        vertx.executeBlocking({ future: Future<T> ->
            try {
                future.complete(run())
            } catch (e: Throwable) {
                future.fail(e)
            }
        }, {
            if (it.succeeded()) success(it.result())
            else failure(it.cause())
        })
    }
}

fun <T> RoutingContext.async(run: () -> T): AsyncBuilder<T> = vertx().async(run)
fun <T> Vertx.async(run: () -> T): AsyncBuilder<T> = AsyncBuilderImpl(this, run)

fun <T> RoutingContext.async(run: () -> T, success: (T) -> Unit, failure: (Throwable) -> Unit) =
    vertx().async(run, success, failure)

fun <T> Vertx.async(run: () -> T, success: (T) -> Unit, failure: (Throwable) -> Unit) {
    executeBlocking({ future: Future<T> ->
        try {
            future.complete(run())
        } catch (e: Throwable) {
            future.fail(e)
        }
    }, {
        if (it.succeeded()) success(it.result())
        else failure(it.cause())
    })
}
