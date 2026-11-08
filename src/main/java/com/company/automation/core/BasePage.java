package com.company.automation.core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Parent class for every page object.
 * Holds the Playwright Page and wraps common actions so that:
 *  - page classes stay short and readable
 *  - logging, waits and error handling live in ONE place
 *  - a change in approach (e.g. extra wait) is made once
 *
 * Rule: BasePage contains generic behaviour only. No app-specific locators here.
 */
public abstract class BasePage {

    protected final Page page;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final double DEFAULT_TIMEOUT_MS = 15_000;

    protected BasePage(Page page) {
        this.page = page;
        this.page.setDefaultTimeout(DEFAULT_TIMEOUT_MS);
    }

    // ---------- Navigation ----------

    protected void navigateTo(String url) {
        log.info("Navigating to {}", url);
        page.navigate(url);
        waitForPageLoad();
    }

    protected void waitForPageLoad() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    protected void waitForNetworkIdle() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    protected void refresh() {
        log.info("Refreshing page");
        page.reload();
    }

    protected void goBack() {
        page.goBack();
    }

    public String getTitle() {
        return page.title();
    }

    public String getCurrentUrl() {
        return page.url();
    }

    protected void waitForUrlContains(String partialUrl) {
        page.waitForURL("**" + partialUrl + "**");
    }

    // ---------- Actions ----------

    protected void click(Locator locator) {
        log.info("Click: {}", locator);
        locator.click();
    }

    protected void doubleClick(Locator locator) {
        log.info("Double click: {}", locator);
        locator.dblclick();
    }

    protected void rightClick(Locator locator) {
        locator.click(new Locator.ClickOptions().setButton(com.microsoft.playwright.options.MouseButton.RIGHT));
    }

    protected void fill(Locator locator, String text) {
        // Do not log the value: it may be a password or other sensitive data
        log.info("Fill: {}", locator);
        locator.fill(text);
    }

    /** Types key by key. Use only when fill() does not trigger the app's key events. */
    protected void type(Locator locator, String text) {
        locator.pressSequentially(text);
    }

    protected void clear(Locator locator) {
        locator.clear();
    }

    protected void pressKey(Locator locator, String key) {
        log.info("Press {} on {}", key, locator);
        locator.press(key);
    }

    protected void hover(Locator locator) {
        locator.hover();
    }

    protected void check(Locator locator) {
        locator.check();
    }

    protected void uncheck(Locator locator) {
        locator.uncheck();
    }

    protected void selectByValue(Locator dropdown, String value) {
        dropdown.selectOption(value);
    }

    protected void selectByLabel(Locator dropdown, String label) {
        dropdown.selectOption(new com.microsoft.playwright.options.SelectOption().setLabel(label));
    }

    protected void uploadFile(Locator fileInput, String filePath) {
        fileInput.setInputFiles(Paths.get(filePath));
    }

    protected void scrollIntoView(Locator locator) {
        locator.scrollIntoViewIfNeeded();
    }

    // ---------- Reading ----------

    protected String getText(Locator locator) {
        return locator.innerText().trim();
    }

    protected String getValue(Locator locator) {
        return locator.inputValue();
    }

    protected String getAttribute(Locator locator, String attribute) {
        return locator.getAttribute(attribute);
    }

    protected List<String> getAllTexts(Locator locator) {
        return locator.allInnerTexts();
    }

    protected int count(Locator locator) {
        return locator.count();
    }

    // ---------- State checks ----------

    protected boolean isVisible(Locator locator) {
        return locator.isVisible();
    }

    protected boolean isEnabled(Locator locator) {
        return locator.isEnabled();
    }

    protected boolean isChecked(Locator locator) {
        return locator.isChecked();
    }

    // ---------- Waits ----------

    protected void waitForVisible(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    protected void waitForHidden(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
    }

    protected void waitForVisible(Locator locator, double timeoutMs) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
    }

    /** Returns true if the element appears within the timeout, without throwing. */
    protected boolean isVisibleWithin(Locator locator, double timeoutMs) {
        try {
            waitForVisible(locator, timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------- Screenshots ----------

    public Path takeScreenshot(String name) {
        Path path = Paths.get("screenshots", name + "_" + System.currentTimeMillis() + ".png");
        page.screenshot(new Page.ScreenshotOptions().setPath(path).setFullPage(true));
        log.info("Screenshot saved: {}", path);
        return path;
    }

    // ---------- JavaScript ----------

    protected Object executeJs(String script) {
        return page.evaluate(script);
    }
}
