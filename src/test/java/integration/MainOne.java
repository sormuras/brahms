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

package integration;

import de.sormuras.brahms.maingine.Test;

public class MainOne {

  @Test("1")
  @Test({"2", "3"})
  @Test(
      displayName = "main with ${ARGS} as args",
      value = {"3", "4", "5"})
  @Test(
      fork = true,
      displayName = "☕ ${ARGS}",
      options = {"-classpath", "${JAVA.CLASS.PATH}"},
      value = {"6", "7"})
  public static void main(String... args) {
    System.out.println("MainOne(" + String.join(", ", args) + ")");
  }
}
