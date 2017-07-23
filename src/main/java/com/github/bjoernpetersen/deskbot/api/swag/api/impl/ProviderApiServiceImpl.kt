package com.github.bjoernpetersen.deskbot.api.swag.api.impl

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException
import com.github.bjoernpetersen.deskbot.api.swag.api.ProviderApiService
import com.github.bjoernpetersen.jmusicbot.MusicBot
import com.github.bjoernpetersen.jmusicbot.provider.NoSuchSongException
import com.github.bjoernpetersen.jmusicbot.provider.ProviderManager
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext


class ProviderApiServiceImpl : ProviderApiService() {
    private lateinit var manager: ProviderManager

    override fun initialize(bot: MusicBot) {
        manager = bot.providerManager
    }

    @Throws(NotFoundException::class)
    override fun getProviders(securityContext: SecurityContext): Response =
            Response.ok(manager.providers.convert()).build()

    @Throws(NotFoundException::class)
    override fun lookupSong(songId: String, providerId: String,
                            securityContext: SecurityContext): Response {
        val provider = lookupProvider(manager, providerId)
        if (provider == null) {
            return Response.status(Response.Status.NOT_FOUND).build()
        }

        try {
            return Response.ok(provider.lookup(songId).convert()).build()
        } catch (e: NoSuchSongException) {
            return Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @Throws(NotFoundException::class)
    override fun searchSong(providerId: String,
                            query: String,
                            securityContext: SecurityContext): Response {
        val provider = lookupProvider(manager, providerId)
        if (provider == null) {
            return Response.status(Response.Status.NOT_FOUND).build()
        }

        val searchResult = provider.search(query).map { it.convert() }
        return Response.ok(searchResult).build()
    }

}