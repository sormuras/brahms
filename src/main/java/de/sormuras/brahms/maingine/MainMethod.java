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
import java.util.Arrays;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

class MainMethod extends AbstractTestDescriptor {

  private static String displayName(Test test) {
    var displayName = test.displayName();
    var args = String.join(", ", test.value());
    if (displayName.length() > 0) {
      return displayName.replace("${ARGS}", args);
    }
    return test.displayName().isEmpty() ? "main(" + args + ")" : test.displayName();
  }

  private final Method method;
  private final boolean fork;
  private final String[] arguments;
  private final String[] options;

  MainMethod(UniqueId uniqueId, Method method) {
    super(uniqueId, "main()", MethodSource.from(method));
    this.method = method;
    this.fork = false;
    this.arguments = new String[0];
    this.options = new String[0];
  }

  MainMethod(UniqueId uniqueId, Method method, Test test) {
    super(uniqueId, displayName(test), MethodSource.from(method));
    this.method = method;
    this.arguments = test.value();
    this.fork = test.fork();
    this.options = test.options();
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }

  Method getMethod() {
    return method;
  }

  boolean isFork() {
    return fork;
  }

  String[] getArguments() {
    return arguments;
  }

  String[] getOptions() {
    return options;
  }

  @Override
  public String toString() {
    String sb =
        "MainMethod{"
            + "method="
            + method
            + ", fork="
            + fork
            + ", arguments="
            + Arrays.toString(arguments)
            + ", options="
            + Arrays.toString(options)
            + '}';
    return sb;
  }
}
