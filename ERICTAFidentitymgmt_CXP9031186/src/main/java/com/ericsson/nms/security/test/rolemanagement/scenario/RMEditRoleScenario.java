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

import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.security.rolemanagement.utility.UiUtils.CSV_TEST_EDIT_ROLES;
import static com.ericsson.oss.testware.security.rolemanagement.utility.UiUtils.REDIRECT_ROLE_PAGE;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.nms.security.test.rolemanagement.scenario.utility.RMUtility;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.rolemanagement.flows.RMEditFlow;

public class RMEditRoleScenario extends TafTestBase {
    private final static String ROLE_TO_CREATE_CSV = "data/rolemanagement/testRoles.csv";
    private final static String ROLE_TO_CLEAN_UP_CSV = "data/rolemanagement/testEditRolesCleanUp.csv";

    @Inject
    private TestContext context;
    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;
    @Inject
    private RMEditFlow editRoleFlow;
    @Inject
    private RMUtility rmUtility;

    @TestId(id = "TORF-47097", title = "Role Management UI - Edit COM Role/Alias")
    @Test(groups = {"RFA"})
    public void testEditRoleScenario() throws Exception {
        final TestScenario scenario = TestScenarios.scenario("Role Management - Verify Edit Role page")
                .addFlow(loginLogoutFlow.loginWithRedirect(REDIRECT_ROLE_PAGE))
                .addFlow(editRoleFlow.verifyEditCOMRole(CSV_TEST_EDIT_ROLES))
                .addFlow(editRoleFlow.verifyEditCOMRoleAlias(CSV_TEST_EDIT_ROLES)) // beware this flow depends on verifyEditCOMRole!!!
                .addFlow(editRoleFlow.verifyEditCustomRoleMixed(CSV_TEST_EDIT_ROLES))   // beware this flow depends on verifyEditCOMRole!!!
                .addFlow(loginLogoutFlow.logout())
                .addFlow(loginLogoutFlow.cleanUpFlow())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

    @BeforeClass(groups = {"RFA"})
    public void setUp() {
        rmUtility.cleanUpAndCreateRolesUsingDataFiles(ROLE_TO_CREATE_CSV, ROLE_TO_CLEAN_UP_CSV);
    }

    @AfterClass(groups = {"RFA"},alwaysRun = true)
    public void tearDown() {
        rmUtility.cleanUpRolesUsingDataFiles(ROLE_TO_CLEAN_UP_CSV);
    }

}
