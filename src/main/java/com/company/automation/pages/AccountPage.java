package com.company.automation.pages;

import com.company.automation.core.BasePage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

public class AccountPage extends BasePage {

    public AccountPage(Page page) {
        super(page);
    }

    /** True when the browser reaches the Account page (route=account/account). */
//    public boolean isDisplayed() {
//        try {
//            waitForUrlContains("account/account");
//            return true;
//        } catch (PlaywrightException e) {
//            return false;
//        }
//    }
    
    public boolean isDisplayed() {
        try {
            page.waitForURL(java.util.regex.Pattern.compile(".*account/account.*"),
                    new Page.WaitForURLOptions()
                            .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.COMMIT)
                            .setTimeout(30_000));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }
}
