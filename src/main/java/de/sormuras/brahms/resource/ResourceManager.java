package de.sormuras.brahms.resource;

import static org.junit.platform.commons.support.ReflectionSupport.newInstance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.JUnitException;

public class ResourceManager implements ParameterResolver, BeforeAllCallback {

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface Closeable {}

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface New {

    Class<? extends ResourceSupplier<?>> value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface Singleton {

    Class<? extends ResourceSupplier<?>> value();
  }

  static class InstanceSupplier<R> implements ResourceSupplier<R> {

    private final R instance;

    InstanceSupplier(R instance) {
      this.instance = instance;
    }

    @Override
    public R get() {
      return instance;
    }
  }

  private static final Namespace NAMESPACE = Namespace.create(ResourceManager.class);

  @Override
  public void beforeAll(ExtensionContext extension) throws Exception {
    var testClass = extension.getRequiredTestClass();
    var classStore = extension.getStore(NAMESPACE);
    for (var field : testClass.getDeclaredFields()) {
      if (field.isSynthetic()) {
        continue;
      }
      if (!field.isAnnotationPresent(Closeable.class)) {
        continue;
      }
      if (!Modifier.isStatic(field.getModifiers())) {
        throw new JUnitException("Annotated field must be static: " + field);
      }
      if (field.trySetAccessible()) {
        var instance = field.get(testClass);
        var supplier = new InstanceSupplier<>(instance);
        classStore.getOrComputeIfAbsent(field.toString(), k -> supplier);
      } else {
        throw new JUnitException("Can't make field accessible: " + field);
      }
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameter, ExtensionContext __) {
    return parameter.isAnnotated(New.class) ^ parameter.isAnnotated(Singleton.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameter, ExtensionContext extension) {
    var supplier = supplier(parameter, extension);
    var parameterType = parameter.getParameter().getType();
    if (ResourceSupplier.class.isAssignableFrom(parameterType)) {
      return supplier;
    }
    var instance = supplier.get();
    if (parameterType.isAssignableFrom(instance.getClass())) {
      return instance;
    }
    throw new ParameterResolutionException(
        "Parameter type "
            + parameterType
            + " isn't compatible with "
            + ResourceSupplier.class
            + " nor "
            + instance.getClass());
  }

  private ResourceSupplier<?> supplier(ParameterContext parameter, ExtensionContext context) {
    var newAnnotation = parameter.findAnnotation(New.class);
    if (newAnnotation.isPresent()) {
      var type = newAnnotation.get().value();
      var key = type.getName() + '@' + parameter.getIndex();
      var instance = newInstance(type);
      context.getStore(NAMESPACE).put(key, instance);
      return instance;
    }

    var singletonAnnotation = parameter.findAnnotation(Singleton.class);
    if (singletonAnnotation.isPresent()) {
      var supplier = singletonAnnotation.get().value();
      var key = supplier.getName();
      var instance = context.getStore(NAMESPACE).get(key, ResourceSupplier.class);
      if (instance != null) {
        return instance;
      }
      return context
          .getRoot()
          .getStore(NAMESPACE)
          .getOrComputeIfAbsent(key, k -> newInstance(supplier), ResourceSupplier.class);
    }

    throw new ParameterResolutionException("Can't resolve resource supplier for: " + parameter);
  }
}
