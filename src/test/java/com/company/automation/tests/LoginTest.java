package com.company.automation.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.company.automation.annotations.TestCaseId;
import com.company.automation.base.BaseTest;
import com.company.automation.components.HeaderComponent;
import com.company.automation.config.ConfigReader;
import com.company.automation.pages.AccountPage;
import com.company.automation.pages.LoginPage;

/** TS_002 Login Functionality. Test case IDs match the Excel sheet. */
public class LoginTest extends BaseTest {

    private static final String NO_MATCH = "No match for E-Mail Address and/or Password";

    private HeaderComponent header;
    private LoginPage loginPage;

    /** Common precondition in every case: My Account > Login. */
    @BeforeMethod(alwaysRun = true)
    public void openLoginPage() {
        header = new HeaderComponent(page);
        loginPage = header.openLogin();
    }

    @Test(groups = {"smoke", "regression"}, description = "Valid credentials")
    @TestCaseId({"TC_LF_001","TC001_LoginValidUser"})
    public void validCredentialsLogIn() {
        assertTrue(page.url().contains("account/login"), "ER-1: should be on the Login page");
        AccountPage account = loginPage.loginAs(
                ConfigReader.get("user.email"), ConfigReader.get("user.password"));
        assertTrue(account.isDisplayed(), "ER-2: should land on the Account page");
    }
    
    @Test(dataProvider = "invalidCredentials", groups = {"regression"},
            description = "Invalid / missing credentials show the warning")
    @TestCaseId({"TC_LF_002", "TC_LF_003", "TC_LF_004", "TC_LF_005"})
    public void invalidCredentialsShowWarning(String caseId, String email, String password) {
        loginPage.attemptLogin(email, password);
        assertTrue(loginPage.isWarningDisplayed(), caseId + ": warning should be displayed");
        assertTrue(loginPage.getWarningMessage().contains(NO_MATCH), caseId + ": wrong warning text");
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        // TC_LF_004 uses a DEDICATED account: 5 failed attempts lock an OpenCart account for 1 hour,
        // which would otherwise break TC_LF_001 for everyone. See README "Test data rules".
        return new Object[][]{
                {"TC_LF_002", "xyzabc123@gmail.com", "xyzabc123"},
                {"TC_LF_003", "xyzabc123@gmail.com", ConfigReader.get("user.password")},
                {"TC_LF_004", ConfigReader.get("user.negative.email"), "xyzabc123"},
                {"TC_LF_005", "", ""}
        };
    }

    @Test(groups = {"regression"}, description = "Forgotten Password link is present and works")
    @TestCaseId("TC_LF_006")
    public void forgottenPasswordLinkWorks() {
        assertTrue(loginPage.isForgottenPasswordLinkDisplayed(), "ER-1: link should be displayed");
        loginPage.clickForgottenPassword();
        assertTrue(page.url().contains("account/forgotten"), "ER-2: should open Forgotten Password page");
    }
    

    @Test(groups = {"regression"}, description = "Placeholders on email and password fields")
    @TestCaseId("TC_LF_008")
    public void fieldsHavePlaceholders() {
        assertFalse(loginPage.getEmailPlaceholder().isBlank(), "E-Mail placeholder missing");
        assertFalse(loginPage.getPasswordPlaceholder().isBlank(), "Password placeholder missing");
    }

    @Test(groups = {"regression"}, description = "Back button after login keeps the user logged in")
    @TestCaseId("TC_LF_009")
    public void browserBackAfterLoginKeepsSession() {
        loginPage.loginAs(ConfigReader.get("user.email"), ConfigReader.get("user.password"));
        page.goBack();
        assertTrue(header.isOptionAvailable("Logout"), "ER-1: user should still be logged in");
    }

    @Test(groups = {"regression"}, description = "Back button after logout does not log the user in again")
    @TestCaseId("TC_LF_010")
    public void browserBackAfterLogoutDoesNotRestoreSession() {
        loginPage.loginAs(ConfigReader.get("user.email"), ConfigReader.get("user.password"));
        header.logout();
        page.goBack();
        assertFalse(header.isOptionAvailable("Logout"), "ER-1: user must not be logged in again");
    }

    @Test(groups = {"regression"}, description = "Password text is masked")
    @TestCaseId("TC_LF_013")
    public void passwordFieldIsMasked() {
        assertEquals(loginPage.getPasswordFieldType(), "password", "ER-1: input type must be 'password'");
    }
}
