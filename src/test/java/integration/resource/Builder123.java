package integration.resource;

import de.sormuras.brahms.resource.ResourceSupplier;

public class Builder123 implements ResourceSupplier<StringBuilder> {

  private final StringBuilder instance;

  public Builder123() {
    this.instance = new StringBuilder("123");
  }

  @Override
  public void close() {
    ResourceSupplier.super.close();
  }

  @Override
  public StringBuilder get() {
    return instance;
  }

  @Override
  public String toString() {
    return "Builder123[" + identity(this) + ", " + identity(get()) + "]: " + get();
  }

  private static String identity(Object object) {
    return "0x" + Integer.toHexString(System.identityHashCode(object)).toUpperCase();
  }
}
