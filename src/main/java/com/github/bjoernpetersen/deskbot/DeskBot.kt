package com.github.bjoernpetersen.deskbot

import com.github.bjoernpetersen.deskbot.view.ConfigWindow
import tornadofx.*
import java.io.IOException
import java.util.logging.LogManager

class DeskBot : App(ConfigWindow::class) {
  init {
    try {
      resources.stream("/logging.properties").use {
        LogManager.getLogManager().readConfiguration(it)
      }
    } catch (e: IOException) {
      System.out.println("Can't initialize logging!");
      System.exit(1);
    }
  }

  var ignoreOutdated: Boolean = false
    private set
  var noConfig: Boolean = false
    private set

  override fun init() {
    super.init()
    noConfig = parameters.raw.contains("--no-config")
    ignoreOutdated = parameters.raw.contains("--ignore-outdated")
  }
}

fun main(args: Array<String>) = launch<DeskBot>(args)

