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

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.ericsson.oss.testware.security.usermanagement.flows.UserListFlow;


public class EditUserForForceAndRevokePassChangeScenario extends TafTestBase {
    public final static String USER_FOR_FORCE_REVOKE_PASSW_CSV = "data/usermanagement_2_0/UserForForceAndRevokePasswChange.csv";
    public final static String FORCE_REVOKE_PASSWORD_EDIT_MULTIPLE_CSV = "data/usermanagement_2_0/ForceAndRevokePasswChangeEditMultiple.csv";
    public final static String FORCE_REVOKE_PASSWORD_EDIT_CSV = "data/usermanagement_2_0/ForceAndRevokePasswChangeEdit.csv";
    public final static String USER_MGMT_APP_NAME_PAGIN_500 = "/#usermanagement/?pagenumber=1&pagesize=500";
    public final static String USERS_REVOKE_FORCE_PASSWD_CHANGE_MULTIPLE = "usersForRevokeForcePasswdChangeMultiple";
    public final static String USERS_REVOKE_FORCE_PASSWD_CHANGE = "usersForRevokeForcePasswdChange";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @Inject
    private TestContext context;

    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;

    @Inject
    private UserManagementTestFlows userManagementTestFlows;

    @Inject
    private UserListFlow userListFlow;

    @Inject
    private GimCleanupFlows gimCleanupFlows;

    @BeforeClass(groups = { "RFA" })
    public void setUp() throws TimeoutException, InterruptedException {
        context.addDataSource(USERS_TO_CREATE, fromCsv(USER_FOR_FORCE_REVOKE_PASSW_CSV));

        final TestScenario scenario = scenario("Create users for Revoke and Force Passwd change").addFlow(userManagementTestFlows.createUser())
                .build();

        start(scenario);

        context.removeDataSource(AVAILABLE_USERS);
    }

    @Test(groups = { "Acceptance", "Revoke and Force Passwd change" })
    @TestSuite
    public void testRevokeAndForcePasswordChange() {

        context.addDataSource(USERS_TO_CREATE, fromCsv(USER_FOR_FORCE_REVOKE_PASSW_CSV));
        context.addDataSource(USERS_REVOKE_FORCE_PASSWD_CHANGE_MULTIPLE, fromCsv(FORCE_REVOKE_PASSWORD_EDIT_MULTIPLE_CSV));
        context.addDataSource(USERS_REVOKE_FORCE_PASSWD_CHANGE, fromCsv(FORCE_REVOKE_PASSWORD_EDIT_CSV));

        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));
        final TestScenario scenario = dataDrivenScenario("Revoke and Force Passwd change Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("18")))
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME_PAGIN_500))
                .addFlow(userListFlow.checkForcePasswordChangeColoumnMultipleInList())
                .addFlow(userListFlow.checkForcePasswordChangeColoumnInList())
                .addFlow(loginLogoutFlow.logout())
                .withExceptionHandler(new UiExceptionHandler(context)).build();

        start(scenario);
    }

    @AfterClass(groups = { "RFA" }, alwaysRun = true)
    public void tearDown() {
        context.addDataSource(USER_TO_CLEAN_UP, fromCsv(USER_FOR_FORCE_REVOKE_PASSW_CSV));

        final TestScenario scenario = scenario("Delete users").addFlow(gimCleanupFlows.cleanUp(USER)).build();

        start(scenario);
    }

}
