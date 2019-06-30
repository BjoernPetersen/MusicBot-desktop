package net.bjoernpetersen.deskbot.impl.image

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.util.pipeline.PipelineContext
import net.bjoernpetersen.musicbot.spi.image.ImageCache
import net.bjoernpetersen.musicbot.spi.image.ImageData
import net.bjoernpetersen.musicbot.spi.image.ImageServerConstraints
import java.util.Base64
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageServer @Inject private constructor(
    private val imageCache: ImageCache
) {
    private lateinit var application: ApplicationEngine
    fun start() {
        application = embeddedServer(CIO, port = ImageServerConstraints.PORT) {
            routing {
                get("${ImageServerConstraints.LOCAL_PATH}/{providerId}/{songId}") {
                    val providerId = call.parameters["providerId"]!!.decode()
                    val songId = call.parameters["songId"]!!.decode()
                    val image = imageCache.getLocal(providerId, songId)
                    respondImage(image)
                }
                get("${ImageServerConstraints.REMOTE_PATH}/{url}") {
                    val url = call.parameters["url"]!!.decode()
                    val image = imageCache.getRemote(url)
                    respondImage(image)
                }
            }
        }
        application.start()
    }

    fun close() {
        application.stop(1, 2, TimeUnit.SECONDS)
    }

    private companion object {
        private val decoder = Base64.getDecoder()
        fun String.decode(): String {
            return String(decoder.decode(toByteArray()), Charsets.UTF_8)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.respondImage(image: ImageData?) {
    if (image == null) {
        call.respond(HttpStatusCode.NotFound)
    } else {
        call.respondBytes(image.data, ContentType.parse(image.type))
    }
}
