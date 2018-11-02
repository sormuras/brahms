package de.sormuras.brahms.resource;

import static org.junit.platform.commons.support.ReflectionSupport.newInstance;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ResourceManager implements ParameterResolver, BeforeAllCallback {

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Repeatable(Resources.class)
  public @interface Resource {

    Class<? extends ResourceSupplier<?>> value();

    String id() default "";

    enum Context {
      CLASS,
      SINGLETON
    }

    Context context() default Context.CLASS;
  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Resources {
    Resource[] value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface Get {

    Class<? extends ResourceSupplier<?>> value();

    String id() default "";
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface New {

    Class<? extends ResourceSupplier<?>> value();
  }

  private static final Namespace NAMESPACE = Namespace.create(ResourceManager.class);

  @Override
  public void beforeAll(ExtensionContext extension) {
    var resources =
        AnnotationSupport.findRepeatableAnnotations(
            extension.getRequiredTestClass(), Resource.class);
    for (var resource : resources) {
      var type = resource.value();
      var key = type.getName() + '@' + resource.id();
      var context = resource.context() == Resource.Context.CLASS ? extension : extension.getRoot();
      var store = context.getStore(NAMESPACE);
      store.getOrComputeIfAbsent(key, k -> newInstance(type), ResourceSupplier.class);
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
      var type = resource.value();
      var key = type.getName() + '@' + resource.id();
      var instance = store.get(key, ResourceSupplier.class);
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
