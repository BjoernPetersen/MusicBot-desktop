package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.BotService;
import com.github.bjoernpetersen.deskbot.api.swag.model.QueueEntry;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class PlayerApiService implements BotService {

  public abstract Response dequeue(String authorization, @NotNull String songId,
      @NotNull String providerId, SecurityContext securityContext) throws NotFoundException;

  public abstract Response enqueue(String authorization, @NotNull String songId,
      @NotNull String providerId, SecurityContext securityContext) throws NotFoundException;

  public abstract Response getPlayerState(SecurityContext securityContext) throws NotFoundException;

  public abstract Response getQueue(SecurityContext securityContext) throws NotFoundException;

  public abstract Response moveEntry(String authorization, @NotNull @Min(0) Integer index,
      QueueEntry entry, SecurityContext securityContext) throws NotFoundException;

  public abstract Response nextSong(String authorization, SecurityContext securityContext)
      throws NotFoundException;

  public abstract Response pausePlayer(SecurityContext securityContext) throws NotFoundException;

  public abstract Response resumePlayer(SecurityContext securityContext) throws NotFoundException;
}
