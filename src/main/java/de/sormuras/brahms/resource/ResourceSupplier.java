package de.sormuras.brahms.resource;

import java.util.function.Supplier;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

public interface ResourceSupplier<R> extends AutoCloseable, CloseableResource, Supplier<R> {

  @Override
  default void close() {
    R instance = get();
    if (instance instanceof AutoCloseable) {
      try {
        ((AutoCloseable) instance).close();
      } catch (Exception e) {
        // ignore
      }
    }
  }
}
