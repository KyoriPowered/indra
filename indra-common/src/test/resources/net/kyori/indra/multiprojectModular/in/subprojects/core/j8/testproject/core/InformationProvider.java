package testproject.core;

import org.jetbrains.annotations.NotNull;

public class InformationProvider {
  public @NotNull static String name() {
    return "Boots";
  }

  public static int age() {
    return 7;
  }

  private InformationProvider() {
  }
}