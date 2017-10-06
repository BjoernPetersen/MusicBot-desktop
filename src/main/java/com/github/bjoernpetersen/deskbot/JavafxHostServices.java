package com.github.bjoernpetersen.deskbot;

import com.github.bjoernpetersen.jmusicbot.platform.ContextSupplier;
import com.github.bjoernpetersen.jmusicbot.platform.HostServices;
import java.net.URL;
import org.jetbrains.annotations.NotNull;

public class JavafxHostServices implements HostServices {

  @Override
  public void openBrowser(URL url) {
    DeskBot.getInstance().getHostServices().showDocument(url.toExternalForm());
  }

  @NotNull
  @Override
  public ContextSupplier contextSupplier() throws IllegalStateException {
    throw new IllegalStateException("Context not available on Desktop systems");
  }
}
