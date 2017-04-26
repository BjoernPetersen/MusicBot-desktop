package com.github.bjoernpetersen.deskbot.model;

import com.github.bjoernpetersen.jmusicbot.config.ConfigStorageAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class ConfigStorage implements ConfigStorageAdapter {

  @Nonnull
  private static final Logger log = Logger.getLogger(ConfigStorage.class.getName());

  @Nonnull
  private final File configFile;
  @Nonnull
  private final File secretsFile;

  public ConfigStorage(File configFile, File secretsFile) {
    this.configFile = configFile;
    this.secretsFile = secretsFile;
  }

  private Map<String, String> load(File file) {
    log.fine("Loading config from file: " + file.getName());
    if (!file.isFile()) {
      return Collections.emptyMap();
    }

    try {
      Properties properties = new Properties();
      properties.load(new FileInputStream(file));
      return properties.entrySet().stream()
        .collect(Collectors.toMap(
          e -> String.valueOf(e.getKey()),
          e -> String.valueOf(e.getValue())
        ));
    } catch (IOException e) {
      log.severe(
        String.format("Could not load config entries from file '%s': %s", file.getName(), e)
      );
      return Collections.emptyMap();
    }
  }

  private void store(File file, Map<String, String> map) {
    log.fine("Storing config in file: " + file.getName());
    if (!file.exists()) {
      try {
        if (!file.createNewFile()) {
          throw new IOException("Could not create file");
        }
      } catch (IOException e) {
        log.severe(String.format("Could not create config file '%s': %s", file.getName(), e));
        return;
      }
    }

    Properties properties = new Properties();
    properties.putAll(map);
    try {
      properties.store(new FileOutputStream(file, false), null);
    } catch (IOException e) {
      log.severe(
        String.format("Could not store config entries in file '%s': %s", file.getName(), e)
      );
    }
  }

  @Nonnull
  @Override
  public Map<String, String> loadPlaintext() {
    return load(configFile);
  }

  @Override
  public void storePlaintext(Map<String, String> map) {
    store(configFile, map);
  }

  @Nonnull
  @Override
  public Map<String, String> loadSecrets() {
    return load(secretsFile);
  }

  @Override
  public void storeSecrets(Map<String, String> map) {
    store(secretsFile, map);
  }
}
