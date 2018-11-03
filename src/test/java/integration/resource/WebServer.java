package integration.resource;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.sormuras.brahms.resource.ResourceSupplier;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WebServer implements ResourceSupplier<HttpServer>, HttpHandler {

  private final AtomicInteger counter;
  private final HttpServer httpServer;

  public WebServer() {
    this.counter = new AtomicInteger();
    this.httpServer = createAndStartHttpServerOnFreePort();
  }

  private HttpServer createAndStartHttpServerOnFreePort() {
    try {
      var server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
      server.createContext("/", this);
      server.start();
      return server;
    } catch (IOException e) {
      throw new UncheckedIOException("Creating HttpServer failed!", e);
    }
  }

  @Override
  public void close() {
    httpServer.stop(1);
  }

  @Override
  public HttpServer get() {
    return httpServer;
  }

  public URI getUri() {
    var host = httpServer.getAddress().getHostName();
    var port = httpServer.getAddress().getPort();
    return URI.create("http://" + host + ":" + port);
  }

  public List<String> getTextLines(URI uri) {
    String result;
    try {
      try (var inputStream = uri.toURL().openStream()) {
        result = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      }
    } catch (IOException exception) {
      result = exception.toString();
    }
    return List.of(result.split("\\R"));
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String response = "counter = " + counter.incrementAndGet();
    exchange.sendResponseHeaders(200, response.length());
    try (var os = exchange.getResponseBody()) {
      os.write(response.getBytes());
    }
  }
}
