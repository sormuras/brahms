# brahms

[![jdk11](https://img.shields.io/badge/jdk-11-blue.svg)](http://jdk.java.net/11)
[![travis](https://travis-ci.com/sormuras/brahms.svg?branch=master)](https://travis-ci.com/sormuras/brahms)
[![Maven Central](https://img.shields.io/maven-central/v/de.sormuras/brahms.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22de.sormuras%22%20AND%20a:%22brahms%22)

This projects offers some proof-of-concept ideas implementing JUnit Jupiter's Extension API and JUnit Platform's
[TestEngine](https://junit.org/junit5/docs/current/user-guide/#launcher-api-engines-custom):

![Brahms Overview](docs/brahms-overview.png)

Download the [latest JAR](https://search.maven.org/remote_content?g=de.sormuras&a=brahms&v=LATEST) or depend via Maven:

```xml
<dependency>
  <groupId>de.sormuras</groupId>
  <artifactId>brahms</artifactId>
  <version>{brahms.version}</version>
  <scope>test</scope>
</dependency>
```

or Gradle:

```
testCompile "de.sormuras:brahms:${brahms.version}"
```

Don't want to use the brahms API in your test code?
Add a test runtime dependency will do just fine:

```
testRuntime "de.sormuras:brahms:${brahms.version}"
```

## Resource Manager Extension

The resource manager extension provides hassle-free global, named, and "one-shot" resource management.
You may use three different annotations at method parameters to declare in which mode a resource should be managed.

- `ResourceManager.@New` - create an instance of the resource and close it when the context is teared down.
- `ResourceManager.@Shared` - get or create an instance of the named resource
- `ResourceManager.@Singleton` - get or create the single instance of the resource - same as `@Shared(name = type.name()...)` 

`@Shared` and `@Singleton` resource are created on demand - the first parameter resolution request wins.
Resource created be those annotation may be used and shared between different test classes!
The attached resources are closed when the global Jupiter extension context store is closing.

### Temporary Directory as Resource

Brahms provides `Temporary` as a resource supplier that creates and closes temporary directories.

```java
@ExtendWith(ResourceManager.class)
class Tests { 
  @Test
  void test(@New(Temporary) Path temp) {
	// do something with "temp"
  }
}
```

`@New` resources supplied to the constructor of a test class are automatically cleaned up after the 
```java
@ExtendWith(ResourceManager.class)
class Tests {

  final Path temp;		
	
  Tests(@New(Temporary) Path temp) {
	this.temp = temp;
  }
  
  @Test
  void test() {
	// do something with "this.temp"
  }
}
```

Use `@TempDir`, a composed annotation short-cut for `@ResourceManager.New(Temporary.class)`:

```java
@ExtendWith(ResourceManager.class)
class Tests { 
  @Test
  void test(@TempDir Path temp) {
	// do something with "temp"
  }
}
```


### Custom Resource

Find samples of custom resources, like a declaring and sharing a `WebServer` and temporary & in-memory directory via `JimFS`, here:

[integration/resource](https://github.com/sormuras/brahms/tree/master/src/test/java/integration/resource)

## ☕ Brahms Maingine

**NOTE: Development of Maingine is continued here: https://github.com/sormuras/mainrunner**

Find classes that contain a `public static void main(String[] args)` method
and execute them.

#### Plain

A simple main class will be executed in-process and any exception will cause
the test to be marked as failed.

```java
public class MainPlain {
  public static void main(String... args) {
    // ...
  }
}
```

#### Customize `@Main` execution

Use the `@Main` annotation to customize the execution: pass arguments, set a
display name, fork a `java` process with different VM parameters. Any exception
will cause the test to be marked as failed.

```java
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
```

When forking, you may also expect a non-zero exit value:

```java
public class SystemExit123 {

  @Main(
  	value = "123",
  	java = @Java(expectedExitValue = 123, options = {"-classpath", "${java.class.path}"}))              
  public static void main(String... args) {
    System.exit(Integer.parseInt(args[0]));
  }
}
```

## 📜 Brahms Single File Source Code TestEngine

**NOTE: Development of SingleFileSourceCodeTestEngine is continued here: https://github.com/sormuras/mainrunner**

Find `.java` source files that contain a `public static void main(String[] args)` method
and execute them. For details see [JEP 330](http://openjdk.java.net/jeps/330).

```java
class SingleFileSourceCodeProgram {
  public static void main(String... args) {
    // ...
  }
}
```
