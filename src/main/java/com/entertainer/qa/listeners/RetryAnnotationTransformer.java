package com.entertainer.qa.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Wires {@link RetryAnalyzer} into every {@code @Test} method automatically, so individual test
 * classes don't need {@code retryAnalyzer = RetryAnalyzer.class} boilerplate on each annotation.
 * Registered globally via the {@code listener} property in pom.xml's Surefire configuration.
 */
public class RetryAnnotationTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
