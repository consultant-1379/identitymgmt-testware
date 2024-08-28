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

package com.ericsson.nms.security.test.usermanagement.teststep;

import static com.ericsson.oss.testware.security.generic.utility.Assertion.assertEquals;
import static com.ericsson.oss.testware.security.generic.utility.Assertion.assertTrue;

import javax.inject.Inject;
import javax.inject.Provider;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.nms.security.test.usermanagement.operators.LauncherOperator;
import com.ericsson.oss.testware.security.authentication.tool.TafToolProvider;

/**
 * This are test steps for User Access Denied series of tests.
 */
public class LauncherTestSteps {

    @Inject
    private TafToolProvider tafToolProvider;

    @Inject
    private Provider<LauncherOperator> launcherOperatorProvider;

    /**
     * This test step checks visibility launcher page after click in access denied dialog.
     */
    @TestStep(id = StepIds.TEST_STEP_CHECK_LAUNCHER_PAGE)
    public void checkLauncherPage() {
        assertEquals("Launcher Title Page is not correct.", getLauncherOperator().getLauncherPageTitle(), "Application Launcher");
        assertTrue("We are not on launcher page", getBrowserTab().getCurrentUrl().contains("/#launcher"));
    }

    private BrowserTab getBrowserTab() {
        return tafToolProvider.getCurrentBrowserTab();
    }

    private LauncherOperator getLauncherOperator() {
        return launcherOperatorProvider.get();
    }

    /**
     * The test Step ID's.
     */
    public static final class StepIds {
        public static final String TEST_STEP_CHECK_LAUNCHER_PAGE = "checkLauncherPage";

        private StepIds() {
        }
    }
}
