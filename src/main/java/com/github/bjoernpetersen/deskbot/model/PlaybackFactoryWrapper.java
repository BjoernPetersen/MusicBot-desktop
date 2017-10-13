package com.github.bjoernpetersen.deskbot.model;

import com.github.bjoernpetersen.jmusicbot.IdPlugin;
import com.github.bjoernpetersen.jmusicbot.config.Config;
import com.github.bjoernpetersen.jmusicbot.config.Config.Entry;
import com.github.bjoernpetersen.jmusicbot.platform.Platform;
import com.github.bjoernpetersen.jmusicbot.platform.Support;
import com.github.bjoernpetersen.jmusicbot.playback.PlaybackFactory;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;

public class PlaybackFactoryWrapper implements IdPlugin {

  @Nonnull
  private final PlaybackFactory playbackFactory;

  public PlaybackFactoryWrapper(@Nonnull PlaybackFactory playbackFactory) {
    this.playbackFactory = playbackFactory;
  }

  @Nonnull
  @Override
  public Support getSupport(@Nonnull Platform platform) {
    return playbackFactory.getSupport(platform);
  }

  @Override
  public String getId() {
    return playbackFactory.getClass().getName();
  }

  @Nonnull
  @Override
  public String getReadableName() {
    return playbackFactory.getClass().getSimpleName();
  }

  @Nonnull
  @Override
  public List<? extends Entry> initializeConfigEntries(@Nonnull Config config) {
    return playbackFactory.initializeConfigEntries(config);
  }

  @Nonnull
  @Override
  public List<? extends Entry> getMissingConfigEntries() {
    return playbackFactory.getMissingConfigEntries();
  }

  @Override
  public void destructConfigEntries() {
    playbackFactory.destructConfigEntries();
  }

  @Override
  public void close() throws IOException {
    playbackFactory.close();
  }

  @Nonnull
  public PlaybackFactory getWrapped() {
    return playbackFactory;
  }
}
