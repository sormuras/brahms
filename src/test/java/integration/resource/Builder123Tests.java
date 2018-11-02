package integration.resource;

import de.sormuras.brahms.resource.ResourceManager;
import de.sormuras.brahms.resource.ResourceManager.Get;
import de.sormuras.brahms.resource.ResourceManager.New;
import de.sormuras.brahms.resource.ResourceManager.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ResourceManager.class)
@Resource(Builder123.class)
@Resource(value = Builder123.class, context = Resource.Context.SINGLETON)
class Builder123Tests {

  @Test
  void isNotEmpty(@Get(Builder123.class) StringBuilder builder) {
    Assertions.assertFalse(builder.length() == 0);
  }

  @Test
  void startsWith123(@Get(Builder123.class) StringBuilder builder) {
    Assertions.assertEquals("123", builder.toString().substring(0, 3));
  }

  @Test
  void same(@Get(Builder123.class) StringBuilder b1, @Get(Builder123.class) StringBuilder b2) {
    Assertions.assertSame(b1, b2);
  }

  @Test
  void mixed(@Get(Builder123.class) StringBuilder shared, @New(Builder123.class) StringBuilder local) {
    Assertions.assertNotSame(shared, local);
  }
}
