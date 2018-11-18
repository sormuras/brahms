package integration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import de.sormuras.brahms.maingine.MainTestEngine;
import java.util.Set;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MaingineIntegrationTests {

  @Test
  void execute_successful_Java_program() {
    var executionResults = execute(SuccessfulMainProgram.class);
    // executionResults.all().debug();
    /*
     * All Events:
     *  ExecutionEvent [type = STARTED, testDescriptor = EngineDescriptor: [engine:brahms-maingine], timestamp = 2018-11-04T07:07:26.131277500Z, payload = null]
     *  ExecutionEvent [type = STARTED, testDescriptor = MainClass: [engine:brahms-maingine]/[main-class:integration.MaingineIntegrationTests$SuccessfulMainProgram], timestamp = 2018-11-04T07:07:26.131277500Z, payload = null]
     *  ExecutionEvent [type = STARTED, testDescriptor = MainMethod: [engine:brahms-maingine]/[main-class:integration.MaingineIntegrationTests$SuccessfulMainProgram]/[main:main0], timestamp = 2018-11-04T07:07:26.131277500Z, payload = null]
     *  ExecutionEvent [type = FINISHED, testDescriptor = MainMethod: [engine:brahms-maingine]/[main-class:integration.MaingineIntegrationTests$SuccessfulMainProgram]/[main:main0], timestamp = 2018-11-04T07:07:26.131277500Z, payload = TestExecutionResult [status = SUCCESSFUL, throwable = null]]
     *  ExecutionEvent [type = FINISHED, testDescriptor = MainClass: [engine:brahms-maingine]/[main-class:integration.MaingineIntegrationTests$SuccessfulMainProgram], timestamp = 2018-11-04T07:07:26.131277500Z, payload = TestExecutionResult [status = SUCCESSFUL, throwable = null]]
     *  ExecutionEvent [type = FINISHED, testDescriptor = EngineDescriptor: [engine:brahms-maingine], timestamp = 2018-11-04T07:07:26.131277500Z, payload = TestExecutionResult [status = SUCCESSFUL, throwable = null]]
     */
    executionResults.all().assertStatistics(it -> it.started(3).finished(3).succeeded(3));
    executionResults.tests().assertStatistics(it -> it.started(1).finished(1).succeeded(1));

    var testDescriptor = executionResults.tests().succeeded().list().get(0).getTestDescriptor();
    assertAll(
        () -> assertEquals("main()", testDescriptor.getDisplayName()),
        () -> assertEquals("main()", testDescriptor.getLegacyReportingName()),
        () -> assertEquals(Set.of(), testDescriptor.getTags()));
  }

  @Test
  void execute_throwing_Java_program() {
    var executionResults = execute(ThrowingMainProgram.class);

    executionResults.all().assertStatistics(it -> it.started(3).finished(3).succeeded(2));
    executionResults.tests().assertStatistics(it -> it.started(1).finished(1).failed(1));

    var testResult =
        executionResults
            .tests()
            .failed()
            .list()
            .get(0)
            .getRequiredPayload(TestExecutionResult.class);
    var throwable = testResult.getThrowable().orElseThrow();
    assertEquals("java.lang.reflect.InvocationTargetException", throwable.toString());
    assertEquals("java.lang.Error: Guru Meditation", String.valueOf(throwable.getCause()));
  }

  private EngineExecutionResults execute(Class<?> program) {
    var discoveryRequest = request().selectors(DiscoverySelectors.selectClass(program)).build();
    return EngineTestKit.execute(new MainTestEngine(), discoveryRequest);
  }

  public static class SuccessfulMainProgram {
    public static void main(String[] args) {}
  }

  public static class ThrowingMainProgram {
    public static void main(String[] args) {
      throw new Error("Guru Meditation");
    }
  }
}
