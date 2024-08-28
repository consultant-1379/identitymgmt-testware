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
 *----------------------------------------------------------------------------*/

package com.ericsson.nms.security.test.usermanagement_2_0.scenario;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.CHANGE_PASSWORD_BY_USER;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USER_TO_CLEAN_UP;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USER_TO_CREATE;
import static com.ericsson.oss.testware.security.gic.data.ValidationRuleDataSources.AVAILABLE_VALIDATION_RULES;
import static com.ericsson.oss.testware.security.gic.data.ValidationRuleDataSources.VALIDATION_RULES;
import static com.ericsson.oss.testware.security.gic.data.ValidationRuleDataSources.VALIDATION_RULES_TO_UPDATE;
import static com.ericsson.oss.testware.security.gic.data.provider.AvailableValidationRuleDataProvider.prepareRulesForHttpPut;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.USER;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.gic.data.ValidationRuleDataRecord;
import com.ericsson.oss.testware.security.gic.data.provider.AvailableValidationRuleDataProvider;
import com.ericsson.oss.testware.security.gic.flows.SystemSecurityConfigurationFlow;
import com.ericsson.oss.testware.security.gic.flows.ValidationRuleFlows;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.usermanagement.flows.ChangePasswordFlow;
import com.ericsson.oss.testware.security.usermanagement.flows.PasswordComplexityFlow;

public class PasswordComplexityScenario extends TafTestBase {
    @Inject
    private TestContext context;

    @Inject
    private LoginLogoutUiFlows loginLogoutUiFlows;

    @Inject
    private PasswordComplexityFlow passwordComplexityFlows;

    @Inject
    private SystemSecurityConfigurationFlow sysSecConfFlows;

    @Inject
    private ValidationRuleFlows validationRuleFlows;

    @Inject
    private GimCleanupFlows gimCleanupFlows;

    @Inject
    private ChangePasswordFlow changePasswordFlow;

    private static final String CHANGE_PASSWORD_BY_USER_CSV = "data/usermanagement_2_0/PasswordComplexityChangePasswordByUser.csv";
    private static final String CHANGE_PASSWORD_BY_ADMIN_CSV = "data/usermanagement_2_0/PasswordComplexityChangePasswordByAdmin.csv";
    private static final String CHANGE_PASSWORD_UNCORRECT_CSV = "data/usermanagement_2_0/PasswordComplexityUncorrect.csv";
    private static final String CREATE_USERS_CSV = "data/usermanagement_2_0/PasswordComplexityCreateUser.csv";
    private static final String DEFAULT_VALIDATION_RULES_CSV = "data/defaultValidationRules.csv";
    private static final String LAUNCHER_URL = "/#launcher/groups";
    private static final String NUMBER_OF_DIGITS_CSV = "data/usermanagement_2_0/GATscenarioData.csv";
    private static final String SELF_CHANGE_PASSWORD_URL = "/#userprofilechangepass";
    private static final String SINGLE_USER = "data/usermanagement_2_0/PasswordComplexityLoginSingleUser.csv";
    private static final String SPECIAL_CHARACTERS_CHANGE_PASSWORD_BY_USER_CSV = "data/usermanagement_2_0/PasswordComplexitySpecialCharactersChangeByUser.csv";
    private static final String SSC_URL = "/#syssecconfig";
    private static final String TEMPLATE = "[{\"name\":\"minimumLength\",\"value\":8},{\"name\":\"minimumLowerCase\",\"value\":1,\"enabled\":true},{\"name\":\"minimumUpperCase\",\"value\":1,\"enabled\":true},{\"name\":\"minimumDigits\",\"value\":1,\"enabled\":true},{\"name\":\"minimumSpecialChars\",\"value\":1,\"enabled\":false},{\"name\":\"maximumRepeatingChars\",\"value\":4,\"enabled\":false},{\"name\":\"maximumConsecutiveChars\",\"value\":4,\"enabled\":false},{\"name\":\"mustNotContainUserId\",\"enabled\":false},{\"name\":\"mustNotContainDictionaryWords\",\"enabled\":false},{\"name\":\"mustNotBeOldPassword\",\"value\":1,\"enabled\":false}]";
    private static final String USERS_FOR_SPECIAL_CHARACTERS = "data/usermanagement_2_0/PasswordComplexitySpecialCharacters.csv";
    private static final String USER_MGMT_APP_NAME_PAGIN_500 = "/#usermanagement/?pagenumber=1&pagesize=500";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @BeforeClass
    public void setUp() {
        loadInitialValidationRules();

        context.addDataSource(USER_TO_CREATE, TafDataSources.shared(TafDataSources.fromCsv(CREATE_USERS_CSV)));
        context.addDataSource(SystemSecurityConfigurationFlow.NUMBER_OF_DIGITS, TafDataSources.fromCsv(NUMBER_OF_DIGITS_CSV));

        final TestScenario scenario = scenario("Set new password policies and create users").addFlow(loginLogoutUiFlows.loginWithRedirect(SSC_URL))
                .addFlow(sysSecConfFlows.setNewPasswordPoliciesFlow()).addFlow(loginLogoutUiFlows.logout())
                .addFlow(loginLogoutUiFlows.loginWithRedirect(USER_MGMT_APP_NAME_PAGIN_500))
                .addFlow(passwordComplexityFlows.createUserAndCheckPasswordPolicies()).addFlow(loginLogoutUiFlows.logout()).build();

        start(scenario);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        restoreInitialValidationRules();

        context.addDataSource(USER_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(CREATE_USERS_CSV)));

        final TestScenario scenario = scenario("Delete users").addFlow(gimCleanupFlows.cleanUp(USER))
                .withExceptionHandler(ScenarioExceptionHandler.LOGONLY).build();

        start(scenario);
    }

    @TestSuite
    @Test(groups = { "Functional" }, priority = 0)
    public void passwordComplexityGATTest() throws Exception {
        context.addDataSource(USER_TO_CREATE, TafDataSources.shared(TafDataSources.fromCsv(SINGLE_USER)));
        context.addDataSource(PasswordComplexityFlow.CHANGE_PASSWORD_BY_USER,
                TafDataSources.shared(TafDataSources.fromCsv(CHANGE_PASSWORD_BY_USER_CSV)));
        context.addDataSource(PasswordComplexityFlow.CHANGE_PASSWORD_BY_ADMIN,
                TafDataSources.shared(TafDataSources.fromCsv(CHANGE_PASSWORD_BY_ADMIN_CSV)));
        context.addDataSource(TEST_DS, TafDataSources.fromCsv(TEST_DS_CSV));

        final TestScenario passwordComplexityScenario = dataDrivenScenario("User Interface for password management, password complexity and forced password change GAT")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("20")))

                .addFlow(passwordComplexityFlows.changePassworDuringFirstLoginAndCheckPasswordPolicies())
                .addFlow(loginLogoutUiFlows.loginWithRedirect(SELF_CHANGE_PASSWORD_URL, CHANGE_PASSWORD_BY_USER))
                        .addFlow(passwordComplexityFlows.changePasswordByUserAndCheckPasswordPolicies())
                        .addFlow(changePasswordFlow.openLauncherWithURL()).addFlow(loginLogoutUiFlows.logout())
                .addFlow(loginLogoutUiFlows.loginWithRedirect(USER_MGMT_APP_NAME_PAGIN_500))
                .addFlow(passwordComplexityFlows.changePasswordByAdminAndCheckPasswordPolicies()).addFlow(loginLogoutUiFlows.logout())
                .addFlow(passwordComplexityFlows.changePassworDuringFirstLoginAndCheckPasswordPolicies())
                .addFlow(loginLogoutUiFlows.loginWithRedirect(LAUNCHER_URL, CHANGE_PASSWORD_BY_USER)).addFlow(loginLogoutUiFlows.logout())
                .withExceptionHandler(new UiExceptionHandler(context)).build();

        start(passwordComplexityScenario);
    }

    @TestSuite
    @Test(groups = { "Functional" }, priority = 1)
    public void passwordConsecutiveCharsTestTORF226521() throws Exception {
        context.addDataSource(PasswordComplexityFlow.CHANGE_PASSWORD_BY_ADMIN,
                TafDataSources.shared(TafDataSources.fromCsv(CHANGE_PASSWORD_UNCORRECT_CSV)));
        context.addDataSource(TEST_DS, TafDataSources.fromCsv(TEST_DS_CSV));

        final TestScenario passwordConsecutiveCharsScenario = dataDrivenScenario("Test_UI_Password Validation on UI User Management - Edit User Profile - Edit Password")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("21")))

                .addFlow(loginLogoutUiFlows.loginWithRedirect(USER_MGMT_APP_NAME_PAGIN_500))
                .addFlow(passwordComplexityFlows.changePasswordByAdminAndCheckConsecutiveCharsPolicyFail())
                .withExceptionHandler(new UiExceptionHandler(context)).build();

        start(passwordConsecutiveCharsScenario);
    }

    @TestSuite
    @Test(groups = { "Functional" }, priority = 1)
    public void specialCharacters() throws Exception {
        context.addDataSource(USER_TO_CREATE, TafDataSources.shared(TafDataSources.fromCsv(USERS_FOR_SPECIAL_CHARACTERS)));
        context.addDataSource(PasswordComplexityFlow.CHANGE_PASSWORD_BY_USER,
                TafDataSources.shared(TafDataSources.fromCsv(SPECIAL_CHARACTERS_CHANGE_PASSWORD_BY_USER_CSV)));
        context.addDataSource(TEST_DS, TafDataSources.fromCsv(TEST_DS_CSV));

        final TestScenario passwordSpecialCharactersScenario = dataDrivenScenario("User Interface for password complexity with special characters")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("22")))

                .addFlow(passwordComplexityFlows.changePassworDuringFirstLoginAndCheckPasswordPolicies())
                .addFlow(loginLogoutUiFlows.loginWithRedirect(SELF_CHANGE_PASSWORD_URL, CHANGE_PASSWORD_BY_USER))
                .addFlow(passwordComplexityFlows.changePasswordByUserAndCheckPasswordPolicies())
                .addFlow(changePasswordFlow.openLauncherWithURL()).addFlow(loginLogoutUiFlows.logout())
                .withExceptionHandler(new UiExceptionHandler(context)).build();

        start(passwordSpecialCharactersScenario);
    }

    private void loadInitialValidationRules() {
        context.removeDataSource(VALIDATION_RULES);
        context.addDataSource(VALIDATION_RULES, TafDataSources.shared(TafDataSources.fromCsv(DEFAULT_VALIDATION_RULES_CSV)));
        final TestScenario loadInitialValidationRulesScenario = scenario("Load initial validation rules scenario")
                .addFlow(validationRuleFlows.getValidationRules()).build();

        start(loadInitialValidationRulesScenario);
    }

    private void restoreInitialValidationRules() {
        context.removeDataSource(VALIDATION_RULES_TO_UPDATE);
        //populates VALIDATION_RULES_TO_UPDATE data source with initial validation rules set in AVAILABLE_VALIDATION_RULES data source
        context.addDataSource(VALIDATION_RULES_TO_UPDATE,
                TafDataSources.shared(
                        TafDataSources.transform(TafDataSources.fromClass(AvailableValidationRuleDataProvider.class, ValidationRuleDataRecord.class),
                        prepareRulesForHttpPut(TEMPLATE))));
        //context.addDataSource(VALIDATION_RULES_TO_UPDATE, shared(fromCsv(DEFAULT_VALIDATION_RULES_CSV)));
        final TestScenario restoreValidationRulesScenario = scenario("Restore initial validation rules scenario")
                .addFlow(validationRuleFlows.updateValidationRulesWithoutAssertion()).build();

        start(restoreValidationRulesScenario);
        DataHandler.unsetAttribute(AVAILABLE_VALIDATION_RULES);
    }

}
