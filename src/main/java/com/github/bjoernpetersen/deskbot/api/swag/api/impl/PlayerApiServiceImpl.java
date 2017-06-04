package com.github.bjoernpetersen.deskbot.api.swag.api.impl;

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException;
import com.github.bjoernpetersen.deskbot.api.swag.api.PlayerApiService;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.ProviderManager;
import com.github.bjoernpetersen.jmusicbot.Song;
import com.github.bjoernpetersen.jmusicbot.playback.Player;
import com.github.bjoernpetersen.jmusicbot.user.InvalidTokenException;
import com.github.bjoernpetersen.jmusicbot.user.Permission;
import com.github.bjoernpetersen.jmusicbot.user.User;
import com.github.bjoernpetersen.jmusicbot.user.UserManager;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

public class PlayerApiServiceImpl extends PlayerApiService {

  private ProviderManager providerManager;
  private Player player;
  private UserManager userManager;

  @Override
  public Response dequeue(String authorization, @NotNull String songId, @NotNull String providerId,
      SecurityContext securityContext) throws NotFoundException {
    User user;
    try {
      user = userManager.fromToken(authorization);
    } catch (InvalidTokenException e) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    if (!user.getPermissions().contains(Permission.SKIP)) {
      return Response.status(Status.FORBIDDEN).build();
    }

    Optional<Song> songOptional = Util
        .lookupSong(providerManager, songId, providerId);
    if (songOptional.isPresent()) {
      player.getQueue().remove(songOptional.get());
      return getQueue(securityContext);
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @Override
  public Response enqueue(@NotNull String songId, @NotNull String providerId,
      SecurityContext securityContext) throws NotFoundException {
    Optional<Song> songOptional = Util
        .lookupSong(providerManager, songId, providerId);
    if (songOptional.isPresent()) {
      player.getQueue().append(songOptional.get());
      return getQueue(securityContext);
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @Override
  public Response getPlayerState(SecurityContext securityContext) throws NotFoundException {
    return Response
        .ok(Util.convert(player.getState()))
        .build();
  }

  @Override
  public Response getQueue(SecurityContext securityContext) throws NotFoundException {
    return Response.ok(Util.convert(player.getQueue().toList())).build();
  }

  @Override
  public Response nextSong(String authorization, SecurityContext securityContext)
      throws NotFoundException {
    User user;
    try {
      user = userManager.fromToken(authorization);
    } catch (InvalidTokenException e) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    if (!user.getPermissions().contains(Permission.SKIP)) {
      return Response.status(Status.FORBIDDEN).build();
    }

    try {
      player.next();
    } catch (InterruptedException e) {
      return Response.serverError().entity("Interrupted").build();
    }

    return getPlayerState(securityContext);
  }

  @Override
  public Response pausePlayer(SecurityContext securityContext) throws NotFoundException {
    player.pause();
    return getPlayerState(securityContext);
  }

  @Override
  public Response resumePlayer(SecurityContext securityContext) throws NotFoundException {
    player.play();
    return getPlayerState(securityContext);
  }

  @Override
  public void initialize(MusicBot bot) {
    this.providerManager = bot.getProviderManager();
    this.player = bot.getPlayer();
    this.userManager = bot.getUserManager();
  }
}
