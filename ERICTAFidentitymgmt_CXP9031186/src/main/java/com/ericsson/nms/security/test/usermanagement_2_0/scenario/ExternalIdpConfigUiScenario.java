/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
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
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_CREATE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_DELETE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USER_TO_CLEAN_UP;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.USER;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.oss.testware.enmbase.scenarios.DebugLogger;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutRestFlows;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.gic.flows.SystemSecurityConfigurationFlow;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;

public class ExternalIdpConfigUiScenario extends TafTestBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private TestContext context;
    @Inject
    private LoginLogoutUiFlows loginLogoutUiFlows;
    @Inject
    private LoginLogoutRestFlows loginlogoutFlow;
    @Inject
    private SystemSecurityConfigurationFlow sysSecConfFlows;
    @Inject
    protected UserManagementTestFlows userManagementTestFlows;
    @Inject
    private GimCleanupFlows gimCleanupFlows;

    protected TestScenarioRunner runner;

    private static final String CREATE_USERS_CSV = "data/usermanagement_2_0/extIdp/usersToCreate_Ui.csv";
    private static final String ADMIN_UI_USER = "data/usermanagement_2_0/extIdp/AdminUiUser.csv";
    private static final String IDP_CONFIG_DATA = "data/usermanagement_2_0/extIdp/ExternalIdpConfigData.csv";

    public static final String EXTERNAL_IDP_DATA_SOURCE = "ExternalIdpDataSource";
    public static final String ERROR_USERS = "errorUsers";

    private static final String TEST_ID_01 = "MR57210_Q2_Functional_ExtIdP_GUI-based_configuration_01";
    private static final String TEST_ID_02 = "MR57210_Q2_Functional_ExtIdP_GUI-based_configuration_02";
    private static final String TEST_ID_03 = "MR57210_Q2_Functional_ExtIdP_GUI-based_configuration_03";
    private static final String TEST_NOSEARCH_LOCAL = "ExtIdP: NOSEARCH-LOCAL";
    private static final String TEST_NOSEARCH_REMOTE = "ExtIdP: NOSEARCH-REMOTE";
    private static final String TEST_STANDARD_REMOTE = "ExtIdP: STANDARD-REMOTE";

    private static final String SSC_URL = "/#syssecconfig";

    @TestId(id = TEST_ID_01, title = TEST_NOSEARCH_LOCAL)
    @Test(groups = { "Functional" }, priority = 1)
    public void extIdPUiConfigNosearchLocal() {

        logger.info("Test scenario: " + TEST_NOSEARCH_LOCAL);

        context.addDataSource(AVAILABLE_USERS, fromCsv(ADMIN_UI_USER));
        context.addDataSource(USERS_TO_CREATE, fromCsv(CREATE_USERS_CSV));

        final TestScenario scenario = scenario(TEST_NOSEARCH_LOCAL)
                .addFlow(loginLogoutUiFlows.loginWithRedirect(SSC_URL, PredicatesExt.extLdapContextFilter("1"))) //Login with Administrator to SysSecConf
                .addFlow(sysSecConfFlows.setExternalIdpFlow(PredicatesExt.extLdapContextFilter("2"))) //configure Ext IdP via UI as LOCAL
                .addFlow(userManagementTestFlows.createUserWithoutRoleVerification()) //create emarccr user and verify its login as LOCAL user
                .addFlow(loginLogoutUiFlows.logout()).build();
        start(scenario);
    }

    @TestId(id = TEST_ID_02, title = TEST_NOSEARCH_REMOTE)
    @Test(groups = { "Functional" }, priority = 2)
    public void extIdPUiConfigNosearchRemote() {

        logger.info("Test scenario: " + TEST_NOSEARCH_REMOTE);

        context.addDataSource(AVAILABLE_USERS, fromCsv(ADMIN_UI_USER));
        context.addDataSource(ERROR_USERS, fromCsv(ADMIN_UI_USER));

        final TestScenario scenario = scenario(TEST_NOSEARCH_REMOTE)
                .addFlow(loginLogoutUiFlows.loginWithRedirect(SSC_URL, PredicatesExt.extLdapContextFilter("1"))) //Login with Administrator to SysSecConf
                .addFlow(sysSecConfFlows.setExternalIdpFlow(PredicatesExt.extLdapContextFilter("1"))) //configure Ext IdP via UI as NOSEARCH-REMOTE
                .addFlow(loginlogoutFlow.unsuccessfulLogin(PredicatesExt.extLdapContextFilter("2"))) //unsuccessful login as emarccr with local pswd
                .addFlow(loginlogoutFlow.login(PredicatesExt.extLdapContextFilter("3"))) //login as emarccr with REMOTE pswd
                .addFlow(loginlogoutFlow.logout()).build();
        start(scenario);
    }

    @TestId(id = TEST_ID_03, title = TEST_STANDARD_REMOTE)
    @Test(groups = { "Functional" }, priority = 3)
    public void extIdPUiConfigStandardRemote() {

        logger.info("Test scenario: " + TEST_STANDARD_REMOTE);

        context.addDataSource(AVAILABLE_USERS, fromCsv(ADMIN_UI_USER));
        context.addDataSource(ERROR_USERS, fromCsv(ADMIN_UI_USER));

        final TestScenario scenario = scenario(TEST_STANDARD_REMOTE)
                .addFlow(loginLogoutUiFlows.loginWithRedirect(SSC_URL, PredicatesExt.extLdapContextFilter("1"))) //Login with Administrator to SysSecConf
                .addFlow(sysSecConfFlows.setExternalIdpFlow(PredicatesExt.extLdapContextFilter("11"))) //configure Ext IdP via UI as STANDARD-REMOTE
                .addFlow(loginlogoutFlow.unsuccessfulLogin(PredicatesExt.extLdapContextFilter("2"))) //unsuccessful login as emarccr with local pswd
                .addFlow(loginlogoutFlow.login(PredicatesExt.extLdapContextFilter("3"))) //login as emarccr with REMOTE pswd
                .addFlow(loginlogoutFlow.logout()).build();
        start(scenario);
    }

    @BeforeSuite
    public void setUp() {
        context.addDataSource(USERS_TO_DELETE, fromCsv(CREATE_USERS_CSV));
        context.addDataSource(EXTERNAL_IDP_DATA_SOURCE, fromCsv(IDP_CONFIG_DATA));

        final TestScenario scenario = scenario("Clean required users").addFlow(userManagementTestFlows.deleteUser())
                .withExceptionHandler(ScenarioExceptionHandler.LOGONLY).build();
        start(scenario);

    }

    @AfterSuite(alwaysRun = true)
    public void tearDown() {
        context.addDataSource(USER_TO_CLEAN_UP, fromCsv(CREATE_USERS_CSV));

        final TestScenario scenario = scenario("Delete users")
                .addFlow(loginLogoutUiFlows.loginWithRedirect(SSC_URL, PredicatesExt.extLdapContextFilter("1"))) //Login with Administrator to SysSecConf
                .addFlow(sysSecConfFlows.setExternalIdpFlow(PredicatesExt.extLdapContextFilter("2"))) //configure Ext IdP via UI as NOSEARCH-LOCAL
                .addFlow(loginLogoutUiFlows.logout()).addFlow(loginLogoutUiFlows.closeTool()).addFlow(gimCleanupFlows.cleanUp(USER)).build();
        executeScenario(scenario);

    }

    public void executeScenario(final TestScenario scenario) {
        runner().withListener(new LoggingScenarioListener()).withListener(new DebugLogger()).build().start(scenario);
    }

}
