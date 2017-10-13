package com.github.bjoernpetersen.deskbot.model;

import com.github.bjoernpetersen.jmusicbot.config.ui.Choice;
import com.github.bjoernpetersen.jmusicbot.provider.Suggester;
import javax.annotation.Nonnull;

public class SuggesterChoice implements Choice<String> {

  @Nonnull
  private final String id;
  @Nonnull
  private final String readableName;

  public SuggesterChoice(@Nonnull Suggester suggester) {
    this(suggester.getId(), suggester.getReadableName());
  }

  public SuggesterChoice(@Nonnull String id, @Nonnull String readableName) {
    this.id = id;
    this.readableName = readableName;
  }

  @Override
  @Nonnull
  public String getId() {
    return id;
  }

  @Override
  @Nonnull
  public String getDisplayName() {
    return readableName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SuggesterChoice that = (SuggesterChoice) o;

    if (!id.equals(that.id)) {
      return false;
    }
    return readableName.equals(that.readableName);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + readableName.hashCode();
    return result;
  }
}
