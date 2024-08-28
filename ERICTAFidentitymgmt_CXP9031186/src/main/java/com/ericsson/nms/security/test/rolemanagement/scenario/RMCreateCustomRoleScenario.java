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

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.security.rolemanagement.utility.UiUtils.CSV_TEST_ROLES;
import static com.ericsson.oss.testware.security.rolemanagement.utility.UiUtils.REDIRECT_ROLE_PAGE;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.nms.security.test.rolemanagement.scenario.utility.RMUtility;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.rolemanagement.flows.RMCreateFlow;

public class RMCreateCustomRoleScenario extends TafTestBase {
    private final static String ROLE_TO_CREATE_CSV = "data/rolemanagement/testComRoles.csv";
    private final static String ROLE_TO_CLEAN_UP_CSV = "data/rolemanagement/testRoles.csv";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/rolemanagement/testDataSource.csv";

    @Inject
    private TestContext context;
    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;
    @Inject
    private RMCreateFlow createRoleFlow;
    @Inject
    private RMUtility rmUtility;

    @Test(groups = {"RFA", "RFA250"})
    @TestSuite
    public void testCreateCustomRole() throws Exception {
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));
        final TestScenario scenario = dataDrivenScenario("Role Management - Create Custom Role")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("2")))
                .addFlow(loginLogoutFlow.loginWithRedirect(REDIRECT_ROLE_PAGE))
                .addFlow(createRoleFlow.verifyCustomRoleMixedCreate(CSV_TEST_ROLES))
                .addFlow(loginLogoutFlow.logout())
                .addFlow(loginLogoutFlow.cleanUpFlow())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

    @BeforeClass(groups = {"RFA", "RFA250"})
    public void setUp() {
        rmUtility.cleanUpAndCreateRolesUsingDataFiles(ROLE_TO_CREATE_CSV, ROLE_TO_CLEAN_UP_CSV);
    }

    @AfterClass(groups = {"RFA", "RFA250"},alwaysRun = true)
    public void tearDown() {
        rmUtility.cleanUpRolesUsingDataFiles(ROLE_TO_CLEAN_UP_CSV);
    }

}
