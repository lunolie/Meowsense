package dev.hez.meowsense.event.bus.annotations;

import dev.hez.meowsense.event.bus.Priorities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EventLink { byte value() default Priorities.MEDIUM; }