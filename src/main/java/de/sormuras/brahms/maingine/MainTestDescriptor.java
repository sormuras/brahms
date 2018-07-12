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

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

class MainTestDescriptor extends AbstractTestDescriptor {

  private final Class<?> mainClass;

  MainTestDescriptor(UniqueId uniqueId, Class<?> mainClass) {
    super(
        uniqueId.append("main-test", mainClass.getName()),
        mainClass.getName(),
        ClassSource.from(mainClass));
    this.mainClass = mainClass;
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }

  Class<?> getMainClass() {
    return mainClass;
  }
}
