package com.entertainer.qa.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Console-level visibility into each test's lifecycle - useful in CI logs where the Allure report
 * isn't viewed until the build finishes. Registered suite-wide via {@code testng.xml}'s
 * {@code <listeners>} block rather than per class, so every test in the suite is covered without
 * each test class needing its own {@code @Listeners} annotation.
 */
public class TestExecutionListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        System.out.println("[" + context.getName() + "] started - " + context.getAllTestMethods().length + " test method(s)");
    }

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("[START] " + testId(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("[PASS]  " + testId(result) + " (" + durationMs(result) + " ms)");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("[FAIL]  " + testId(result) + " (" + durationMs(result) + " ms) - "
                + (result.getThrowable() != null ? result.getThrowable().getMessage() : "no exception message"));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("[SKIP]  " + testId(result));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        System.out.println("[FLAKY] " + testId(result) + " (" + durationMs(result) + " ms)");
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println(String.format(
                "[%s] finished - passed=%d, failed=%d, skipped=%d",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size()));
    }

    private static String testId(ITestResult result) {
        return result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
    }

    private static long durationMs(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }
}
