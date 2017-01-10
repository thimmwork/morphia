package org.mongodb.morphia.annotations;


import org.mongodb.morphia.utils.IndexDirection;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Specified on fields that should be Indexed.
 *
 * @author Scott Hernandez
 */
@SuppressWarnings("deprecation")
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Indexed {
    /**
     * Options to apply to the index.  Use of this field will ignore any of the deprecated options defined on {@link Index} directly.
     */
    IndexOptions options() default @IndexOptions();

    /**
     * Indicates the type of the index (ascending, descending, geo2d); default is ascending
     */
    IndexDirection value() default IndexDirection.ASC;
}
