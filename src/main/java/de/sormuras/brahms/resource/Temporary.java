package de.sormuras.brahms.resource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.ParameterResolutionException;

public class Temporary implements ResourceSupplier<Path> {

  private final Path path;

  public Temporary() {
    try {
      this.path = createTempDirectory();
    } catch (Exception e) {
      throw new ParameterResolutionException("creating temporary directory failed", e);
    }
  }

  protected Path createTempDirectory() throws Exception {
    return Files.createTempDirectory("temporary-");
  }

  @Override
  public Path get() {
    return path;
  }

  @Override
  public void close() {
    // trivial case: delete empty directory right away
    try {
      if (Files.deleteIfExists(path)) {
        return;
      }
    } catch (IOException ignored) {
      // fall-through
    }
    // default case: walk the tree...
    try (var stream = Files.walk(path)) {
      var selected = stream.sorted((p, q) -> -p.compareTo(q));
      for (var path : selected.collect(Collectors.toList())) {
        Files.deleteIfExists(path);
      }
    } catch (IOException e) {
      throw new UncheckedIOException("removing tree failed: " + path, e);
    }
  }
}
