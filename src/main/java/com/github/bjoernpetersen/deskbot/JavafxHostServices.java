package com.github.bjoernpetersen.deskbot;

import com.github.bjoernpetersen.jmusicbot.platform.ContextSupplier;
import com.github.bjoernpetersen.jmusicbot.platform.HostServices;
import java.net.URL;
import javax.annotation.Nonnull;

public class JavafxHostServices implements HostServices {

  @Override
  public void openBrowser(URL url) {
    DeskBot.getInstance().getHostServices().showDocument(url.toExternalForm());
  }

  @Nonnull
  @Override
  public ContextSupplier contextSupplier() throws IllegalStateException {
    throw new IllegalStateException("Context not available on Desktop systems");
  }
}
