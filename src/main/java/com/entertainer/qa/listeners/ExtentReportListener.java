package com.entertainer.qa.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.entertainer.qa.config.ConfigManager;
import com.entertainer.qa.reporting.ExtentManager;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Drives the ExtentReports HTML report (a second, standalone report alongside Allure): one
 * {@link ExtentTest} node per test method, categorised by which API it targets and tagged with
 * the report author, with the full failure detail logged on failure.
 */
public class ExtentReportListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = ExtentManager.startTest(testId(result), result.getMethod().getDescription());
        test.assignCategory(apiCategory(result));
        test.assignAuthor(ConfigManager.get("report.author", "Midhat Rosull Chughtai"));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentManager.getTest().log(Status.PASS, "Passed in " + durationMs(result) + " ms");
        ExtentManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        if (result.getThrowable() != null) {
            test.log(Status.FAIL, result.getThrowable());
        } else {
            test.log(Status.FAIL, "Failed in " + durationMs(result) + " ms");
        }
        ExtentManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.log(Status.SKIP, result.getThrowable() != null
                    ? result.getThrowable().getMessage()
                    : "Skipped");
            ExtentManager.removeTest();
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        ExtentManager.getTest().log(Status.WARNING, "Failed but within the configured success percentage");
        ExtentManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        // Flushed once for the whole suite by SuiteLifecycleListener.onFinish, not here - this
        // fires once per <test> tag and an early flush would just mean extra disk I/O.
    }

    private static String testId(ITestResult result) {
        return result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
    }

    private static long durationMs(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }

    private static String apiCategory(ITestResult result) {
        String packageName = result.getTestClass().getRealClass().getPackageName();
        if (packageName.contains(".reqres")) {
            return "ReqRes";
        }
        if (packageName.contains(".booker")) {
            return "Restful-Booker";
        }
        if (packageName.contains(".jsonplaceholder")) {
            return "JSONPlaceholder";
        }
        return "Uncategorized";
    }
}
