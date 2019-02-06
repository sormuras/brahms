package integration.resource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import de.sormuras.brahms.resource.ResourceManager;
import de.sormuras.brahms.resource.ResourceManager.New;
import de.sormuras.brahms.resource.ResourceManager.Shared;
import de.sormuras.brahms.resource.ResourceManager.Singleton;
import de.sormuras.brahms.resource.ResourceSupplier;
import de.sormuras.brahms.resource.Temporary;
import java.nio.file.FileSystem;
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
    System.out.println("*** chicken() = " + chicken.toUri());
    System.out.println("GLOBAL     = " + global);
    System.out.println("this.local = " + local);
  }

  @Test
  void egg(@New(JimFS.class) Path egg) {
    System.out.println();
    System.out.println("**** egg() = " + egg.toUri());
    System.out.println("GLOBAL     = " + global);
    System.out.println("this.local = " + local);
  }

  @Test
  void mix(
      @New(Temporary.class) Path m1,
      @New(JimFS.class) Path m2,
      @Singleton(Temporary.class) Path m3) {
    System.out.println();
    System.out.println("*** mix(1) = " + m1.toUri());
    System.out.println("*** mix(2) = " + m2.toUri());
    System.out.println("*** mix(3) = " + m3.toUri());
    System.out.println("GLOBAL     = " + global);
    System.out.println("this.local = " + local);
  }

  @Test
  void shared(@Shared(name = "4711", type = JimFS.class) Path shared) {
    System.out.println();
    System.out.println("* shared() = " + shared.toUri());
    System.out.println("GLOBAL     = " + global);
    System.out.println("this.local = " + local);
  }
}

class JimFS implements ResourceSupplier<Path> {

  private final FileSystem jim = Jimfs.newFileSystem(Configuration.unix());

  @Override
  public Path get() {
    return jim.getPath("/");
  }

  @Override
  public void close() {
    try {
      jim.close();
    } catch (Exception e) {
      throw new RuntimeException("He's dead, Jim.", e);
    }
  }
}
