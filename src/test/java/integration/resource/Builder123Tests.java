package integration.resource;

import de.sormuras.brahms.resource.ClassResource;
import de.sormuras.brahms.resource.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ResourceManager.class)
class Builder123Tests {

  @Test
  void startsWith123(@ClassResource(Builder123.class) StringBuilder builder) {
    Assertions.assertEquals("123", builder.toString().substring(0, 3));
  }
}
