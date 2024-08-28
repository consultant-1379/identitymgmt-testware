/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.nms.security.test.usermanagement.operators;

import com.ericsson.cifwk.taf.ui.InternalDriverAware;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.nms.security.test.usermanagement.models.LauncherModel;

import java.util.concurrent.TimeUnit;

/**
 * The operator, which operates on launcher page in User Access Denied test.
 */
@Operator
class LauncherOperatorImpl extends UserManagementBaseOperator implements LauncherOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LauncherOperatorImpl.class);

    @Override
    public String getLauncherPageTitle() {
        waitUntilPageIsLoaded();
        //getBrowserTab().waitUntilComponentIsDisplayed(getModel().getLauncherTitle(), WAIT_FOR_COMPONENT_LONG);
        //return getModel().getLauncherTitle().getText();
        WebElement launcherPageTitleCore = this.waitUntilUsingDriver(getModel().getLauncherTitle_Selector(),getModel().getLauncherTitle_SelectorType(),WAIT_FOR_COMPONENT_LONG);
        LOGGER.info("Launcher page being:: " + launcherPageTitleCore.toString());
        return launcherPageTitleCore.getText();
    }

    @Override
    protected void waitUntilPageIsLoaded() {
        //getBrowserTab().waitUntilComponentIsDisplayed(getModel().getLauncherPage(), WAIT_FOR_COMPONENT_LONG);
        this.waitUntilUsingDriver(getModel().getLauncherPage_Selector(),getModel().getLauncherPage_SelectorType(),WAIT_FOR_COMPONENT_LONG);
        LOGGER.info("Page is loaded.");
    }

    private LauncherModel getModel() {
        if (context.getAttribute(LauncherModel.MODEL) == null) {
            context.setAttribute(LauncherModel.MODEL, loadPageModel(LauncherModel.class));
        }
        return context.getAttribute(LauncherModel.MODEL);
    }

    // Workaround to resolve TimedOut issue which makes selenium core calls to access a WebElement: TORF-463964
    private WebElement waitUntilUsingDriver(final String component, final String componentType, final long timeout) {
        WebElement webElement = null;
        try {
            WebDriver webDriver = ((InternalDriverAware) getBrowserTab()).getInternalDriver();
            WebDriverWait waitLauncherElement = new WebDriverWait(webDriver, TimeUnit.MILLISECONDS.toSeconds(timeout));
            if (componentType.equals("CSS")) {
                LOGGER.info("Selector Component " );
                webElement = waitLauncherElement.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(component)));
            } else {
                webElement = waitLauncherElement.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(component)));
            }
            return webElement;
        }catch( Exception e){
            LOGGER.info("Exception occurred while waiting for the component : " + e);
            getBrowserTab().takeScreenshot("Exception");
            throw e;
        }
    }

}
