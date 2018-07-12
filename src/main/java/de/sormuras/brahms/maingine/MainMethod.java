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

import java.lang.reflect.Method;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

class MainMethod extends AbstractTestDescriptor {

  static MainMethod of(Method method, TestDescriptor parent) {
    var uniqueId = parent.getUniqueId().append("main-method", "main");
    var displayName = "main()";
    var result = new MainMethod(uniqueId, displayName, MethodSource.from(method), method);
    parent.addChild(result);
    return result;
  }

  private final Method method;

  private MainMethod(UniqueId uniqueId, String displayName, MethodSource source, Method method) {
    super(uniqueId, displayName, source);
    this.method = method;
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }

  Method getMethod() {
    return method;
  }
}
