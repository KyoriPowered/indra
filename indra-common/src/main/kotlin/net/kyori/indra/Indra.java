package net.kyori.indra;

import java.util.HashSet;
import java.util.Set;

public class Indra {
  public static final String EXTENSION_NAME = "indra";
  public static final String PUBLICATION_NAME = "maven";

  public static final Set<String> SOURCE_FILES = sourceFiles();

  private static Set<String> sourceFiles() {
    final Set<String> sourceFiles = new HashSet<>();
    sourceFiles.add( "**/*.groovy");
    sourceFiles.add( "**/*.java");
    sourceFiles.add( "**/*.kt");
    sourceFiles.add( "**/*.scala");
    return sourceFiles;
  }
}
