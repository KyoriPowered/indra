package net.kyori.indra.test;

import net.kyori.mammoth.test.TestVariant;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An annotation providing Gradle 7.x test variants, so this version can be overridden when running on Java versions
 * that older Gradle versions do not support.
 */
@TestVariant(gradleVersion = "7.6.1")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface LegacyGradleVersion {
}
