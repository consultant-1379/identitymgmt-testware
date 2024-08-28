/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.nms.security.test.usermanagement.teststep;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.oss.testware.security.authentication.tool.TafToolProvider;

public class BrowserUtilityTestSteps {
    @Inject
    private TafToolProvider tafToolProvider;

    /**
     * Causes the browser to go back in history.
     */
    @TestStep(id = StepIds.TEST_STEP_BROWSER_GO_BACK)
    public void browserGoBack() {
        getBrowserTab().back();
    }

    private BrowserTab getBrowserTab() {
        return tafToolProvider.getCurrentBrowserTab();
    }

    public static final class StepIds {
        public static final String TEST_STEP_BROWSER_GO_BACK = "browserGoBack";

        private StepIds() {}
    }
}
