package producer;

import java.util.Arrays;
import java.util.List;

/**
 * Values exposed by documentation.
 */
public final class Values {
  /**
   * Get a list of known fish.
   * 
   * @return known fish
   * @since 1.0.0
   */
  public static List<String> fish() {
    return Arrays.asList("one", "two", "red", "blue");
  }
}