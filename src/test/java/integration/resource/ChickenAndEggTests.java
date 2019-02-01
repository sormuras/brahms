package integration.resource;

import de.sormuras.brahms.resource.ResourceManager;
import de.sormuras.brahms.resource.ResourceManager.New;
import de.sormuras.brahms.resource.ResourceManager.Singleton;
import de.sormuras.brahms.resource.Temporary;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ResourceManager.class)
class ChickenAndEggTests {

  private final Path global;
  private final Path local;

  public ChickenAndEggTests(
      @Singleton(Temporary.class) Path global, @New(Temporary.class) Path local) {
    this.global = global;
    this.local = local;
    System.out.println();
    System.out.println();
    System.out.println();
    System.out.println("*** c'tor()");
    System.out.println("GLOBAL     = " + global);
    System.out.println("this.local = " + local);
  }

  @Test
  void chicken(@New(Temporary.class) Path chicken) {
    System.out.println();
    System.out.println("*** chicken() = " + chicken);
    System.out.println("GLOBAL     = " + global);
    System.out.println("this.local = " + local);
  }

  @Test
  void egg(@New(JimFS.class) Path egg) {
    System.out.println();
    System.out.println("**** egg() = " + egg);
    System.out.println("GLOBAL     = " + global);
    System.out.println("this.local = " + local);
  }
}

class JimFS extends Temporary {
  @Override
  protected Path createTempDirectory() throws Exception {
    return Files.createTempDirectory("here-be-jim-fs-");
  }
}
