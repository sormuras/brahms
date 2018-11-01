package integration.resource;

import de.sormuras.brahms.resource.ResourceSupplier;

abstract class AbstractStringBuilderResource implements ResourceSupplier<StringBuilder> {

  private final StringBuilder instance;

  AbstractStringBuilderResource(StringBuilder instance) {
    this.instance = instance;
    log(">> created");
  }

  @Override
  public void close() {
    ResourceSupplier.super.close();
    log("<< closed");
  }

  @Override
  public StringBuilder get() {
    return instance;
  }

  private void log(String message) {
    StringBuilder builder = get();
    String identity = "0x" + Integer.toHexString(System.identityHashCode(builder)).toUpperCase();
    System.out.println(identity + ": " + message);
  }
}
