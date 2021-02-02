package net.kyori.indra.api.model;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface ContinuousIntegration {
  @NonNull String system();

  @NonNull String url();
}
