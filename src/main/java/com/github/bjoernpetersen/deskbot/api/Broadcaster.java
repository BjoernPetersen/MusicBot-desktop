package com.github.bjoernpetersen.deskbot.api;

import com.github.bjoernpetersen.jmusicbot.InitializationException;
import com.github.bjoernpetersen.jmusicbot.Loggable;
import com.github.bjoernpetersen.jmusicbot.NamedThreadFactory;
import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class Broadcaster implements Loggable, Closeable {

  private final int port;
  private final byte[] message;
  private final InetAddress groupAddress;
  private final ScheduledExecutorService scheduler;
  private final DatagramSocket socket;

  public Broadcaster(int port, String groupAddress, String message) throws InitializationException {
    this.port = port;
    this.message = message.getBytes(StandardCharsets.UTF_8);
    try {
      this.groupAddress = InetAddress.getByName(groupAddress);
    } catch (UnknownHostException e) {
      throw new InitializationException(e);
    }
    ThreadFactory threadFactory = new NamedThreadFactory("UDP IP broadcaster", true);
    this.scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);

    try {
      this.socket = new DatagramSocket();
      this.socket.setBroadcast(true);
    } catch (SocketException e) {
      throw new InitializationException(e);
    }

    scheduler.scheduleWithFixedDelay(this::broadcast, 1, 2, TimeUnit.SECONDS);
  }

  private void broadcast() {
    DatagramPacket packet = createPacket();
    try {
      socket.send(packet);
    } catch (IOException e) {
      logInfo("Could not broadcast state packet", e);
    }
  }

  @Nonnull
  private DatagramPacket createPacket() {
    return new DatagramPacket(message, message.length, groupAddress, port);
  }

  @Override
  public void close() throws IOException {
    scheduler.shutdown();
    try {
      scheduler.awaitTermination(500, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new IOException(e);
    }

    socket.close();
  }
}
