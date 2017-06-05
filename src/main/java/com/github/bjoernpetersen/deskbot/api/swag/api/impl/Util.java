package com.github.bjoernpetersen.deskbot.api.swag.api.impl;

import com.github.bjoernpetersen.deskbot.api.swag.model.PlayerState;
import com.github.bjoernpetersen.deskbot.api.swag.model.Queue;
import com.github.bjoernpetersen.deskbot.api.swag.model.QueueEntry;
import com.github.bjoernpetersen.deskbot.api.swag.model.Song;
import com.github.bjoernpetersen.deskbot.api.swag.model.SongEntry;
import com.github.bjoernpetersen.jmusicbot.ProviderManager;
import com.github.bjoernpetersen.jmusicbot.playback.Queue.Entry;
import com.github.bjoernpetersen.jmusicbot.provider.NoSuchSongException;
import com.github.bjoernpetersen.jmusicbot.provider.Provider;
import com.github.bjoernpetersen.jmusicbot.provider.Suggester;
import com.github.bjoernpetersen.jmusicbot.user.User;
import java.util.List;
import java.util.Optional;

final class Util {

  public static Song convert(com.github.bjoernpetersen.jmusicbot.Song song) {
    Song result = new Song();
    result.setId(song.getId());
    result.setProviderId(song.getProviderName());
    result.setTitle(song.getTitle());
    result.setDescription(song.getDescription());
    result.setAlbumArtUrl(song.getAlbumArtUrl().orElse(null));
    return result;
  }

  public static QueueEntry convert(Entry entry) {
    QueueEntry queueEntry = new QueueEntry();
    queueEntry.setUserName(entry.getUser().getName());
    queueEntry.setSong(convert(entry.getSong()));
    return queueEntry;
  }

  public static SongEntry convert(com.github.bjoernpetersen.jmusicbot.playback.SongEntry entry) {
    Song song = convert(entry.getSong());
    User user = entry.getUser();
    String userName = user == null ? null : user.getName();
    SongEntry queueEntry = new SongEntry();
    queueEntry.setUserName(userName);
    queueEntry.setSong(song);
    return queueEntry;
  }

  public static Queue convert(List<Entry> queue) {
    Queue result = new Queue();
    queue.stream()
        .map(Util::convert)
        .forEach(result::add);
    return result;
  }

  public static PlayerState convert(
      com.github.bjoernpetersen.jmusicbot.playback.PlayerState playerState) {
    PlayerState result = new PlayerState();
    result.setState(PlayerState.StateEnum.valueOf(playerState.getState().name()));
    result.setSongEntry(playerState.getEntry().map(Util::convert).orElse(null));
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
