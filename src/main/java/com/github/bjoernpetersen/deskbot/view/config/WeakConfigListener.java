package com.github.bjoernpetersen.deskbot.view.config;


import com.github.bjoernpetersen.jmusicbot.config.Config;
import com.github.bjoernpetersen.jmusicbot.config.ConfigListener;
import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;

public final class WeakConfigListener implements ConfigListener<String> {

  @Nonnull
  private final Config.ReadOnlyStringEntry entry;
  @Nonnull
  private final WeakReference<ConfigListener<String>> wrappedRef;

  public WeakConfigListener(@Nonnull Config.ReadOnlyStringEntry entry, @Nonnull ConfigListener<String> wrapped) {
    this.entry = entry;
    this.wrappedRef = new WeakReference<>(wrapped);
  }

  @Override
  public void onChange(String oldValue, String newValue) {
    ConfigListener<String> wrapped = wrappedRef.get();
    if (wrapped == null) {
      entry.removeListener(this);
    } else {
      wrapped.onChange(oldValue, newValue);
    }
  }
}
