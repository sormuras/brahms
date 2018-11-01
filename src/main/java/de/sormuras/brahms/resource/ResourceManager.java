package de.sormuras.brahms.resource;

import static org.junit.platform.commons.support.ReflectionSupport.newInstance;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class ResourceManager implements ParameterResolver {

  @Override
  public boolean supportsParameter(ParameterContext parameter, ExtensionContext __) {
    return parameter.isAnnotated(Resource.class)
        ^ parameter.isAnnotated(GlobalResource.class)
        ^ parameter.isAnnotated(ClassResource.class)
        ^ parameter.isAnnotated(MethodResource.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameter, ExtensionContext extension) {
    var supplier = new Resolver(parameter, extension).resolve();
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

  private static class Resolver {

    private static final Namespace NAMESPACE = Namespace.create(ResourceManager.class);

    private final ParameterContext parameterContext;
    private final ExtensionContext methodContext;

    Resolver(ParameterContext parameterContext, ExtensionContext methodContext) {
      this.parameterContext = parameterContext;
      this.methodContext = methodContext;
    }

    private ResourceSupplier<?> resolve() {
      var methodResource = parameterContext.findAnnotation(MethodResource.class);
      if (methodResource.isPresent()) {
        return createMethodResource(methodResource.get(), parameterContext.getIndex());
      }
      var classResource = parameterContext.findAnnotation(ClassResource.class);
      if (classResource.isPresent()) {
        return getOrCreateClassResource(classResource.get());
      }
      var globalResource = parameterContext.findAnnotation(GlobalResource.class);
      if (globalResource.isPresent()) {
        return getOrCreateGlobalResource(globalResource.get());
      }
      var resource = parameterContext.findAnnotation(Resource.class);
      if (resource.isPresent()) {
        return resolveResource(resource.get(), parameterContext.getIndex());
      }
      throw new ParameterResolutionException("Can't resolve resource for: " + parameterContext);
    }

    private ResourceSupplier<?> resolveResource(Resource resource, int index) {
      var type = resource.value();
      if (resource.context() == Resource.Context.METHOD) {
        return createResource(type, index);
      }
      var key = type.getName() + '@' + resource.id();
      var context =
          resource.context() == Resource.Context.GLOBAL
              ? methodContext.getRoot()
              : methodContext.getParent().orElseThrow(AssertionError::new);
      return getOrCreateResource(type, key, context);
    }

    private ResourceSupplier<?> createMethodResource(MethodResource methodResource, int index) {
      return createResource(methodResource.value(), index);
    }

    private ResourceSupplier<?> createResource(
        Class<? extends ResourceSupplier<?>> type, int index) {
      var key = type.getName() + '@' + index;
      var instance = newInstance(type);
      methodContext.getStore(NAMESPACE).put(key, instance);
      return instance;
    }

    private ResourceSupplier<?> getOrCreateClassResource(ClassResource classResource) {
      var type = classResource.value();
      var key = type.getName() + '@' + classResource.id();
      var context = methodContext.getParent().orElseThrow(AssertionError::new);
      return getOrCreateResource(type, key, context);
    }

    private ResourceSupplier<?> getOrCreateGlobalResource(GlobalResource globalResource) {
      var type = globalResource.value();
      var key = type.getName() + '@' + globalResource.id();
      var context = methodContext.getRoot();
      return getOrCreateResource(type, key, context);
    }

    private ResourceSupplier<?> getOrCreateResource(
        Class<? extends ResourceSupplier<?>> type, Object key, ExtensionContext storeContext) {
      // first, look up an existing resource available via the current method context's hierarchy
      var resource = methodContext.getStore(NAMESPACE).get(key, ResourceSupplier.class);
      if (resource != null) {
        return resource;
      }
      // still here: create resource and store it in the specified store context
      return storeContext
          .getStore(NAMESPACE)
          .getOrComputeIfAbsent(key, k -> newInstance(type), ResourceSupplier.class);
    }
  }
}
