package org.xandercat.cat.back.swing.zenput.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.xandercat.cat.back.swing.zenput.validator.TimeDurationValidator;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValidateTimeDuration {
	Class<?> validatorClass() default TimeDurationValidator.class;
}
