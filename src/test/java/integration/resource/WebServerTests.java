package integration.resource;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import de.sormuras.brahms.resource.ResourceManager;
import de.sormuras.brahms.resource.ResourceManager.Closeable;
import de.sormuras.brahms.resource.ResourceManager.New;
import de.sormuras.brahms.resource.ResourceManager.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ResourceManager.class)
class WebServerTests {

  @Closeable private static WebServer classServer = new WebServer();

  @Test
  void usingClassLocalServer() {
    var actual = browse(classServer.getUri());
    assertLinesMatch(List.of("counter = [1|2]"), actual);
  }

  @Test
  void usingFreshServerInstance(@New(WebServer.class) WebServer server) {
    var actual = browse(server.getUri());
    assertLinesMatch(List.of("counter = 1"), actual);
  }

  @Test
  void usingSharedServerInstance(@Singleton(WebServer.class) WebServer server) {
    var actual = browse(server.getUri());
    assertLinesMatch(List.of("counter = [1|2|3]"), actual);
  }

  @Nested
  class SecondLayer {

    @Test
    void usingSharedServerAgain(@Singleton(WebServer.class) WebServer server) {
      var actual = browse(server.getUri());
      assertLinesMatch(List.of("counter = [1|2|3]"), actual);
    }

    @Test
    void usingClassLocalAgain() {
      var actual = browse(classServer.getUri());
      assertLinesMatch(List.of("counter = [1|2]"), actual);
    }
  }

  private List<String> browse(URI uri) {
    var request = HttpRequest.newBuilder().uri(uri).build();
    var builder = new StringBuilder();
    HttpClient.newHttpClient()
        .sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(HttpResponse::body)
        .thenAccept(builder::append)
        .join();
    return List.of(builder.toString().split("\\R"));
  }
}
