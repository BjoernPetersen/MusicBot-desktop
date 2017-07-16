package com.github.bjoernpetersen.deskbot.api;

import com.github.bjoernpetersen.jmusicbot.MusicBot;
import javax.annotation.Nonnull;

public interface BotService {

  void initialize(@Nonnull MusicBot bot);
}
