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

package com.ericsson.nms.security.test.usermanagement.flows;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.nms.security.test.usermanagement.teststep.BrowserUtilityTestSteps;
import com.ericsson.oss.testware.launcher.teststeps.LauncherTestSteps;
import com.ericsson.oss.testware.launcher.teststeps.LauncherUiTestSteps;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.usermanagement.steps.UserListTestSteps;

public class BrowserBackOnUserManagementFlow {
    @Inject
    private BrowserUtilityTestSteps browserUtilityTestSteps;

    @Inject
    private LauncherUiTestSteps launcherUiTestSteps;

    @Inject
    private com.ericsson.nms.security.test.usermanagement.teststep.LauncherTestSteps launcherTestSteps;

    @Inject
    private UserListTestSteps userListTestSteps;

    @Inject
    private LoginLogoutUiFlows loginFlows;

    public TestStepFlowBuilder checkBrowserBackOnUserManagementFlow() {
        return flow("Check browser Back button")
                .addSubFlow(loginFlows.login())
                .addTestStep(annotatedMethod(launcherUiTestSteps, LauncherTestSteps.TestStepIds.OPEN_APPLICATION_STEP)
                                     .withParameter("applicationId", "user_management"))
                .addTestStep(annotatedMethod(userListTestSteps, UserListTestSteps.StepIds.TEST_STEP_WAIT_UNTIL_PAGE_IS_LOADED))
                .addTestStep(annotatedMethod(browserUtilityTestSteps, BrowserUtilityTestSteps.StepIds.TEST_STEP_BROWSER_GO_BACK))
                .addTestStep(annotatedMethod(launcherTestSteps, com.ericsson.nms.security.test.usermanagement.teststep
                        .LauncherTestSteps.StepIds.TEST_STEP_CHECK_LAUNCHER_PAGE))
                .addSubFlow(loginFlows.logout());
    }
}
