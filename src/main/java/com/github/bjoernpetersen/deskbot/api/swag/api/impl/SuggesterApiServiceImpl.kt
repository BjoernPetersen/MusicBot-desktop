package com.github.bjoernpetersen.deskbot.api.swag.api.impl

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException
import com.github.bjoernpetersen.deskbot.api.swag.api.SuggesterApiService
import com.github.bjoernpetersen.jmusicbot.MusicBot
import com.github.bjoernpetersen.jmusicbot.provider.NoSuchSongException
import com.github.bjoernpetersen.jmusicbot.provider.ProviderManager
import com.github.bjoernpetersen.jmusicbot.provider.Suggester
import com.github.bjoernpetersen.jmusicbot.user.InvalidTokenException
import com.github.bjoernpetersen.jmusicbot.user.Permission
import com.github.bjoernpetersen.jmusicbot.user.User
import com.github.bjoernpetersen.jmusicbot.user.UserManager
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext


class SuggesterApiServiceImpl : SuggesterApiService() {
    private lateinit var userManager: UserManager
    private lateinit var providerManager: ProviderManager

    override fun initialize(bot: MusicBot) {
        userManager = bot.userManager
        providerManager = bot.providerManager
    }


    @Throws(NotFoundException::class)
    override fun getSuggesters(securityContext: SecurityContext): Response =
            Response.ok(providerManager.suggesters.convert()).build()


    @Throws(NotFoundException::class)
    override fun suggestSong(suggesterId: String,
                             max: Int?,
                             securityContext: SecurityContext): Response {
        val suggester = lookupSuggester(providerManager, suggesterId)
        if (suggester != null) {
            val maxSuggestions = if (max == null || max < 1 || max > 64) 16 else max
            val suggestions = suggester.getNextSuggestions(maxSuggestions).map { it.convert() }
            return Response.ok(suggestions).build()
        } else {
            return Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @Throws(NotFoundException::class)
    override fun removeSuggestion(suggesterId: String, authorization: String, songId: String,
                                  providerId: String, securityContext: SecurityContext): Response {
        val user: User
        try {
            user = userManager.fromToken(authorization)
        } catch (e: InvalidTokenException) {
            return Response.status(Response.Status.UNAUTHORIZED).build()
        }

        if (!user.permissions.contains(Permission.DISLIKE)) {
            return Response.status(Response.Status.FORBIDDEN).build()
        }

        val suggester: Suggester?
        val song: com.github.bjoernpetersen.jmusicbot.Song
        try {
            val provider = providerManager.getProvider(providerId)
            suggester = providerManager.getSuggester(suggesterId)

            if (provider == null || suggester == null) {
                return Response.status(Response.Status.NOT_FOUND).build()
            }

            song = provider.lookup(songId)
        } catch (e: NoSuchSongException) {
            return Response.status(Response.Status.NOT_FOUND).build()
        }

        suggester.removeSuggestion(song)
        return Response.noContent().build()
    }
}
