/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
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
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.FORCE_PASSWORD_CHANGE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_CREATE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USER_TO_CLEAN_UP;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.USER;
import static com.ericsson.oss.testware.security.usermanagement.flows.ChangePasswordFlow.DATA_SOURCE_CHANGE_PASSWORD;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.nms.security.test.usermanagement.flows.LoginDelayFlow;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.ericsson.oss.testware.security.gim.steps.UserManagementTestSteps;
import com.ericsson.oss.testware.security.usermanagement.flows.ChangePasswordFlow;

public class ChangePasswordScenario extends TafTestBase {
    final static String CSV_CREATE_USER = "data/usermanagement_2_0/ChangePasswordCreateUser.csv";

    private final static String CSV_CHANGE_USER_PASSWORD = "data/usermanagement_2_0/ChangeUserPassword.csv";
    private final static String CSV_CHANGE_USER_PASSWORD_FORCE_DATA = "data/usermanagement_2_0/ChangeUserPasswordForceData.csv";
    private final static String CSV_CHANGE_USER_PASSWORD_NO_FORCE = "data/usermanagement_2_0/ChangeUserPasswordNoForce.csv";
    private final static String CSV_CHANGE_YOUR_PASSWORD = "data/usermanagement_2_0/ChangeYourPassword.csv";
    private final static String CSV_CHANGE_YOUR_PASSWORD_FORCE_DATA = "data/usermanagement_2_0/ChangeYourPasswordData.csv";

    private final static String USER_MGMT_APP_NAME_PAGIN_500 = "/#usermanagement/?pagenumber=1&pagesize=500";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @Inject
    private TestContext context;

    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;

    @Inject
    private ChangePasswordFlow changePasswordFlow;

    @Inject
    private UserManagementTestFlows userManagementTestFlows;

    @Inject
    private GimCleanupFlows gimCleanupFlows;

    @Inject
    private LoginDelayFlow loginDelayFlow;

    @BeforeClass(groups = {"RFA", "RFA250"})
    public void setUp() throws TimeoutException, InterruptedException {
        context.addDataSource(USERS_TO_CREATE, TafDataSources.shared(TafDataSources.fromCsv(CSV_CREATE_USER)));

        final TestScenario scenario = scenario("Create user for Change Password Scenario")
                .addFlow(userManagementTestFlows.createUser())
                .build();

        start(scenario);
        context.removeDataSource(AVAILABLE_USERS);
        context.removeDataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE);
    }

    @Test(groups = {"Acceptance", "RFA", "RFA250", "Change Password"})
    @TestSuite
    public void testChangePassword() throws Exception {
        context.addDataSource(DATA_SOURCE_CHANGE_PASSWORD, TafDataSources.shared(TafDataSources.fromCsv(CSV_CHANGE_USER_PASSWORD)));
        context.addDataSource(FORCE_PASSWORD_CHANGE, TafDataSources.shared(TafDataSources.fromCsv(CSV_CHANGE_USER_PASSWORD_FORCE_DATA)));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Change Password Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("5")))
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME_PAGIN_500))
                .addFlow(changePasswordFlow.changePassword())
                .addFlow(loginLogoutFlow.logout())
                .addFlow(loginDelayFlow.delayLogin())
                .addFlow(loginLogoutFlow.loginForceChangePassword())
                .addFlow(loginLogoutFlow.isUserLoggedIn())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

    @Test(groups = {"Acceptance", "RFA", "Change Password"})
    @TestSuite
    public void testChangePasswordNoForceChangePassword() throws Exception {
        context.addDataSource(DATA_SOURCE_CHANGE_PASSWORD, TafDataSources.shared(TafDataSources.fromCsv(CSV_CHANGE_USER_PASSWORD_NO_FORCE)));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Change Password Scenario with no force change password")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("6")))
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME_PAGIN_500))
                .addFlow(changePasswordFlow.changePassword())
                .addFlow(loginLogoutFlow.logout())
                .addFlow(loginDelayFlow.delayLogin())
                .addFlow(loginLogoutFlow.login().bindDataSource(AVAILABLE_USERS, dataSource(DATA_SOURCE_CHANGE_PASSWORD)))
                .addFlow(loginLogoutFlow.isUserLoggedIn())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

    @Test(groups = {"Acceptance", "RFA", "RFA250", "Change Your Password"})
    @TestSuite
    public void testChangeYourPassword() throws Exception {
        context.addDataSource(DATA_SOURCE_CHANGE_PASSWORD, TafDataSources.shared(TafDataSources.fromCsv(CSV_CHANGE_YOUR_PASSWORD)));
        context.addDataSource(AVAILABLE_USERS, TafDataSources.shared(TafDataSources.fromCsv(CSV_CHANGE_YOUR_PASSWORD_FORCE_DATA)));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Change Your Password Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("7")))
                .addFlow(changePasswordFlow.changeYourPassword())
                .addFlow(loginDelayFlow.delayLogin())
                .addFlow(loginLogoutFlow.login())
                .addFlow(loginLogoutFlow.isUserLoggedIn())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

    @AfterClass(groups = {"RFA", "RFA250"}, alwaysRun = true)
    public void tearDown() {
        context.addDataSource(USER_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(CSV_CREATE_USER)));

        final TestScenario scenario = scenario("Clean up users check profile")
                .addFlow(gimCleanupFlows.cleanUp(USER))
                .build();

        start(scenario);

        context.removeDataSource(AVAILABLE_USERS);
    }
}

