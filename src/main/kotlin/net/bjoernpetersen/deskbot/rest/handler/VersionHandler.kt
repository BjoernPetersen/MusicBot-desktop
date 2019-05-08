package net.bjoernpetersen.deskbot.rest.handler

import com.github.zafarkhaja.semver.ParseException
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.async
import net.bjoernpetersen.deskbot.rest.end
import net.bjoernpetersen.deskbot.rest.model.ImplementationInfo
import net.bjoernpetersen.deskbot.rest.model.VersionInfo
import java.io.IOException
import java.util.Properties
import javax.inject.Inject

// TODO: This should definitely be automatically determined
private const val API_VERSION = "0.12.0"
private const val PROJECT_PAGE = "https://github.com/BjoernPetersen/MusicBot-desktop"
private const val PROJECT_NAME = "DeskBot"

class VersionHandler @Inject private constructor() : HandlerController {

    private val versionInfo: VersionInfo by lazy { loadInfo() }

    override suspend fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("getVersion", ::getVersion)
    }

    private fun getVersion(ctx: RoutingContext) {
        ctx.async {
            versionInfo
        } success {
            ctx.response().end(versionInfo)
        } failure {
            ctx.fail(it)
        }
    }

    private companion object {
        fun loadInfo(): VersionInfo {
            val implVersion = loadImplementationVersion()
            return VersionInfo(
                API_VERSION, ImplementationInfo(
                    PROJECT_PAGE,
                    PROJECT_NAME,
                    implVersion
                )
            )
        }

        private fun loadImplementationVersion() = try {
            val properties = Properties()
            VersionHandler::class.java
                .getResourceAsStream("/net/bjoernpetersen/deskbot/version.properties")
                .use { versionStream -> properties.load(versionStream) }
            properties.getProperty("version") ?: throw IllegalStateException("Version is missing")
        } catch (e: IOException) {
            throw IllegalStateException("Could not read version resource", e)
        } catch (e: ParseException) {
            throw IllegalStateException("Could not read version resource", e)
        }
    }
}
