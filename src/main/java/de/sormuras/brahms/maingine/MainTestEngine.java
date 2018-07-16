/*
 * Copyright (C) 2018 Christian Stein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sormuras.brahms.maingine;

import static java.lang.System.identityHashCode;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInPackage;
import static org.junit.platform.commons.util.ReflectionUtils.isPublic;
import static org.junit.platform.commons.util.ReflectionUtils.isStatic;
import static org.junit.platform.commons.util.ReflectionUtils.returnsVoid;
import static org.junit.platform.engine.support.filter.ClasspathScanningSupport.buildClassNamePredicate;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

/** Main-invoking TestEngine implementation. */
public class MainTestEngine implements TestEngine {

  @Override
  public String getId() {
    return "brahms-main-engine";
  }

  @Override
  public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
    var engine = new EngineDescriptor(uniqueId, "Main Engine");

    ClassFilter classFilter = ClassFilter.of(buildClassNamePredicate(discoveryRequest), c -> true);

    // package
    discoveryRequest
        .getSelectorsByType(PackageSelector.class)
        .stream()
        .map(PackageSelector::getPackageName)
        .map(packageName -> findAllClassesInPackage(packageName, classFilter))
        .flatMap(Collection::stream)
        .forEach(candidate -> handleCandidate(engine, candidate));

    // class
    discoveryRequest
        .getSelectorsByType(ClassSelector.class)
        .stream()
        .map(ClassSelector::getJavaClass)
        .filter(classFilter)
        .forEach(candidate -> handleCandidate(engine, candidate));

    return engine;
  }

  private void handleCandidate(EngineDescriptor engine, Class<?> candidate) {
    Method main;
    try {
      main = candidate.getDeclaredMethod("main", String[].class);
    } catch (NoSuchMethodException e) {
      return;
    }
    if (!isPublic(main)) {
      return;
    }
    if (!isStatic(main)) {
      return;
    }
    if (!returnsVoid(main)) {
      return;
    }
    var container = MainClass.of(candidate, engine);
    var annotations = main.getDeclaredAnnotationsByType(Main.class);
    if (annotations.length == 0) {
      var id = container.getUniqueId().append("main", "main0");
      container.addChild(new MainMethod(id, main));
      return;
    }
    for (var annotation : annotations) {
      var id = container.getUniqueId().append("main", "main" + identityHashCode(annotation));
      container.addChild(new MainMethod(id, main, annotation));
    }
  }

  @Override
  public void execute(ExecutionRequest request) {
    var engine = request.getRootTestDescriptor();
    var listener = request.getEngineExecutionListener();
    listener.executionStarted(engine);
    for (var mainClass : engine.getChildren()) {
      listener.executionStarted(mainClass);
      for (var mainMethod : mainClass.getChildren()) {
        listener.executionStarted(mainMethod);
        var result = executeMainMethod(((MainMethod) mainMethod));
        listener.executionFinished(mainMethod, result);
      }
      listener.executionFinished(mainClass, TestExecutionResult.successful());
    }
    listener.executionFinished(engine, TestExecutionResult.successful());
  }

  private TestExecutionResult executeMainMethod(MainMethod mainMethod) {
    return mainMethod.isFork() ? executeForked(mainMethod) : executeDirect(mainMethod);
  }

  private TestExecutionResult executeDirect(MainMethod mainMethod) {
    try {
      var method = mainMethod.getMethod();
      var arguments = new Object[] {mainMethod.getArguments()};
      method.invoke(null, arguments);
    } catch (Throwable t) {
      return TestExecutionResult.failed(t);
    }
    return TestExecutionResult.successful();
  }

  // https://docs.oracle.com/javase/10/tools/java.htm
  // java [options] mainclass [args...]
  // java [options] -jar jarfile [args...]
  // java [options] [--module-path modulepath] --module module[/mainclass] [args...]
  private TestExecutionResult executeForked(MainMethod mainMethod) {
    var builder = new ProcessBuilder(java().normalize().toAbsolutePath().toString());
    var command = builder.command();
    Arrays.stream(mainMethod.getOptions())
        .map(MainTestEngine::replaceSystemProperties)
        .forEach(command::add);
    command.add(mainMethod.getMethod().getDeclaringClass().getName());
    command.addAll(List.of(mainMethod.getArguments()));
    builder.inheritIO();
    try {
      var process = builder.start();
      var actualExitValue = process.waitFor();
      var expectedExitValue = mainMethod.getExpectedExitValue();
      if (actualExitValue != expectedExitValue) {
        var message = "expected exit value " + expectedExitValue + ", but got: " + actualExitValue;
        return TestExecutionResult.failed(new IllegalStateException(message));
      }
    } catch (IOException | InterruptedException e) {
      return TestExecutionResult.failed(e);
    }
    return TestExecutionResult.successful();
  }

  private static Path java() {
    return ProcessHandle.current().info().command().map(Paths::get).orElseThrow();
  }

  // https://docs.oracle.com/javase/10/docs/api/java/lang/System.html#getProperties()
  private static String replaceSystemProperties(String string) {
    string = replaceSystemProperty(string, "java.class.path");
    string = replaceSystemProperty(string, "jdk.module.path");
    return string;
  }

  private static String replaceSystemProperty(String string, String key) {
    var replacement = System.getProperty(key);
    if (replacement == null) {
      return string;
    }
    return string.replace("${" + key + "}", replacement);
  }
}
