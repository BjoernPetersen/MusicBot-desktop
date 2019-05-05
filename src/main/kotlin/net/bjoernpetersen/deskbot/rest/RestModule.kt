package net.bjoernpetersen.deskbot.rest

import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.vertx.core.Vertx
import javax.inject.Singleton

class RestModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideVertx(): Vertx = Vertx.vertx()
}
