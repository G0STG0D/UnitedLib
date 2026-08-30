package org.unitedlands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.RECORD_COMPONENT })
public @interface UnitedSetting {

    String key();
    String def() default "";

}
