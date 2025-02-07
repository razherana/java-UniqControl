package mg.razherana.uniqcontrol.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.servlet.http.HttpServletResponse;

// This annotation is used to mark a parameter in a method as a path parameter
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PathParameter {
  // The name of the path parameter
  String name() default "";

  boolean required() default true;

  // The error code to return if the parameter is required and not present
  int requiredError() default HttpServletResponse.SC_NOT_FOUND;

  String defaultValue() default "";
}
