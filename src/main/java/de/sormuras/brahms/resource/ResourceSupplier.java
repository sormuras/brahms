package de.sormuras.brahms.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

public interface ResourceSupplier<R> extends AutoCloseable, CloseableResource, Supplier<R> {

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
  @interface New {

    Class<? extends ResourceSupplier<?>> value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  @interface Shared {

    Class<? extends ResourceSupplier<?>> value();

    String key();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  @interface Singleton {

    Class<? extends ResourceSupplier<?>> value();
  }

  default Object as(Class<?> parameterType) {
    // TODO find unique converter, like String Object#toString() or File Path#toFile()?
    throw new UnsupportedOperationException("Can't convert to " + parameterType);
  }

  @Override
  default void close() {
    R instance = get();
    if (instance instanceof AutoCloseable) {
      try {
        ((AutoCloseable) instance).close();
      } catch (Exception e) {
        // TODO better exception handling by reporting or re-throwing?
        throw new RuntimeException("closing failed: " + instance, e);
      }
    }
  }
}
