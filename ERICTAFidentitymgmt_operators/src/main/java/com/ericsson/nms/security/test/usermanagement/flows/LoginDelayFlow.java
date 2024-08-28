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

package com.ericsson.nms.security.test.usermanagement.flows;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.nms.security.test.usermanagement.teststep.LoginDelayTestSteps;

/**
 * This is a temporary flow for delay login in case synchronization in OpenIDM.
 */
public class LoginDelayFlow {

    @Inject
    private LoginDelayTestSteps loginDelayTestSteps;

    /**
     * This flow executes delay in login. This flow executes the following test steps:
     * LoginDelayTestSteps.StepIds.TEST_STEP_LOGIN_DELAY
     *
     * @return a flow which executes delay in login.
     */
    public TestStepFlowBuilder delayLogin() {
        return flow("Get Login Delay Flow").addTestStep(annotatedMethod(loginDelayTestSteps, LoginDelayTestSteps.StepIds.TEST_STEP_LOGIN_DELAY));
    }

}
