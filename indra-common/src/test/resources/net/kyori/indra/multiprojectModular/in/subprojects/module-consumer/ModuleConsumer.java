package testproject.consumer;

import testproject.core.InformationProvider;

public class ModuleConsumer {
  public static void main(final String[] args) {
    System.out.println("The cat is " + InformationProvider.age() + " years old!");
  }
}
