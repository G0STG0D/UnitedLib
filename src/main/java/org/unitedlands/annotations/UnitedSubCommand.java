package org.unitedlands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UnitedSubCommand {

    String name();

    Class<?> parent();

    String[] aliases() default {};

    String description() default "";

    String usage() default "";

    String permission() default "";

    boolean playerOnly() default false;

    boolean catchAll() default false;

}
