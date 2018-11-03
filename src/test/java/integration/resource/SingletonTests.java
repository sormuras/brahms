package integration.resource;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import de.sormuras.brahms.resource.ResourceManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ResourceManager.class)
class SingletonTests {

  @Test
  void usingSharedServerInstance(@ResourceManager.Singleton(WebServer.class) WebServer server) {
    var actual = server.getTextLines(server.getUri());
    assertLinesMatch(List.of("counter = [1|2|3]"), actual);
  }
}
