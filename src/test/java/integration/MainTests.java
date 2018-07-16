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

import de.sormuras.brahms.maingine.Java;
import de.sormuras.brahms.maingine.Main;

public class MainTests {

  // No-args test run
  @Main
  // Single argument test run
  @Main("1")
  // Multiple arguments test run
  @Main({"2", "3"})
  // Custom display name of test run
  @Main(
      displayName = "main with '${ARGS}' as args",
      value = {"3", "4", "5"})
  // Fork VM and launch with specific java/VM options
  @Main(
      displayName = "☕ ${ARGS}",
      value = {"6", "7"},
      java = @Java(options = {"-classpath", "${java.class.path}"}))
  public static void main(String... args) {
    var message = args.length == 0 ? "<no-args>" : String.join(", ", args);
    System.out.println("MainTests: " + message);
  }
}
