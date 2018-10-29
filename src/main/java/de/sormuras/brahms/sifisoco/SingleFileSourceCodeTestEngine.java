package de.sormuras.brahms.sifisoco;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.descriptor.FileSource;

public class SingleFileSourceCodeTestEngine implements TestEngine {

  static String ENGINE_ID = "brahms-sifisoco";

  static String ENGINE_DISPLAY_NAME = "Brahms Single-File Source-Code Engine (JEP 330)";

  static Pattern PUBLIC_STATIC_VOID_MAIN_PATTERN =
      Pattern.compile(".+(?:public\\s+)?static\\s+void\\s+main.+String.+", Pattern.DOTALL);

  private static boolean isSingleFileSourceCodeProgram(Path path, BasicFileAttributes attributes) {
    if (!attributes.isRegularFile()) {
      return false;
    }
    if (attributes.size() < 42) { // "interface A{static void main(String[]a){}}"
      return false;
    }
    if (!path.toString().endsWith(".java")) {
      return false;
    }
    try {
      var code = Files.readString(path);
      if (!PUBLIC_STATIC_VOID_MAIN_PATTERN.matcher(code).matches()) {
        return false;
      }
    } catch (IOException e) {
      return false;
    }
    return true;
  }

  private static Path getCurrentJavaExecutablePath() {
    var path = ProcessHandle.current().info().command().map(Paths::get).orElseThrow();
    return path.normalize().toAbsolutePath();
  }

  private final Path java;

  public SingleFileSourceCodeTestEngine() {
    this.java = getCurrentJavaExecutablePath();
  }

  @Override
  public String getId() {
    return ENGINE_ID;
  }

  @Override
  public EngineDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
    var engine = new EngineDescriptor(uniqueId, ENGINE_DISPLAY_NAME);
    var scanner = new Scanner(engine);
    scanner.scanRequest(discoveryRequest);
    scanner.scanJavaClassPath();
    return engine;
  }

  @Override
  public void execute(ExecutionRequest request) {
    var engine = request.getRootTestDescriptor();
    var listener = request.getEngineExecutionListener();
    listener.executionStarted(engine);
    for (var descriptor : engine.getChildren()) {
      listener.executionStarted(descriptor);
      var test = (TestDescriptor) descriptor;
      var result = execute(test.path);
      listener.executionFinished(descriptor, result);
    }
    listener.executionFinished(engine, TestExecutionResult.successful());
  }

  private TestExecutionResult execute(Path program) {
    if (Runtime.version().feature() < 11) {
      var error = new AssertionError("Java 11 or higher required, running: " + Runtime.version());
      return TestExecutionResult.aborted(error);
    }
    var builder = new ProcessBuilder();
    builder.command().add(java.toString());
    builder.command().add(program.getFileName().toString());
    builder.directory(program.getParent().toFile());
    builder.inheritIO();
    try {
      var process = builder.start();
      var actualExitValue = process.waitFor();
      var expectedExitValue = 0;
      if (actualExitValue != expectedExitValue) {
        var message = "expected exit value " + expectedExitValue + ", but got: " + actualExitValue;
        return TestExecutionResult.failed(new IllegalStateException(message));
      }
    } catch (IOException | InterruptedException e) {
      return TestExecutionResult.failed(e);
    }
    return TestExecutionResult.successful();
  }

  private static class Scanner {

    final EngineDescriptor engine;
    final Set<Path> programs;

    Scanner(EngineDescriptor engine) {
      this.engine = engine;
      this.programs = new HashSet<>();
    }

    void scanJavaClassPath() {
      var roots = System.getProperty("java.class.path").split(Pattern.quote(File.pathSeparator));
      Arrays.stream(roots).map(Paths::get).filter(Files::isDirectory).forEach(this::scanDirectory);
    }

    void scanRequest(EngineDiscoveryRequest discoveryRequest) {
      // class-path root
      discoveryRequest
          .getSelectorsByType(ClasspathRootSelector.class)
          .stream()
          .map(ClasspathRootSelector::getClasspathRoot)
          .map(Paths::get)
          .filter(Files::isDirectory)
          .forEach(this::scanDirectory);

      // uri
      discoveryRequest
          .getSelectorsByType(UriSelector.class)
          .stream()
          .map(UriSelector::getUri)
          .map(Paths::get)
          .filter(Files::isDirectory)
          .forEach(this::scanDirectory);
    }

    private void scanDirectory(Path path) {
      try {
        Files.find(path, 1, SingleFileSourceCodeTestEngine::isSingleFileSourceCodeProgram)
            .map(Path::normalize)
            .map(Path::toAbsolutePath)
            .filter(file -> !programs.contains(file))
            .forEach(this::add);
      } catch (IOException e) {
        throw new UncheckedIOException("scan directory failed: " + path, e);
      }
    }

    private void add(Path program) {
      var id = engine.getUniqueId().append("main-java", "java-" + program);
      engine.addChild(new TestDescriptor(id, program));
      programs.add(program);
    }
  }

  private static class TestDescriptor extends AbstractTestDescriptor {

    final Path path;

    TestDescriptor(UniqueId uniqueId, Path path) {
      super(uniqueId, path.getFileName().toString(), FileSource.from(path.toFile()));
      this.path = path;
    }

    @Override
    public Type getType() {
      return Type.TEST;
    }
  }
}
