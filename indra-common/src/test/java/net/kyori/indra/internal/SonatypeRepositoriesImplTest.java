package net.kyori.indra.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SonatypeRepositoriesImplTest {

  @Test
  void testFormatHost() {
    final String expected = "https://s01.oss.sonatype.org/content/repositories/snapshots/";
    assertEquals(expected, SonatypeRepositoriesImpl.formatOssHost(1));
  }

  @Test
  void testZeroInvalid() {
    assertThrows(IllegalArgumentException.class, () -> SonatypeRepositoriesImpl.formatOssHost(0));
  }
}
