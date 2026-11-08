package com.company.automation.base;

import com.company.automation.config.ConfigReader;
import com.company.automation.core.BrowserFactory;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Lifecycle (thread-safe because testng.xml runs parallel="classes"):
 *   @BeforeClass  -> one Playwright + Browser per test class (expensive, so reused)
 *   @BeforeMethod -> fresh BrowserContext + Page per test (cheap, fully isolated cookies/storage)
 */
public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected Page page;

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;

    @BeforeClass(alwaysRun = true)
    public void launchBrowser() {
        playwright = Playwright.create();
        browser = BrowserFactory.launch(playwright);
    }

    @BeforeMethod(alwaysRun = true)
    public void openContext() {
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1366, 768));
        context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
        page = context.newPage();
        page.navigate(ConfigReader.get("base.url"));
    }

    @AfterMethod(alwaysRun = true)
    public void closeContext(ITestResult result) {
        try {
            if (result.getStatus() != ITestResult.SUCCESS) {
                String name = result.getTestClass().getRealClass().getSimpleName()
                        + "." + result.getMethod().getMethodName();
                byte[] png = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(png), "png");
                Path trace = Paths.get("target", "traces", name + "-" + System.currentTimeMillis() + ".zip");
                context.tracing().stop(new Tracing.StopOptions().setPath(trace));
                log.warn("Trace saved: {}  (open with: mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args=\"show-trace {}\")", trace, trace);
            } else {
                context.tracing().stop();
            }
        } catch (RuntimeException e) {
            log.warn("Could not collect failure artifacts: {}", e.getMessage());
        } finally {
            context.close();
        }
    }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
