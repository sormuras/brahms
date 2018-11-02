package de.sormuras.brahms.resource;

import static org.junit.platform.commons.support.AnnotationSupport.findRepeatableAnnotations;
import static org.junit.platform.commons.support.ReflectionSupport.newInstance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class ResourceManager implements ParameterResolver, BeforeAllCallback {

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Repeatable(Resources.class)
  public @interface Resource {

    String id();

    Class<? extends ResourceSupplier<?>> supplier();

    boolean global() default false;
  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Resources {
    Resource[] value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface Put {
    String value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface Get {
    String value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface New {

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
    for (var resource : findRepeatableAnnotations(testClass, Resource.class)) {
      var supplier = resource.supplier();
      var context = resource.global() ? extension.getRoot() : extension;
      context.getStore(NAMESPACE).getOrComputeIfAbsent(resource.id(), k -> newInstance(supplier));
    }
    for (var field : testClass.getDeclaredFields()) {
      var put = field.getAnnotation(Put.class);
      if (put == null) {
        continue;
      }
      if (field.trySetAccessible()) {
        var instance = field.get(testClass);
        var supplier = new InstanceSupplier<>(instance);
        extension.getStore(NAMESPACE).getOrComputeIfAbsent(put.value(), k -> supplier);
      }
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameter, ExtensionContext __) {
    return parameter.isAnnotated(Get.class) ^ parameter.isAnnotated(New.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameter, ExtensionContext extension) {
    var supplier = resolveSupplier(parameter, extension.getStore(NAMESPACE));
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

  private ResourceSupplier<?> resolveSupplier(ParameterContext parameter, Store store) {
    var getResource = parameter.findAnnotation(Get.class);
    if (getResource.isPresent()) {
      var resource = getResource.get();
      var instance = store.get(resource.value(), ResourceSupplier.class);
      if (instance == null) {
        throw new ParameterResolutionException("No such resource found: " + resource);
      }
      return instance;
    }
    var newResource = parameter.findAnnotation(New.class);
    if (newResource.isPresent()) {
      var resource = newResource.get();
      var type = resource.value();
      var key = type.getName() + '@' + parameter.getIndex();
      var instance = newInstance(type);
      store.put(key, instance);
      return instance;
    }
    throw new ParameterResolutionException("Can't resolve resource supplier for: " + parameter);
  }
}
