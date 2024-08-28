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
 *----------------------------------------------------------------------------*/

package com.ericsson.nms.security.test.usermanagement_2_0.scenario;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_CREATE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USER_TO_CLEAN_UP;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.USER;
import static com.ericsson.oss.testware.security.usermanagement.flows.UserListFlow.EXPECTED_TEST_RESULTS;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.oss.testware.enmbase.data.CommonDataSources;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.ericsson.oss.testware.security.usermanagement.data.UserListSelectionTestResult;
import com.ericsson.oss.testware.security.usermanagement.data.UserListTestResult;
import com.ericsson.oss.testware.security.usermanagement.flows.UserListFlow;

public class UserListScenario extends TafTestBase {
    static final String USERS_CSV = "data/usermanagement_2_0/Users.csv";
    private static final String ADMIN_USER_CSV = "data/usermanagement_2_0/AdminUser.csv";
    private static final String USER_MGMT_APP_NAME_FILTER = "/#usermanagement/?pagenumber=1&pagesize=5&filter={\"username\"%3A\"UserManagement_ListUsers\"}";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @Inject
    private TestContext context;

    @Inject
    private UserManagementTestFlows userManagementTestFlows;

    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;

    @Inject
    private UserListFlow userListFlow;

    @Inject
    private GimCleanupFlows gimCleanupFlows;

    @BeforeClass(groups = {"RFA"})
    public void setUp(){
        CommonDataSources.initializeDataSources();
        context.addDataSource(USERS_TO_CREATE, TafDataSources.shared(TafDataSources.fromCsv(USERS_CSV)));

        final TestScenario scenario = scenario("Create test users")
                .addFlow(userManagementTestFlows.createUser())
                .build();

        start(scenario);
    }

    @AfterClass(groups = {"RFA"}, alwaysRun = true)
    public void tearDown(){
        context.addDataSource(USER_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(USERS_CSV)));

        final TestScenario scenario = scenario("Delete test users")
                .addFlow(gimCleanupFlows.cleanUp(USER))
                .build();

        start(scenario);

        context.removeDataSource(AVAILABLE_USERS);
    }

    @Test(groups = {"Acceptance", "RFA"})
    @TestSuite
    public void testUserList() throws Exception {
        context.addDataSource(AVAILABLE_USERS, TafDataSources.shared(TafDataSources.fromCsv(ADMIN_USER_CSV)));
        context.addDataSource(EXPECTED_TEST_RESULTS, TafDataSources.fromClass(UserListTestResult.class));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("User List Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("9")))
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME_FILTER))
                .addFlow(userListFlow.checkUserList())
                .addFlow(loginLogoutFlow.logout())
                .addFlow(loginLogoutFlow.cleanUpFlow())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

    @Test(groups = {"Acceptance", "RFA"})
    @TestSuite
    public void testUsersSelection() throws Exception {
        context.addDataSource(AVAILABLE_USERS, TafDataSources.shared(TafDataSources.fromCsv(ADMIN_USER_CSV)));
        context.addDataSource(EXPECTED_TEST_RESULTS, TafDataSources.fromClass(UserListSelectionTestResult.class));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));
        final TestScenario scenario = dataDrivenScenario("User List Selection Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("10")))
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME_FILTER))
                .addFlow(userListFlow.checkSimpleUsersSelection())
                .addFlow(userListFlow.checkSelectAllUsers())
                .addFlow(loginLogoutFlow.logout())
                .addFlow(loginLogoutFlow.cleanUpFlow())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

}
