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

package de.sormuras.brahms.sifisoco;

import static de.sormuras.brahms.sifisoco.SingleFileSourceCodeTestEngine.ENGINE_DISPLAY_NAME;
import static de.sormuras.brahms.sifisoco.SingleFileSourceCodeTestEngine.ENGINE_ID;
import static de.sormuras.brahms.sifisoco.SingleFileSourceCodeTestEngine.PUBLIC_STATIC_VOID_MAIN_PATTERN;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SingleFileSourceCodeTestEngineTests {

  @Test
  void providesPublicDefaultConstructor() {
    assertDoesNotThrow(SingleFileSourceCodeTestEngine::new);
  }

  @Test
  void constantsAreNotNull() {
    assertNotNull(ENGINE_ID);
    assertNotNull(ENGINE_DISPLAY_NAME);
  }

  @Test
  void nonStaticProgramDontMatch() {
    var program = "class A{public void main(String[]a){}}";
    assertFalse(PUBLIC_STATIC_VOID_MAIN_PATTERN.matcher(program).matches());
  }

  @Test
  void minimalClassProgramMatchesPattern() {
    var program = "class A{public static void main(String[]a){}}";
    assertTrue(PUBLIC_STATIC_VOID_MAIN_PATTERN.matcher(program).matches());
  }

  @Test
  void minimalInterfaceProgramMatchesPattern() {
    var program = "interface A{static void main(String[]a){}}";
    assertTrue(PUBLIC_STATIC_VOID_MAIN_PATTERN.matcher(program).matches());
  }

  @Test
  void minimalEnumProgramMatchesPattern() {
    var program = "enum A{;public static void main(String[]a){}}";
    assertTrue(PUBLIC_STATIC_VOID_MAIN_PATTERN.matcher(program).matches());
  }

  @Test
  void multiLineProgramMatchesPattern() {
    var program = "class A{public   static\n void\r main   (\r\n\nString[] args){}}";
    assertTrue(PUBLIC_STATIC_VOID_MAIN_PATTERN.matcher(program).matches());
  }
}
