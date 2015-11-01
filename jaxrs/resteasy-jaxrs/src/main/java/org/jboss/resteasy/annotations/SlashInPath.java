/*
 * Copyright (c) Smals
 */
package org.jboss.resteasy.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.PathParam;


/**
 * Disables automatic encoding of the '/' path separator in values bound using
 * {@link PathParam}
 * Using this annotation on a method will disable decoding for all parameters.
 * Using this annotation on a class will disable decoding for all parameters of
 * all methods.
 *
 * @author Thomas De Smedt
 * @see PathParam
 * @since 3.1
 */
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SlashInPath {
}
