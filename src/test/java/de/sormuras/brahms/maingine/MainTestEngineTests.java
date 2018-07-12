package de.sormuras.brahms.maingine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class MainTestEngineTests {

  @Test
  void providesPublicDefaultConstructor() {
    assertDoesNotThrow(MainTestEngine::new);
  }
}
