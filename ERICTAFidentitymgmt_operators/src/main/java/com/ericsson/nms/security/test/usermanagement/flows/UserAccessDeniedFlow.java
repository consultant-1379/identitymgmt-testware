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
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.nms.security.test.usermanagement.teststep.LauncherTestSteps;
import com.ericsson.oss.testware.security.authentication.steps.LoginLogoutUiTestSteps;
import com.ericsson.oss.testware.security.usermanagement.steps.UserAccessDeniedTestSteps;

/**
 * This is a test flow for checking user profile displaying and editing.
 */
public class UserAccessDeniedFlow {
    /**
     * Datasource constants.
     */
    public static final String DATA_SOURCE_CHECK_USER_ACCESS_DENIED = "DATA_SOURCE_CHECK_USER_ACCESS_DENIED";

    /**
     * Other constants.
     */
    public static final String USER_PROFILE_APP_NAME_USERMANAGEMENT = "/#usermanagement";
    public static final String USER_PROFILE_APP_NAME_USERMGMTPROFILE = "/#usermanagement/usermgmtprofile/create";
    public static final String USER_PROFILE_APP_NAME_USERMGMTCHANGEPASS = "/#usermgmtchangepass";
    public static final String REDIRECT_URL = "redirectUrl";

    @Inject
    private UserAccessDeniedTestSteps userAccessDeniedTestSteps;

    @Inject
    private LauncherTestSteps launcherTestSteps;

    @Inject
    private LoginLogoutUiTestSteps loginUITestSteps;

    /**
     * This flow checks user access denied for page in application usermanagement . It checks if:
     * - logged in user can not access usermanagement profile page (role different then administrator)
     * <p>
     * Flow uses following data source: DATA_SOURCE_CHECK_USER_ACCESS_DENIED
     * <p>
     * The datasource must contain the following mandatory columns:
     * {@value com.ericsson.oss.testware.security.usermanagement.steps.UMTestStepInputs#TEST_INPUT_USERNAME}
     * {@value com.ericsson.oss.testware.security.usermanagement.steps.UMTestStepInputs#TEST_INPUT_PASSWORD}
     * <p>
     * This flow has the following test steps:<br>
     * {@link com.ericsson.oss.testware.security.authentication.steps.LoginLogoutUiTestSteps#TEST_STEP_LOGIN_REDIRECT}<br>
     * {@link UserAccessDeniedTestSteps.StepIds#TEST_STEP_CHECK_ACCESS_DENIED}<br>
     * {@link LauncherTestSteps.StepIds#TEST_STEP_CHECK_LAUNCHER_PAGE}<br>
     * {@link com.ericsson.oss.testware.security.authentication.steps.LoginLogoutUiTestSteps#TEST_STEP_LOGOUT}<br>
     * It is executed 3 times. Each one for other application (usermanagement, usermgmtprofile, usermgmtchangepass)
     * <p>
     *
     * @return a flow which executes checking user access denied.
     */
    public TestStepFlowBuilder checkUserAccessDenied() {
        return flow("Check user access denied")
                .addSubFlow(getCheckAccessDeniedSubflow(USER_PROFILE_APP_NAME_USERMANAGEMENT))
                .addSubFlow(getCheckAccessDeniedSubflow(USER_PROFILE_APP_NAME_USERMGMTPROFILE))
                .addSubFlow(getCheckAccessDeniedSubflow(USER_PROFILE_APP_NAME_USERMGMTCHANGEPASS))
                .withDataSources(dataSource(DATA_SOURCE_CHECK_USER_ACCESS_DENIED).bindTo(AVAILABLE_USERS));
    }

    private TestStepFlowBuilder getCheckAccessDeniedSubflow(final String appName) {
        return flow("Get Access Denied Flow")
                .addTestStep(annotatedMethod(loginUITestSteps, LoginLogoutUiTestSteps.TEST_STEP_LOGIN_REDIRECT).withParameter(REDIRECT_URL, appName))
                .addTestStep(annotatedMethod(userAccessDeniedTestSteps, UserAccessDeniedTestSteps.StepIds.TEST_STEP_CHECK_ACCESS_DENIED))
                .addTestStep(annotatedMethod(launcherTestSteps, LauncherTestSteps.StepIds.TEST_STEP_CHECK_LAUNCHER_PAGE))
                .addTestStep(annotatedMethod(loginUITestSteps, LoginLogoutUiTestSteps.TEST_STEP_LOGOUT));
    }

}
