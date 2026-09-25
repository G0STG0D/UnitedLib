package org.unitedlands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UnitedCommand {

    String name();

    String[] aliases() default {};

    String description() default "";

    String usage() default "";

    String permission() default "";

    boolean playerOnly() default false;

    int cooldown() default 0;

    String cooldownPermission() default "united.lands.admin";

    String[] requirePlugins() default {};

}
