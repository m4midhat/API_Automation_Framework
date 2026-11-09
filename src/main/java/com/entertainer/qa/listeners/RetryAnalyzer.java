package com.entertainer.qa.listeners;

import com.entertainer.qa.config.ConfigManager;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test a small, configurable number of times before letting it fail for good.
 * Guards against transient network flakiness when hitting third-party public sandbox APIs, rather
 * than masking genuine assertion failures - each retry is still recorded by the
 * {@link io.qameta.allure.testng.AllureTestNg} listener.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempts = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxAttempts = ConfigManager.getInt("http.retry.max.attempts", 2);
        if (attempts < maxAttempts) {
            attempts++;
            return true;
        }
        return false;
    }
}
