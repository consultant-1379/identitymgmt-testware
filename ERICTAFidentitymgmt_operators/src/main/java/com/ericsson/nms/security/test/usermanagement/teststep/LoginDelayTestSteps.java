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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.TestStep;


/**
 * This are test steps for delay login.
 */
public class LoginDelayTestSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginDelayTestSteps.class);

    /**
     * This test step delay login in case synchronization in OpenIDM.
     */
    @TestStep(id = StepIds.TEST_STEP_LOGIN_DELAY)
    public void loginDelay() {
        final long time = 30000;

        try {
            LOGGER.info("Waiting 30 seconds...");
            Thread.sleep(time);
        } catch (final InterruptedException ie) {
            LOGGER.error("Interrupted Exception in loginDelay  ...");
            LOGGER.error(ie.getMessage());
        }
    }

    /**
     * The test Step ID's.
     */
    public static final class StepIds {
        public static final String TEST_STEP_LOGIN_DELAY = "loginDelay";

        private StepIds() {
        }
    }
}
