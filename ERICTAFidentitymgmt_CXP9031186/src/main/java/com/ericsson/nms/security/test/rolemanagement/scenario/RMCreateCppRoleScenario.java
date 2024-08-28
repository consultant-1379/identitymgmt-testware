/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 -----------------------------------------------------------------------------*/
package com.ericsson.nms.security.test.rolemanagement.scenario;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.nms.security.test.rolemanagement.scenario.utility.RMUtility;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.rolemanagement.flows.RMCreateFlow;


public class RMCreateCppRoleScenario extends TafTestBase {
    private final static String ROLE_TO_CLEAN_UP_CSV = "data/rolemanagement/testRoles.csv";

    @Inject
    private TestContext context;
    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;
    @Inject
    private RMCreateFlow createRoleFlow;
    @Inject
    private RMUtility rmUtility;

    // Intentionally commented out as Task Profile creation is not supported yet by application
    //    @TestId(id = "TORF-47094_create_cpp", title = "Role Management UI - Create CPP Role")
    //    @Test(groups = {"RFA"})
    //    public void testCreateCOMRole() throws Exception {
    //        final TestScenario scenario = scenario("Role Management - Create CPP Role")
    //                .addFlow(loginLogoutFlow.loginWithRedirect(REDIRECT_ROLE_PAGE))
    //                .addFlow(createRoleFlow.verifyCppRoleCreate(CSV_TEST_ROLES))
    //                .addFlow(loginLogoutFlow.logout())
    //                .addFlow(loginLogoutFlow.cleanUpFlow())
    //                .withExceptionHandler(new UiExceptionHandler(context))
    //                .build();
    //
    //        start(scenario);
    //    }

    @BeforeClass(groups = {"RFA"})
    public void setUp() {
        rmUtility.cleanUpRolesUsingDataFiles(ROLE_TO_CLEAN_UP_CSV);
    }

    @AfterClass(groups = {"RFA"},alwaysRun = true)
    public void tearDown() {
        rmUtility.cleanUpRolesUsingDataFiles(ROLE_TO_CLEAN_UP_CSV);
    }

}
