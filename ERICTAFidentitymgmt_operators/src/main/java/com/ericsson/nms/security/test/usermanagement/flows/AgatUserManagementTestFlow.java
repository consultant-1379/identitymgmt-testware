/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
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
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.fromTestStepResult;
import static com.ericsson.nms.security.test.usermanagement.teststep.AgatUserManagementTestStep.TestStepId.CHANGE_PASSWORD_TESTSTEP;
import static com.ericsson.nms.security.test.usermanagement.teststep.AgatUserManagementTestStep.TestStepId.WAIT_TESTSTEP;
import static com.ericsson.oss.testware.security.authentication.steps.LoginLogoutRestTestSteps.TEST_INPUT_ERROR_USERS;
import static com.ericsson.oss.testware.security.authentication.steps.LoginLogoutRestTestSteps.TEST_STEP_CHECK_USERLOGIN_RESPONSE;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.nms.security.test.usermanagement.teststep.AgatUserManagementTestStep;
import com.ericsson.oss.testware.security.authentication.steps.LoginLogoutRestTestSteps;

public class AgatUserManagementTestFlow {
    public static final String LOCKOUT_SEQUENCE_DATASOURCE = "lockupSequenceDataSource";

    @Inject
    private AgatUserManagementTestStep agatUserManagementTestStep;
    @Inject
    private LoginLogoutRestTestSteps loginLogoutRestTestStep;

    public TestStepFlow loginLockSequence() {
        final TestStepFlowBuilder testFlowBuilder = new TestStepFlowBuilder("Login Lock Sequence");
        testFlowBuilder.addTestStep(annotatedMethod(agatUserManagementTestStep, WAIT_TESTSTEP));
        testFlowBuilder.addTestStep(annotatedMethod(agatUserManagementTestStep, CHANGE_PASSWORD_TESTSTEP));
        testFlowBuilder.addTestStep(annotatedMethod(loginLogoutRestTestStep, TEST_STEP_CHECK_USERLOGIN_RESPONSE)
                .withParameter(TEST_INPUT_ERROR_USERS, fromTestStepResult(CHANGE_PASSWORD_TESTSTEP)));
        testFlowBuilder.withDataSources(dataSource(LOCKOUT_SEQUENCE_DATASOURCE));
        return testFlowBuilder.build();
    }
}
