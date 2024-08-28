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

public class UserManagementHardenedScenario extends TafTestBase {

    private static final String ACCEPTANCE = "Acceptance";
    private static final String CREATE_USER = "Create user";
    private static final String DELETE_USER = "Delete user";
    private static final String CREATE_USER_WITH_PASSWORD_RESET_NEGATIVE = "Create user disabling password reset flag";
    private static final String CREATE_USER_WITH_PASSWORD_RESET_FLAG_UNDEFINED_POSITIVE = "Create user without password reset flag";
    private static final String UPDATE_USER_WITH_PASSWORD_RESET_FLAG_UNDEFINED_POSITIVE = "Update user without password reset flag";
    private static final String UPDATE_USER_WITH_PASSWORD_RESET_FLAG_TRUE_POSITIVE = "Update user with password reset flag true";
    private static final String UPDATE_USER_WITH_PASSWORD_RESET_FLAG_FALSE_FROM_TRUE_NEGATIVE = "Update user with password reset flag to false from true";
    private static final String CREATE_AND_UPDATE_USER_WITH_PASSWORD_RESET_FLAG_FALSE_POSITIVE = "Create and update a user with passwordResetFlag false after password change";
    private final static String USER_MGMT_CSV_PATH = "data/usermanagement_2_0/";
    private final static String ADMIN_USER_CSV = USER_MGMT_CSV_PATH + "AdminUser.csv";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = USER_MGMT_CSV_PATH + "testDataSource.csv";
    private static final String UM_USER_HARDENED_TO_CREATE_OK_CSV = USER_MGMT_CSV_PATH + "PRFCreateUser.csv";
    private static final String UM_USER_HARDENED_TO_DELETE_OK_CSV = USER_MGMT_CSV_PATH + "PRFUserToDelete.csv";
    private static final String UM_USER_HARDENED_NEGATIVE_TO_CREATE_CSV = USER_MGMT_CSV_PATH + "PRFCreateUserNegative.csv";
    private static final String UM_USER_HARDENED_POSITIVE_TO_CREATE_CSV = USER_MGMT_CSV_PATH + "PRFCreateUserPositive.csv";
    private static final String UM_USER_HARDENED_POSITIVE_TO_UPDATE_CSV = USER_MGMT_CSV_PATH + "PRFUpdateUserPositive.csv";
    private static final String UM_USER_HARDENED_POSITIVE_TO_UPDATE_TRUE_CSV = USER_MGMT_CSV_PATH + "PRFUpdateUserPositiveTrue.csv";
    private static final String UM_USER_HARDENED_POSITIVE_TO_UPDATE_FROM_TRUE_TO_FALSE_CSV = USER_MGMT_CSV_PATH
            + "PRFUpdateUserNegativeFromTrueToFalse.csv";
    private static final String USERS_TO_CREATE_WITH_FPC_TO_BE_UPDATED_CSV = USER_MGMT_CSV_PATH + "PRFCreateUserWithPasswordChangePositive.csv";
    private static final String USERS_TO_UPDATE_CSV = USER_MGMT_CSV_PATH + "PRFUsersToUpdatePositive.csv";

    private static final Logger logger = LoggerFactory.getLogger(UserManagementHardenedScenario.class);
    @Inject
    private UserManagementTestFlows userManagementTestFlows;
    @Inject
    private GimCleanupFlows gimCleanupFlows;
    @Inject
    private LoginLogoutRestFlows loginLogoutRestFlows;
    @Inject
    private TestContext context;

    /**
     * The suite setup covers TORF-573672_Funct1 - Test step 2
     */
    @BeforeSuite
    public void suiteSetup() {
        context.addDataSource(CommonDataSources.AVAILABLE_USERS, TafDataSources.shared(TafDataSources.fromCsv(ADMIN_USER_CSV)));
        context.addDataSource(TEST_DS, TafDataSources.fromCsv(TEST_DS_CSV));
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.fromCsv(UM_USER_HARDENED_TO_CREATE_OK_CSV));
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP, TafDataSources.fromCsv(UM_USER_HARDENED_TO_DELETE_OK_CSV));

        final TestScenario createUserScenario = scenario(CREATE_USER).addFlow(gimCleanupFlows.cleanUp(GimCleanupFlows.EnmObjectType.USER))
                .addFlow(userManagementTestFlows.createUserWithoutCreationCheck()).build();
        start(createUserScenario);

        context.removeDataSource(CommonDataSources.USERS_TO_CREATE);
    }

    /**
     * TORF-573672_Funct1 - Test step 1
     *
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 1)
    public void createUserWithResetPasswordHardenedNegative() {
        logger.info(CREATE_USER_WITH_PASSWORD_RESET_NEGATIVE + " test - START");
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.fromCsv(UM_USER_HARDENED_NEGATIVE_TO_CREATE_CSV));
        final TestScenario createUserWithPasswordResetFlagNegativeScenarioIndiaReg = dataDrivenScenario(CREATE_USER_WITH_PASSWORD_RESET_NEGATIVE)
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("24")))
                .addFlow(userManagementTestFlows.createUserWithoutVerificationNegativeIndiaReg()).build();
        start(createUserWithPasswordResetFlagNegativeScenarioIndiaReg);
        context.removeDataSource(CommonDataSources.USERS_TO_CREATE);
        logger.info(CREATE_USER_WITH_PASSWORD_RESET_NEGATIVE + " test - FINISH");
    }

    /**
     * TORF-573672_Funct1 - Test step 3
     *
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 2)
    public void createUserWithoutResetPasswordHardenedPositive() {
        logger.info(CREATE_USER_WITH_PASSWORD_RESET_FLAG_UNDEFINED_POSITIVE + " test - START");
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.fromCsv(UM_USER_HARDENED_POSITIVE_TO_CREATE_CSV));
        final TestScenario createUserWithoutPasswordResetFlagPositiveScenarioIndiaReg = dataDrivenScenario(
                CREATE_USER_WITH_PASSWORD_RESET_FLAG_UNDEFINED_POSITIVE)
                        .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("25")))
                        .addFlow(userManagementTestFlows.createUserWithoutCreationCheck()).build();
        start(createUserWithoutPasswordResetFlagPositiveScenarioIndiaReg);
        context.removeDataSource(CommonDataSources.USERS_TO_CREATE);
        logger.info(CREATE_USER_WITH_PASSWORD_RESET_FLAG_UNDEFINED_POSITIVE + " test - FINISH");
    }

    /**
     * TORF-573672_Funct1 - Test step 4
     *
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 3)
    public void updateUserWithoutResetPasswordHardenedPositive() {
        logger.info(UPDATE_USER_WITH_PASSWORD_RESET_FLAG_UNDEFINED_POSITIVE + " test - START");
        context.addDataSource(USERS_TO_UPDATE, TafDataSources.fromCsv(UM_USER_HARDENED_POSITIVE_TO_UPDATE_CSV));
        final TestScenario updateUserWithoutPasswordResetFlagPostiveScenarioIndiaReg = dataDrivenScenario(
                UPDATE_USER_WITH_PASSWORD_RESET_FLAG_UNDEFINED_POSITIVE)
                        .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("26")))
                        .addFlow(userManagementTestFlows.updateUser()).build();
        start(updateUserWithoutPasswordResetFlagPostiveScenarioIndiaReg);
        logger.info(UPDATE_USER_WITH_PASSWORD_RESET_FLAG_UNDEFINED_POSITIVE + " test - FINISH");
    }

    /**
     * TORF-573672_Funct1 - Test step 5
     * 
    */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 4)
    public void updateUserWithResetPasswordHardenedPositive() {
        logger.info(UPDATE_USER_WITH_PASSWORD_RESET_FLAG_TRUE_POSITIVE + " test - START");
        context.addDataSource(USERS_TO_UPDATE, TafDataSources.fromCsv(UM_USER_HARDENED_POSITIVE_TO_UPDATE_TRUE_CSV));
        final TestScenario updateUserWithPasswordResetFlagTruePostiveScenarioIndiaReg = dataDrivenScenario(
                UPDATE_USER_WITH_PASSWORD_RESET_FLAG_TRUE_POSITIVE)
                        .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("27")))
                        .addFlow(userManagementTestFlows.updateUser()).build();
        start(updateUserWithPasswordResetFlagTruePostiveScenarioIndiaReg);
        logger.info(UPDATE_USER_WITH_PASSWORD_RESET_FLAG_TRUE_POSITIVE + " test - FINISH");
    }

    /**
     * TORF-573672_Funct1 - Test step 6
     *
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 5)
    public void updateUserWithResetPasswordHardenedSettingFalseNegative() {
        logger.info(UPDATE_USER_WITH_PASSWORD_RESET_FLAG_FALSE_FROM_TRUE_NEGATIVE + " test - START");
        context.addDataSource(USERS_TO_UPDATE, TafDataSources.fromCsv(UM_USER_HARDENED_POSITIVE_TO_UPDATE_FROM_TRUE_TO_FALSE_CSV));
        final TestScenario updateUserWithPasswordResetFlagToFalseNegativeScenarioIndiaReg = dataDrivenScenario(
                UPDATE_USER_WITH_PASSWORD_RESET_FLAG_FALSE_FROM_TRUE_NEGATIVE)
                        .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("28")))
                        .addFlow(userManagementTestFlows.updateUserNegativeIndiaReg()).build();
        start(updateUserWithPasswordResetFlagToFalseNegativeScenarioIndiaReg);
        logger.info(UPDATE_USER_WITH_PASSWORD_RESET_FLAG_FALSE_FROM_TRUE_NEGATIVE + " test - FINISH");
    }

    /**
     * TORF-573672_Funct2
     * Create a user with passwordResetFlag to false (in India reg this is created with true),
     * login as default user and change the password
     * update successfully the user with passwordResetFlag to false
     *
     */
    @TestSuite
    @Test(groups = { ACCEPTANCE }, priority = 6)
    public void createAndupdateUseAftePasswordChangerWithPasswordResetFlagFalsePositive() {
        logger.info(CREATE_AND_UPDATE_USER_WITH_PASSWORD_RESET_FLAG_FALSE_POSITIVE + " test - START");
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.fromCsv(USERS_TO_CREATE_WITH_FPC_TO_BE_UPDATED_CSV));
        context.addDataSource(USERS_TO_UPDATE, TafDataSources.fromCsv(USERS_TO_UPDATE_CSV));
        context.addDataSource(AVAILABLE_USERS, TafDataSources.fromCsv(ADMIN_USER_CSV));
        final TestScenario createAndUpdateUserWithPasswordResetFlagFalsePositiveScenarioIndiaReg = dataDrivenScenario(
                CREATE_AND_UPDATE_USER_WITH_PASSWORD_RESET_FLAG_FALSE_POSITIVE)
                        .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("29")))
                        .addFlow(userManagementTestFlows.createUser()).addFlow(loginLogoutRestFlows.login())
                        .addFlow(userManagementTestFlows.changePassword()
                                .beforeFlow(AgatScenarioUtility.removeDataSources(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS))
                                .afterFlow(AgatScenarioUtility.hardBindTo(
                                        UserManagementTestSteps.MODIFIED_AVAILABLE_USERS, UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE))
                                .withDataSources(
                                        TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE).bindColumn("originalPassword", "password")
                                                .bindColumn("password_01", "newPassword").bindColumn("expectedResponse_01", "expectedResult")))
                        .addFlow(userManagementTestFlows.updateUser()
                                .beforeFlow(AgatScenarioUtility.removeDataSources(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS))
                                .afterFlow(AgatScenarioUtility.hardBindTo(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS,
                                        UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE))
                                .withDataSources(TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE)
                                        .bindColumn("passwordResetFlag_01", "passwordResetFlag").bindColumn("expectedResponse_02", "expectedResult")))
                        .build();
        start(createAndUpdateUserWithPasswordResetFlagFalsePositiveScenarioIndiaReg);
        logger.info(CREATE_AND_UPDATE_USER_WITH_PASSWORD_RESET_FLAG_FALSE_POSITIVE + " test - FINISH");

    }

    @AfterSuite(alwaysRun = true)
    void suiteTearDown() {
        context.addDataSource(CommonDataSources.AVAILABLE_USERS, TafDataSources.fromCsv(ADMIN_USER_CSV));
        context.removeDataSource(CommonDataSources.AVAILABLE_USERS);
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP, TafDataSources.fromCsv(UM_USER_HARDENED_TO_DELETE_OK_CSV));
        final TestScenario deleteUserScenario = scenario(DELETE_USER).addFlow(gimCleanupFlows.cleanUp(GimCleanupFlows.EnmObjectType.USER)).build();
        start(deleteUserScenario);
        context.removeDataSource(CommonDataSources.USER_TO_CLEAN_UP);
    }
}
