/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
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
import com.ericsson.oss.testware.security.usermanagement.steps.UserListTestSteps;

public class FederatedUserManagementFlow {
    @Inject
    private UserListTestSteps userListTestSteps;


    public TestStepFlowBuilder checkFederatedUsersTabFlow() {
        return flow("Federated Users UI check")
                .addTestStep(annotatedMethod(userListTestSteps, UserListTestSteps.StepIds.TEST_STEP_CHECK_FEDERATED_USERS_PAGE));
    }
}
