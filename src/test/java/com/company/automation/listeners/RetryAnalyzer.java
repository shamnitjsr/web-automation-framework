package com.company.automation.listeners;

import com.company.automation.config.ConfigReader;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/** Retries a failed test up to retry.count times (default 1). Track retried tests as flaky; don't hide them. */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempt = 0;
    private final int max = ConfigReader.getInt("retry.count", 1);

    @Override
    public boolean retry(ITestResult result) {
        return attempt++ < max;
    }
}
