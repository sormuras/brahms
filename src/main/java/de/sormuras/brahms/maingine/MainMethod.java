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

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

class MainMethod extends AbstractTestDescriptor {

  static MainMethod of(Class<?> mainClass, TestDescriptor parent) {
    var uniqueId = parent.getUniqueId().append("main-method", mainClass.getName());
    var displayName = "main()";
    var testSource = getMainMethodSource(mainClass);
    var result = new MainMethod(uniqueId, displayName, testSource, mainClass);
    parent.addChild(result);
    return result;
  }

  private static MethodSource getMainMethodSource(Class<?> mainClass) {
    try {
      return MethodSource.from(mainClass.getMethod("main", String[].class));
    } catch (NoSuchMethodException e) {
      throw new Error("main method not found", e);
    }
  }

  private final Class<?> mainClass;

  private MainMethod(
      UniqueId uniqueId, String displayName, MethodSource source, Class<?> mainClass) {
    super(uniqueId, displayName, source);
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
