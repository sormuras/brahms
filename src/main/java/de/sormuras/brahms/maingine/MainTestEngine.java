/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package de.sormuras.brahms.maingine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
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

    // TODO find classes with psvm method...
    if (Boolean.getBoolean("grmlpfg")) {
      try {
        var main = getClass().getClassLoader().loadClass("integration.MainOne");
        var container = new MainContainerDescriptor(engine.getUniqueId(), main);
        var test = new MainTestDescriptor(container.getUniqueId(), main);
        container.addChild(test);
        engine.addChild(container);
      } catch (ReflectiveOperationException e) {
        // ignore
      }
    }
    return engine;
  }

  @Override
  public void execute(ExecutionRequest request) {
    var engine = request.getRootTestDescriptor();
    var listener = request.getEngineExecutionListener();
    listener.executionStarted(engine);
    for (var container : engine.getChildren()) {
      listener.executionStarted(container);
      for (var child : container.getChildren()) {
        listener.executionStarted(child);
        var result = executeMainMethod(((MainTestDescriptor) child).getMainClass());
        listener.executionFinished(child, result);
      }
      listener.executionFinished(container, TestExecutionResult.successful());
    }
    listener.executionFinished(engine, TestExecutionResult.successful());
  }

  private TestExecutionResult executeMainMethod(Class<?> mainClass) {
    try {
      var mainMethod = mainClass.getDeclaredMethod("main", String[].class);
      var arguments = new String[0];
      mainMethod.invoke(null, new Object[] {arguments});
      return TestExecutionResult.successful();
    } catch (Throwable t) {
      return TestExecutionResult.failed(t);
    }
  }
}
