package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.rest.Status
import net.bjoernpetersen.deskbot.rest.setStatus
import net.bjoernpetersen.musicbot.api.image.ImageServerConstraints
import net.bjoernpetersen.musicbot.spi.image.ImageCache
import net.bjoernpetersen.musicbot.spi.image.ImageData
import java.util.Base64
import javax.inject.Inject

class ImageHandler @Inject private constructor(
    private val imageCache: ImageCache
) {
    private val logger = KotlinLogging.logger { }
    fun register(router: Router) {
        router.route(HttpMethod.GET, "${ImageServerConstraints.LOCAL_PATH}/:providerId/:songId")
            .handler(::getLocalImage)
        router.route(HttpMethod.GET, "${ImageServerConstraints.REMOTE_PATH}/:remoteUrl")
            .handler(::getRemoteImage)
    }

    private fun getLocalImage(ctx: RoutingContext) {
        val providerId by ctx.pathParams()
        val songId by ctx.pathParams()
        val image = imageCache.getLocal(providerId.decode(), songId.decode())
        ctx.sendImage(image)
    }

    private fun getRemoteImage(ctx: RoutingContext) {
        val remoteUrl by ctx.pathParams()
        val image = imageCache.getRemote(remoteUrl.decode())
        ctx.sendImage(image)
    }
}

private fun RoutingContext.sendImage(image: ImageData?) {
    if (image == null) {
        response()
            .setStatus(Status.NOT_FOUND)
            .end()
    } else {
        response()
            .putHeader("Content-Type", image.type)
            .end(Buffer.buffer(image.data))
    }
}

private val decoder = Base64.getDecoder()
private fun String.decode(): String {
    return String(decoder.decode(toByteArray()), Charsets.UTF_8)
}
