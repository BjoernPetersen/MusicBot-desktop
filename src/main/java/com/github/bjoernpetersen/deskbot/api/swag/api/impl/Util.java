package com.github.bjoernpetersen.deskbot.api.swag.api.impl;

import com.github.bjoernpetersen.deskbot.api.swag.model.PlayerState;
import com.github.bjoernpetersen.deskbot.api.swag.model.Queue;
import com.github.bjoernpetersen.deskbot.api.swag.model.Song;
import com.github.bjoernpetersen.jmusicbot.ProviderManager;
import com.github.bjoernpetersen.jmusicbot.provider.NoSuchSongException;
import com.github.bjoernpetersen.jmusicbot.provider.Provider;
import com.github.bjoernpetersen.jmusicbot.provider.Suggester;
import java.util.List;
import java.util.Optional;

final class Util {

  public static Song convert(com.github.bjoernpetersen.jmusicbot.Song song) {
    Song result = new Song();
    result.setId(song.getId());
    result.setProviderId(song.getProviderName());
    result.setTitle(song.getTitle());
    result.setDescription(song.getDescription());
    return result;
  }

  public static Queue convert(List<com.github.bjoernpetersen.jmusicbot.Song> queue) {
    Queue result = new Queue();
    queue.stream().map(Util::convert)
        .forEach(result::add);
    return result;
  }

  public static PlayerState convert(
      com.github.bjoernpetersen.jmusicbot.playback.PlayerState playerState) {
    PlayerState result = new PlayerState();
    result.setState(PlayerState.StateEnum.valueOf(playerState.getState().name()));
    result.setSong(playerState.getSong().map(
        Util::convert).orElse(null));
    return result;
  }

  public static Optional<Provider> lookupProvider(ProviderManager manager, String providerId) {
    try {
      return Optional.of(manager.getProvider(providerId));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }


  public static Optional<Suggester> lookupSuggester(ProviderManager manager, String suggesterId) {
    try {
      return Optional.of(manager.getSuggester(suggesterId));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static Optional<com.github.bjoernpetersen.jmusicbot.Song> lookupSong(
      ProviderManager manager, String songId, String providerId) {
    return lookupProvider(manager, providerId)
        .map(provider -> {
          try {
            return provider.lookup(songId);
          } catch (NoSuchSongException e) {
            return null;
          }
        });
  }

}
