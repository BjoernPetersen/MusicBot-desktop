package com.github.bjoernpetersen.deskbot.model;

import com.github.bjoernpetersen.jmusicbot.MusicBot;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BotHolder {

  @Nonnull
  private static final BotHolder INSTANCE = new BotHolder();

  @Nonnull
  public static BotHolder getInstance() {
    return INSTANCE;
  }

  @Nonnull
  private final ObjectProperty<MusicBot> musicBot;

  private BotHolder() {
    musicBot = new SimpleObjectProperty<>();
  }

  @Nonnull
  public ReadOnlyObjectProperty<MusicBot> botProperty() {
    return musicBot;
  }

  public void set(@Nullable MusicBot musicBot) {
    this.musicBot.set(musicBot);
  }

  public boolean hasValue() {
    return getNullable() != null;
  }

  @Nullable
  public MusicBot getNullable() {
    return this.musicBot.get();
  }

  @Nonnull
  public MusicBot getValue() {
    MusicBot bot = getNullable();
    if (bot == null) {
      throw new IllegalStateException();
    }
    return bot;
  }

  @Nonnull
  public Optional<MusicBot> get() {
    return Optional.ofNullable(getNullable());
  }
}
