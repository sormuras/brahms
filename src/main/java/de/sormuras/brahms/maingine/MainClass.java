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
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

class MainClass extends AbstractTestDescriptor {

  static MainClass of(Class<?> mainClass, TestDescriptor parent) {
    var uniqueId = parent.getUniqueId().append("main-class", mainClass.getName());
    var displayName = mainClass.getName();
    var testSource = ClassSource.from(mainClass);
    var result = new MainClass(uniqueId, displayName, testSource);
    parent.addChild(result);
    return result;
  }

  private MainClass(UniqueId uniqueId, String displayName, TestSource testSource) {
    super(uniqueId, displayName, testSource);
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }
}
