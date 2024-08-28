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

import static com.ericsson.oss.testware.security.generic.utility.Properties.getProperty;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.oss.testware.security.authentication.tool.TafToolProvider;
import com.ericsson.oss.testware.security.usermanagement.utility.LoggerUtils;

/**
 * The base class of all User Management UI operators.
 */
abstract class UserManagementBaseOperator {
    public static final int WAIT_FOR_COMPONENT_LONG = getProperty("taf_ui.timeout.long", Integer.class);;
    public static final int WAIT_FOR_COMPONENT_MEDIUM = getProperty("taf_ui.timeout.medium", Integer.class);;
    public static final int WAIT_FOR_COMPONENT_SHORT = getProperty("taf_ui.timeout.short", Integer.class);;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementBaseOperator.class);

    @Inject
    protected TestContext context;
    @Inject
    private TafToolProvider tafToolProvider;

    /**
     * Returns page model based on specified class.
     * @param modelClass page model class
     * @return page model
     */
    protected <T extends GenericViewModel> T loadPageModel(final Class<T> modelClass) {
        return getBrowserTab().getView(modelClass);
    }

    /**
     * Returns current browser tab.
     * @return current browser tab
     */
    protected BrowserTab getBrowserTab() {
        return tafToolProvider.getCurrentBrowserTab();
    }

    /**
     * Waits until page is fully loaded.
     */
    protected abstract void waitUntilPageIsLoaded();

    /**
     * Waits until page is fully loaded by waiting for specific component on the page.
     * @param specificComponent component used to determine if page is loaded
     */
    protected void waitUntilPageIsLoaded(final UiComponent specificComponent) {
        getBrowserTab().waitUntilComponentIsDisplayed(specificComponent, WAIT_FOR_COMPONENT_LONG);
    }

    /**
     * Waits until page is fully loaded by waiting for specific components on the page.
     * @param specificComponents components used to determine if page is loaded
     */
    protected void waitUntilPageIsLoaded(final List<UiComponent> specificComponents) {
        for (final UiComponent component : specificComponents) {
            this.waitUntilPageIsLoaded(component);
        }
    }

    /**
     * Checks if delete dialog is present and has valid title.
     * @param warningBox the component for dialog
     * @param deleteConfirmationDialogTitle the component for dialog title
     * @return true if dialog is present and has valid title
     */
    protected boolean isDeleteDialogPresent(final UiComponent warningBox, final UiComponent deleteConfirmationDialogTitle) {
        getBrowserTab().waitUntilComponentIsDisplayed(warningBox, WAIT_FOR_COMPONENT_LONG);
        LOGGER.info(LoggerUtils.getLogForDisplayedUIComponent(warningBox, "deleteWarningBox"));
        final boolean isDeleteDialogPresent = warningBox.exists() && deleteConfirmationDialogTitle.isDisplayed();
        LOGGER.info("Method returned: " + isDeleteDialogPresent);
        return isDeleteDialogPresent;
    }

    /**
     * Checks if leave page dialog is present and has valid title.
     * @param leavePageBox the component for dialog
     * @param leavePageConfirmationDialogTitle the component for dialog title
     * @return true if dialog is present and has valid title
     */
    protected boolean isLeavePageDialogPresent(final UiComponent leavePageBox, final UiComponent leavePageConfirmationDialogTitle) {
        getBrowserTab().waitUntilComponentIsDisplayed(leavePageBox, WAIT_FOR_COMPONENT_LONG);
        LOGGER.info(LoggerUtils.getLogForDisplayedUIComponent(leavePageBox, "leavePageBox"));
        final boolean isLeavePageDialogPresent = leavePageBox.exists() && leavePageConfirmationDialogTitle.isDisplayed();
        LOGGER.info("Method returned: " + isLeavePageDialogPresent);
        return isLeavePageDialogPresent;
    }

    /**
     * Returns current page URL.
     */
    protected String getCurrentPageURL() {
        LOGGER.info("Return current page URL.");
        return getBrowserTab().getCurrentUrl();
    }

    /**
     * Returns a BrowserTab.
     */
    protected BrowserTab getCurrentBrowserTab() {
        return tafToolProvider.getCurrentBrowserTab();
    }

}
