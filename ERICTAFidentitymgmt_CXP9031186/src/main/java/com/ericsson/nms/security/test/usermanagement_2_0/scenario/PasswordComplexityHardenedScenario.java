/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
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
import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_UPDATE;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.nms.security.test.usermanagement.scenario.AgatScenarioUtility;
import com.ericsson.oss.testware.enmbase.data.CommonDataSources;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutRestFlows;
import com.ericsson.oss.testware.security.authentication.flows.UtilityFlows;
import com.ericsson.oss.testware.security.gic.flows.PasswordSettingsFlows;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.ericsson.oss.testware.security.gim.steps.UserManagementTestSteps;

public class PasswordComplexityHardenedScenario extends TafTestBase {
    private static final String ACCEPTANCE = "Acceptance";
    private static final String SUITE_SETUP = "Suite setup";
    private static final String SUITE_TEARDOWN = "Delete user";
    private static final String PASSWORD_COMPLEXITY_GET_SCENARIO_INDIA_REG = "Get password complexity in India regulator";
    private static final String CREATE_USER_WITH_WEAK_PASSWORD_NEGATIVE = "Create user with a weak password - India regulator";
    private static final String CREATE_USER_WITH_STRONG_PD_POSITIVE = "Create user with a strong password - India regulator";
    private static final String UPDATE_USER_WITH_WEAK_PD_NEGATIVE = "Update user password with a weak password - India regulator";
    private static final String CHANGE_PD_POSITIVE = "Change Password - India regulator";
    private static final String LOGIN_PD_POSITIVE = "Login with Password - India regulator";
    private static final String PASSWORD_SETTINGS = "passwordSettings";
    private static final String USER_MGMT_CSV_PATH = "data/usermanagement_2_0/";
    private static final String ADMIN_USER_CSV = USER_MGMT_CSV_PATH + "AdminUser.csv";
    private static final String LOGIN_POSITIVE_CSV = USER_MGMT_CSV_PATH + "LoginUser.csv";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = USER_MGMT_CSV_PATH + "testDataSource.csv";
    private static final String PD_COMPLEXITY_TO_DELETE_OK_CSV = USER_MGMT_CSV_PATH + "PasswordComplexityUserToDelete.csv";
    private static final String USERS_TO_CREATE_WITH_WEAK_PD_NEGATIVE_CSV = USER_MGMT_CSV_PATH + "UsersToCreatePasswordComplexNegative.csv";
    private static final String USERS_TO_CREATE_WITH_STRONG_PD_POSITIVE_CSV = USER_MGMT_CSV_PATH + "UsersToCreatePasswordComplexPositive.csv";
    private static final String USERS_TO_CREATE_WEAK_PD_CSV = USER_MGMT_CSV_PATH + "UserToCreateWeakPassword.csv";
    private static final String USERS_TO_UPDATE_WEAK_PD_NEGATIVE_CSV = USER_MGMT_CSV_PATH + "UserToUpdateWeakPassword.csv";
    private static final String CHANGE_PD_POSITIVE_CSV = USER_MGMT_CSV_PATH + "UserToUpdatePassword.csv";
    private static final String WAIT_CSV = USER_MGMT_CSV_PATH + "validWaitFiles.csv";
    private static final String VALID_WAIT_FILES = "validWaitFiles";
    public static final String DATASOURCE_ERROR_USERS = "errorUsers";
    // THIS IS STILL NEEDED, but actually the expectedJson column is not used
    public static final String PD_SETTINGS_USER_HARDENED_GET_CSV = USER_MGMT_CSV_PATH + "GetPasswordSettingsHarden.csv";

    private static final Logger logger = LoggerFactory.getLogger(PasswordComplexityHardenedScenario.class);
    @Inject
    private UserManagementTestFlows userManagementTestFlows;

    @Inject
    private GimCleanupFlows gimCleanupFlows;

    @Inject
    private LoginLogoutRestFlows loginLogoutRestFlows;

    @Inject
    private PasswordSettingsFlows passwordSettingsFlows;

    @Inject
    private UtilityFlows utilityFlows;

    @Inject
    private TestContext context;

    @BeforeSuite
    public void suiteSetup() {
        context.addDataSource(CommonDataSources.AVAILABLE_USERS,
                TafDataSources.shared(TafDataSources.fromCsv(ADMIN_USER_CSV)));
        context.addDataSource(TEST_DS, TafDataSources.fromCsv(TEST_DS_CSV));
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP,
                TafDataSources.fromCsv(PD_COMPLEXITY_TO_DELETE_OK_CSV));

        final TestScenario createUserScenario = scenario(SUITE_SETUP).addFlow(gimCleanupFlows.cleanUp(GimCleanupFlows.EnmObjectType.USER))
                .build();
        start(createUserScenario);

        context.removeDataSource(CommonDataSources.USERS_TO_CREATE);
    }

    /**
     * TORF-558222_IndiaRegulation_Test_create_update_user_via_NBI Test steps 1 and 2
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 10)
    public void getPasswordSettingsHardenScenario() {
        logger.info(PASSWORD_COMPLEXITY_GET_SCENARIO_INDIA_REG + " test - START");
        final TestDataSource<DataRecord> getPasswordSettings = shared(fromCsv(PD_SETTINGS_USER_HARDENED_GET_CSV));
        context.addDataSource(PASSWORD_SETTINGS, getPasswordSettings);
        final TestScenario passwordSettingsGetScenario = dataDrivenScenario("Get Password Settings in India Regulator Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("34")))
                .addFlow(loginLogoutRestFlows.loginDefaultUser()).addFlow(passwordSettingsFlows.getPasswordSettingsHardened()).build();
        start(passwordSettingsGetScenario);

        logger.info(PASSWORD_COMPLEXITY_GET_SCENARIO_INDIA_REG + " test - FINISH");
    }

    /**
     * TORF-558222_IndiaRegulation_Test_create_update_user_via_NBI Test step 3
     *
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 11)
    public void createUserWithWeakPasswordNegative() {
        logger.info(CREATE_USER_WITH_WEAK_PASSWORD_NEGATIVE + " test - START");
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.fromCsv(USERS_TO_CREATE_WITH_WEAK_PD_NEGATIVE_CSV));
        context.addDataSource(CommonDataSources.AVAILABLE_USERS, TafDataSources.fromCsv(ADMIN_USER_CSV));
        final TestScenario createUserWithWeakPasswordNegativeScenario = dataDrivenScenario(CREATE_USER_WITH_WEAK_PASSWORD_NEGATIVE)
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("35")))
                .addFlow(userManagementTestFlows.createUserWithoutVerificationNegativeIndiaReg())
                .build();
        start(createUserWithWeakPasswordNegativeScenario);
        logger.info(CREATE_USER_WITH_WEAK_PASSWORD_NEGATIVE + " test - FINISH");

    }

    /**
     * TORF-558222_IndiaRegulation_Test_create_update_user_via_NBI Test step 4
     *
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 12)
    public void createUserWithNoWeakPasswordPositive() {
        logger.info(CREATE_USER_WITH_STRONG_PD_POSITIVE + " test - START");
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.fromCsv(USERS_TO_CREATE_WITH_STRONG_PD_POSITIVE_CSV));
        context.addDataSource(CommonDataSources.AVAILABLE_USERS, TafDataSources.fromCsv(ADMIN_USER_CSV));
        final TestScenario createUserWithNoWeakPasswordPositiveScenario = dataDrivenScenario(CREATE_USER_WITH_STRONG_PD_POSITIVE)
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("36")))
                .addFlow(userManagementTestFlows.createUser())
                .build();
        start(createUserWithNoWeakPasswordPositiveScenario);
        logger.info(CREATE_USER_WITH_STRONG_PD_POSITIVE + " test - FINISH");

    }

    /**
     * TORF-558222_IndiaRegulation_Test_create_update_user_via_NBI Test step 5
     *
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 13)
    public void updateUserPasswordNegative() {
        logger.info(UPDATE_USER_WITH_WEAK_PD_NEGATIVE + " test - START");
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.fromCsv(USERS_TO_CREATE_WEAK_PD_CSV));
        context.addDataSource(USERS_TO_UPDATE, TafDataSources.fromCsv(USERS_TO_UPDATE_WEAK_PD_NEGATIVE_CSV));
        context.addDataSource(AVAILABLE_USERS, TafDataSources.fromCsv(ADMIN_USER_CSV));
        final TestScenario updateUserPasswordNegativeScenario = dataDrivenScenario(UPDATE_USER_WITH_WEAK_PD_NEGATIVE)
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("37")))
                .addFlow(userManagementTestFlows.createUser()).addFlow(loginLogoutRestFlows.loginDefaultUser())
                .addFlow(userManagementTestFlows.changePasswordAdmin()
                        .beforeFlow(AgatScenarioUtility.removeDataSources(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS))
                        .afterFlow(AgatScenarioUtility.hardBindTo(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS,
                                UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE))
                        .withDataSources(TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE).bindColumn("originalPassword", "password")
                                .bindColumn("password_01", "newPassword").bindColumn("expectedResponse_01", "expectedResult")))
                .build();
        start(updateUserPasswordNegativeScenario);
        logger.info(UPDATE_USER_WITH_WEAK_PD_NEGATIVE + " test - FINISH");

    }

    /**
     * TORF-558222_IndiaRegulation_Test_create_update_user_via_NBI Test steps 6, 7, 8 and 9 - Note: this test is depending from the previous ones
     *
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 14)
    public void updateUserPasswordPositive() {
        logger.info(CHANGE_PD_POSITIVE + " test - START");
        context.addDataSource(USERS_TO_UPDATE, TafDataSources.fromCsv(CHANGE_PD_POSITIVE_CSV));
        context.addDataSource(CommonDataSources.AVAILABLE_USERS,
                TafDataSources.shared(TafDataSources.fromTafDataProvider(CommonDataSources.USERS_TO_UPDATE)));
        context.addDataSource(DATASOURCE_ERROR_USERS, TafDataSources.fromCsv(CHANGE_PD_POSITIVE_CSV));
        context.addDataSource(VALID_WAIT_FILES, TafDataSources.fromCsv(WAIT_CSV));
        final TestScenario updateUserPasswordPositiveScenario = dataDrivenScenario(CHANGE_PD_POSITIVE)
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("38")))
                .addFlow(loginLogoutRestFlows.loginDefaultUser())
                .addFlow(userManagementTestFlows.changePasswordAdmin()
                        .beforeFlow(AgatScenarioUtility.removeDataSources(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS))
                        .afterFlow(AgatScenarioUtility.hardBindTo(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS,
                                UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE))
                        .withDataSources(TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE).bindColumn("originalPassword", "password")
                                .bindColumn("password_01", "newPassword").bindColumn("expectedResponse_01", "expectedResult")))
                // password_01 - TestPassw1rd$
                // expectedResponse_01 - NO CONTENT
                .addFlow(loginLogoutRestFlows.logout())
                .addFlow(utilityFlows.sleep())
                .addFlow(loginLogoutRestFlows.loginWithRedirectToChangePassword())
                .addFlow(userManagementTestFlows.changePassword()
                        .beforeFlow(AgatScenarioUtility.removeDataSources(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS))
                        .afterFlow(AgatScenarioUtility.hardBindTo(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS,
                                UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE))
                        .withDataSources(TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE).bindColumn("originalPassword", "password")
                                .bindColumn("password_02", "newPassword").bindColumn("expectedResponse_02", "expectedResult")))
                // password_02 - NewPassw0rd
                // expectedResponse_02 - UNPROCESSABLE_ENTITY
                .addFlow(userManagementTestFlows.changePassword()
                        .beforeFlow(AgatScenarioUtility.removeDataSources(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS))
                        .afterFlow(AgatScenarioUtility.hardBindTo(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS,
                                UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE))
                        .withDataSources(TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE).bindColumn("originalPassword", "password")
                                .bindColumn("password_03", "newPassword").bindColumn("expectedResponse_03", "expectedResult")))
                // password_03 NewPassw0rd$
                // expectedResponse_03 - NO_CONTENT
                .addFlow(utilityFlows.sleep())
                .build();
        start(updateUserPasswordPositiveScenario);
        logger.info(CHANGE_PD_POSITIVE + " test - FINISH");

    }

    /**
     * TORF-558222_IndiaRegulation_Test_create_update_user_via_NBI Test step 10 - Note: this test is depending from the previous ones
     *
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 15)
    public void loginWithUpdatedUserPasswordPositive() {
        logger.info(LOGIN_PD_POSITIVE + " test - START");
        context.removeDataSource(CommonDataSources.AVAILABLE_USERS);
        context.addDataSource(CommonDataSources.AVAILABLE_USERS, TafDataSources.fromCsv(LOGIN_POSITIVE_CSV));
        final TestScenario updateUserPasswordPositiveScenario = dataDrivenScenario(LOGIN_PD_POSITIVE)
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("39")))
                .addFlow(loginLogoutRestFlows.login())
                .build();
        start(updateUserPasswordPositiveScenario);
        logger.info(LOGIN_PD_POSITIVE + " test - FINISH");

    }
    @AfterSuite(alwaysRun = true)
    void suiteTearDown() {
        context.addDataSource(CommonDataSources.AVAILABLE_USERS, TafDataSources.fromCsv(ADMIN_USER_CSV));
        context.removeDataSource(CommonDataSources.AVAILABLE_USERS);
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP, TafDataSources.fromCsv(PD_COMPLEXITY_TO_DELETE_OK_CSV));
        final TestScenario deleteUserScenario = scenario(SUITE_TEARDOWN).addFlow(gimCleanupFlows.cleanUp(GimCleanupFlows.EnmObjectType.USER)).build();
        start(deleteUserScenario);
        context.removeDataSource(CommonDataSources.USER_TO_CLEAN_UP);
    }
}
