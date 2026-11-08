package com.company.automation.core;

import com.company.automation.config.ConfigReader;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import java.util.List;

/** Launches the browser chosen by config: -Dbrowser=chromium|firefox|webkit, -Dheadless=true|false. */
public final class BrowserFactory {

    private BrowserFactory() { }

    public static Browser launch(Playwright playwright) {
        String name = ConfigReader.get("browser", "chromium").toLowerCase();
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(ConfigReader.getBoolean("headless", true));

        return switch (name) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit" -> playwright.webkit().launch(options);
            case "chromium" -> playwright.chromium()
                    .launch(options.setArgs(List.of("--disable-dev-shm-usage")));
            default -> throw new IllegalArgumentException("Unsupported browser: " + name);
        };
    }
}
