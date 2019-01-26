package integration.resource;

import de.sormuras.brahms.resource.ResourceManager;
import de.sormuras.brahms.resource.ResourceManager.New;
import de.sormuras.brahms.resource.ResourceManager.Singleton;
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
    System.out.println("GLOBAL     = " + global);
    System.out.println("this.local = " + local);
  }

  @Test
  void chicken(@New(Temporary.class) Path chicken) {
    System.out.println();
    System.out.println("** chicken = " + chicken);
    System.out.println("GLOBAL     = " + global);
    System.out.println("this.local = " + local);
  }

  @Test
  void egg(@New(Temporary.class) Path egg) {
    System.out.println();
    System.out.println("****** egg = " + egg);
    System.out.println("GLOBAL     = " + global);
    System.out.println("this.local = " + local);
  }
}
