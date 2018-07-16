package de.sormuras.brahms.maingine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MainRepeatable.class)
public @interface Main {

  /** Argument array to be passed to the test run. */
  String[] value() default {};

  /** Display name of the test run. */
  String displayName() default "main(${ARGS})";

  /** Fork a new Java VM instance and launch the main class. */
  Java java() default @Java;
}
