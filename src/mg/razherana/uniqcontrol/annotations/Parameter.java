package mg.razherana.uniqcontrol.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.servlet.http.HttpServletResponse;

// This annotation is used to mark a parameter in a method as a parameter

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Parameter {
  String name() default "";

  String defaultValue() default "";

  // If required is set to true, the parameter must be present in the request or
  // else SC_NOT_FOUND will be returned
  boolean required() default true;

  // The error code to return if the parameter is required and not present
  int requiredError() default HttpServletResponse.SC_NOT_FOUND;

  boolean multiple() default false;
}
