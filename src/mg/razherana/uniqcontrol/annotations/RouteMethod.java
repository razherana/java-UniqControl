package mg.razherana.uniqcontrol.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RouteMethod {
    String value() default "";

    /**
     * The syntax is {"method:/path/web"}
     */
    String[] routes() default {};
}
