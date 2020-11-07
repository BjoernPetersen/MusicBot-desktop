package net.bjoernpetersen.deskbot.impl

import com.google.inject.AbstractModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readBytes
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.bjoernpetersen.musicbot.spi.image.ImageData
import net.bjoernpetersen.musicbot.spi.image.ImageLoader
import javax.inject.Inject

class ImageLoaderImpl @Inject private constructor() : ImageLoader {
    private val logger = KotlinLogging.logger {}

    override fun get(url: String): ImageData? {
        return runBlocking {
            HttpClient(OkHttp).use { client ->
                client.get<HttpResponse>(url).use { response ->
                    if (!response.status.isSuccess()) null
                    else {
                        val bytes = response.readBytes()
                        val type = response.contentType()?.toString()
                        if (type == null) {
                            logger.warn { "Didn't get content type for image!" }
                            null
                        } else ImageData(type, bytes)
                    }
                }
            }
        }
    }

    companion object : AbstractModule() {
        override fun configure() {
            bind(ImageLoader::class.java).to(ImageLoaderImpl::class.java)
        }
    }
}
