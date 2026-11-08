package com.company.automation.components;

import com.company.automation.core.BasePage;
import com.company.automation.pages.LoginPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * The top bar is on every page, and almost every test case starts with
 * "My Account > Login/Register". Model it once as a component.
 * NOTE: locators are written for OpenCart 4.x. Verify them with `playwright codegen` or the MCP server.
 */
public class HeaderComponent extends BasePage {

    private final Locator myAccountMenu = page.getByTitle("My Account");
    private final Locator dropdown = page.locator("#top .dropdown-menu");

    public HeaderComponent(Page page) {
        super(page);
    }

    private Locator option(String name) {
        return dropdown.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(name).setExact(true));
    }

    public void openMyAccountMenu() {
        click(myAccountMenu);
    }

    public LoginPage openLogin() {
        openMyAccountMenu();
        click(option("Login"));
        return new LoginPage(page);
    }

    public void logout() {
        openMyAccountMenu();
        click(option("Logout"));
    }

    /** Opens the My Account menu and reports whether the given option (e.g. "Logout") is shown. */
    public boolean isOptionAvailable(String name) {
        openMyAccountMenu();
        return isVisibleWithin(option(name), 3_000);
    }
}
