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

import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInPackage;
import static org.junit.platform.engine.support.filter.ClasspathScanningSupport.buildClassNamePredicate;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
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
    return "maingine";
  }

  @Override
  public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
    var engine = new EngineDescriptor(uniqueId, "Maingine");

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
    try {
      var main = candidate.getMethod("main", String[].class);
      if (!Modifier.isStatic(main.getModifiers())) {
        return;
      }
      if (main.getReturnType() != void.class) {
        return;
      }
      var container = MainClass.of(candidate, engine);
      MainMethod.of(main, container);
    } catch (NoSuchMethodException e) {
      // ignore
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
        var result = executeMainMethod(((MainMethod) mainMethod).getMethod());
        listener.executionFinished(mainMethod, result);
      }
      listener.executionFinished(mainClass, TestExecutionResult.successful());
    }
    listener.executionFinished(engine, TestExecutionResult.successful());
  }

  private TestExecutionResult executeMainMethod(Method method) {
    try {
      var arguments = new String[0];
      method.invoke(null, new Object[] {arguments});
      return TestExecutionResult.successful();
    } catch (Throwable t) {
      return TestExecutionResult.failed(t);
    }
  }
}
