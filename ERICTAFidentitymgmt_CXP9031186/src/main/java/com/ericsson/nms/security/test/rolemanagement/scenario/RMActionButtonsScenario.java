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
 -----------------------------------------------------------------------------*/
package com.ericsson.nms.security.test.rolemanagement.scenario;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.security.rolemanagement.utility.UiUtils.CSV_TEST_ROLES;
import static com.ericsson.oss.testware.security.rolemanagement.utility.UiUtils.REDIRECT_ROLE_PAGE;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.nms.security.test.rolemanagement.scenario.utility.RMUtility;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.rolemanagement.flows.RMActionButtonsFlow;
import com.ericsson.oss.testware.security.rolemanagement.flows.RMListFlow;

public class RMActionButtonsScenario extends TafTestBase {
    private final static String ROLE_TO_CREATE_CSV = "data/rolemanagement/testRoles.csv";

    @Inject
    private TestContext context;
    @Inject
    private RMListFlow rolesListFlow;
    @Inject
    private RMActionButtonsFlow actionButtonsFlow;
    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;
    @Inject
    private RMUtility rmUtility;


    @TestId(id = "TORF-47094_TORF-47097_only_toolbar", title = "Role Management UI - List Actions")
    @Test(groups = {"RFA"})
    public void testActionButtonsOnRoleManagementPage() throws Exception {
        final TestScenario scenario = scenario("Role Management Actions scenario")
                .addFlow(loginLogoutFlow.loginWithRedirect(REDIRECT_ROLE_PAGE))
                .addFlow(actionButtonsFlow.verifyCreateRoleButtonAction(CSV_TEST_ROLES))
                .addFlow(actionButtonsFlow.verifyEditRoleButtonAction(CSV_TEST_ROLES))
                .addFlow(rolesListFlow.verifyThatSelectedRolesAreRemembered())
                .addFlow(loginLogoutFlow.logout())
                .addFlow(loginLogoutFlow.cleanUpFlow())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

    @BeforeClass(groups = {"RFA"})
    public void setUp() {
        rmUtility.cleanUpAndCreateRolesUsingDataFiles(ROLE_TO_CREATE_CSV);
    }

    @AfterClass(groups = {"RFA"},alwaysRun = true)
    public void tearDown() {
        rmUtility.cleanUpRolesUsingDataFiles(ROLE_TO_CREATE_CSV);
    }
}
