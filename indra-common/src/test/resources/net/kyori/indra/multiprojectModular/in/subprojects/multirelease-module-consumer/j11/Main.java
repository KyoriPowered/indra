package testproject.consumer.multirelease;

import testproject.core.InformationProvider;

public class Main {
  public static void main(final String[] args) {
    System.out.println(InformationProvider.name() + " (on Java 11+!)");
  }
}