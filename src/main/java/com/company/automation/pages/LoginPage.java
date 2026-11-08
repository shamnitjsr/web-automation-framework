package com.company.automation.pages;

import com.company.automation.core.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/** Login page. Only locators and user actions live here; assertions live in the tests. */
public class LoginPage extends BasePage {

    private final Locator emailField = page.locator("#input-email");
    private final Locator passwordField = page.locator("#input-password");
    private final Locator loginButton = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Login"));
    private final Locator warning = page.locator(".alert-danger");
   
    private final Locator forgottenPasswordLink = page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Forgotten Password")).first();

    public LoginPage(Page page) {
        super(page);
    }

    /** Submit valid credentials and land on the Account page. */
    public AccountPage loginAs(String email, String password) {
        submit(email, password);
        return new AccountPage(page);
    }

    /** Submit credentials expecting to stay on the login page (negative scenarios). */
    public LoginPage attemptLogin(String email, String password) {
        submit(email, password);
        return this;
    }

    private void submit(String email, String password) {
        fill(emailField, email);
        fill(passwordField, password);
        click(loginButton);
    }

    public boolean isWarningDisplayed() {
        return isVisibleWithin(warning, 5_000);
    }

    public String getWarningMessage() {
        waitForVisible(warning);
        return getText(warning);
    }

    public String getPasswordFieldType() {
        return getAttribute(passwordField, "type");
    }

    public String getEmailPlaceholder() {
        return getAttribute(emailField, "placeholder");
    }

    public String getPasswordPlaceholder() {
        return getAttribute(passwordField, "placeholder");
    }

    public boolean isForgottenPasswordLinkDisplayed() {
        return isVisible(forgottenPasswordLink);
    }

    public void clickForgottenPassword() {
        click(forgottenPasswordLink);
    }
}
