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
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.nms.security.test.usermanagement.scenario.AgatScenarioUtility;
import com.ericsson.oss.testware.enmbase.data.CommonDataSources;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutRestFlows;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.ericsson.oss.testware.security.gim.steps.UserManagementTestSteps;

public class UserManagementChangePasswordHardenedScenario extends TafTestBase {

    private static final String ACCEPTANCE = "Acceptance";
    private static final String SUITE_SETUP = "Suite setup";
    private static final String SUITE_TEARDOWN = "Delete user";
    private static final String CHANGE_PD_FOR_USER_WITH_PASSWORD_RESET_FLAG_FALSE_NEGATIVE = "Change password for a user with passwordResetFlag false - Negative scenario";
    private static final String CHANGE_PD_FOR_USER_WITH_PASSWORD_RESET_FLAG_TRUE_POSITIVE = "Change password for a user with passwordResetFlag false - Positive scenario";

    private final static String USER_MGMT_CSV_PATH = "data/usermanagement_2_0/";
    private final static String ADMIN_USER_CSV = USER_MGMT_CSV_PATH + "AdminUser.csv";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = USER_MGMT_CSV_PATH + "testDataSource.csv";
    private static final String CHANGE_PD_USER_HARDENED_TO_DELETE_OK_CSV = USER_MGMT_CSV_PATH + "ChangePasswordPRFUserToDelete.csv";

    private static final String USERS_TO_CREATE_WITH_FPC_TO_BE_UPDATED_NEGATIVE_CSV = USER_MGMT_CSV_PATH
            + "UsersToCreateForPasswordChangeFCP.csv";
    private static final String USERS_TO_CREATE_WITH_FPC_TO_BE_UPDATED_POSITIVE_CSV = USER_MGMT_CSV_PATH
            + "UsersToCreateForPasswordChangeFCPPositive.csv";
    private static final String USERS_TO_UPDATE_NEGATIVE_CSV = USER_MGMT_CSV_PATH + "ChangePasswordPRFUsersToUpdateNegative.csv";
    private static final String USERS_TO_UPDATE_POSITIVE_CSV = USER_MGMT_CSV_PATH + "ChangePasswordPRFUsersToUpdatePositive.csv";

    private static final Logger logger = LoggerFactory.getLogger(UserManagementChangePasswordHardenedScenario.class);
    @Inject
    private UserManagementTestFlows userManagementTestFlows;
    @Inject
    private GimCleanupFlows gimCleanupFlows;
    @Inject
    private LoginLogoutRestFlows loginLogoutRestFlows;
    @Inject
    private TestContext context;

    @BeforeSuite
    public void suiteSetup() {
        context.addDataSource(CommonDataSources.AVAILABLE_USERS,
                TafDataSources.shared(TafDataSources.fromCsv(ADMIN_USER_CSV)));
        context.addDataSource(TEST_DS, TafDataSources.fromCsv(TEST_DS_CSV));
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP,
                TafDataSources.fromCsv(CHANGE_PD_USER_HARDENED_TO_DELETE_OK_CSV));

        final TestScenario createUserScenario = scenario(SUITE_SETUP).addFlow(gimCleanupFlows.cleanUp(GimCleanupFlows.EnmObjectType.USER))
                .build();
        start(createUserScenario);

        context.removeDataSource(CommonDataSources.USERS_TO_CREATE);
    }


    /**
     * TORF-573672_Funct3 - Test step 2
     *
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 7)
    public void changePasswordForUserWithPasswordResetFlagNegative() {
        logger.info(CHANGE_PD_FOR_USER_WITH_PASSWORD_RESET_FLAG_FALSE_NEGATIVE + " test - START");
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.fromCsv(USERS_TO_CREATE_WITH_FPC_TO_BE_UPDATED_NEGATIVE_CSV));
        context.addDataSource(USERS_TO_UPDATE, TafDataSources.fromCsv(USERS_TO_UPDATE_NEGATIVE_CSV));
        context.addDataSource(AVAILABLE_USERS, TafDataSources.fromCsv(ADMIN_USER_CSV));
        final TestScenario changePasswordForUserWithPasswordResetFlagFalseNegativeScenario = dataDrivenScenario(
                CHANGE_PD_FOR_USER_WITH_PASSWORD_RESET_FLAG_FALSE_NEGATIVE)
                        .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("32")))
                        .addFlow(userManagementTestFlows.createUser())
                        .addFlow(loginLogoutRestFlows.loginDefaultUser())
                        .addFlow(userManagementTestFlows.changePasswordAdmin()
                                .beforeFlow(AgatScenarioUtility.removeDataSources(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS)).afterFlow(
                                        AgatScenarioUtility.hardBindTo(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS,
                                                UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE)).withDataSources(
                                        TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE).bindColumn("originalPassword", "password")
                                                .bindColumn("password_01", "newPassword").bindColumn("expectedResponse_01", "expectedResult")))
                        .build();
        start(changePasswordForUserWithPasswordResetFlagFalseNegativeScenario);
        logger.info(CHANGE_PD_FOR_USER_WITH_PASSWORD_RESET_FLAG_FALSE_NEGATIVE + " test - FINISH");

    }

    /**
     * TORF-573672_Funct3 - Test step 3
     *
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 8)
    public void changePasswordForUserWithPasswordResetFlagPositive() {
        logger.info(CHANGE_PD_FOR_USER_WITH_PASSWORD_RESET_FLAG_TRUE_POSITIVE + " test - START");
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.fromCsv(USERS_TO_CREATE_WITH_FPC_TO_BE_UPDATED_POSITIVE_CSV));
        context.addDataSource(USERS_TO_UPDATE, TafDataSources.fromCsv(USERS_TO_UPDATE_POSITIVE_CSV));
        context.addDataSource(AVAILABLE_USERS, TafDataSources.fromCsv(ADMIN_USER_CSV));
        final TestScenario createAndUpdateUserWithPasswordResetFlagFalsePositiveScenarioIndiaReg = dataDrivenScenario(
                CHANGE_PD_FOR_USER_WITH_PASSWORD_RESET_FLAG_TRUE_POSITIVE)
                        .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("33")))
                        .addFlow(userManagementTestFlows.createUser()).addFlow(loginLogoutRestFlows.loginDefaultUser())
                        .addFlow(userManagementTestFlows.changePasswordAdmin()
                                .beforeFlow(AgatScenarioUtility.removeDataSources(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS))
                                .afterFlow(AgatScenarioUtility.hardBindTo(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS,
                                        UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE))
                                .withDataSources(
                                        TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE).bindColumn("originalPassword", "password")
                                                .bindColumn("password_01", "newPassword").bindColumn("expectedResponse_01", "expectedResult")))
                        .build();
        start(createAndUpdateUserWithPasswordResetFlagFalsePositiveScenarioIndiaReg);
        logger.info(CHANGE_PD_FOR_USER_WITH_PASSWORD_RESET_FLAG_TRUE_POSITIVE + " test - FINISH");

    }

    @AfterSuite(alwaysRun = true)
    void suiteTearDown() {
        context.addDataSource(CommonDataSources.AVAILABLE_USERS, TafDataSources.fromCsv(ADMIN_USER_CSV));
        context.removeDataSource(CommonDataSources.AVAILABLE_USERS);
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP, TafDataSources.fromCsv(CHANGE_PD_USER_HARDENED_TO_DELETE_OK_CSV));
        final TestScenario deleteUserScenario = scenario(SUITE_TEARDOWN).addFlow(gimCleanupFlows.cleanUp(GimCleanupFlows.EnmObjectType.USER)).build();
        start(deleteUserScenario);
        context.removeDataSource(CommonDataSources.USER_TO_CLEAN_UP);
    }
}
