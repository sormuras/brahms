package integration.resource;

import de.sormuras.brahms.resource.ResourceManager;
import de.sormuras.brahms.resource.ResourceManager.Get;
import de.sormuras.brahms.resource.ResourceManager.New;
import de.sormuras.brahms.resource.ResourceManager.Put;
import de.sormuras.brahms.resource.ResourceManager.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ResourceManager.class)
@Resource(id = "123", supplier = Builder123.class)
@Resource(id = "global", supplier = Builder123.class, global = true)
class ResourceManagerDemoTests {

  @Put("456")
  private static StringBuilder builder456 = new StringBuilder("456");

  @Test
  void isNotEmpty(@Get("123") StringBuilder builder) {
    Assertions.assertFalse(builder.length() == 0);
  }

  @Test
  void startsWith123(@Get("456") StringBuilder builder) {
    Assertions.assertEquals("456", builder.toString().substring(0, 3));
  }

  @Test
  void same(@Get("123") Comparable<StringBuilder> comparable, @Get("123") CharSequence sequence) {
    Assertions.assertSame(comparable, sequence);
  }

  @Test
  void mixed(
      @New(Builder123.class) Object local,
      @Get("123") Object shared,
      @Get("global") Object global) {
    Assertions.assertNotSame(global, local);
    Assertions.assertNotSame(shared, local);
    Assertions.assertNotSame(shared, global);
  }
}
