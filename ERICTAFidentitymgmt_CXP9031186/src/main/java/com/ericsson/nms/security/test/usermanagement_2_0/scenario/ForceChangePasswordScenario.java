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
import static com.ericsson.oss.testware.security.usermanagement.flows.ForceChangePasswordFlow.DATA_SOURCE_FORCE_USER;

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
import com.ericsson.oss.testware.security.usermanagement.flows.ForceChangePasswordFlow;

public class ForceChangePasswordScenario  extends TafTestBase {
    final static String CSV_CREATE_USER = "data/usermanagement_2_0/ForceChangePasswordUser.csv";
    private final static String CSV_FORCE_CHANGE_PASSWORD = "data/usermanagement_2_0/ForceChangePasswordData.csv";
    private final static String USER_MGMT_APP_NAME_PAGIN_500 = "/#usermanagement/?pagenumber=1&pagesize=500";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @Inject
    private TestContext context;

    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;

    @Inject
    private ForceChangePasswordFlow forceChangePasswordFlow;

    @Inject
    private UserManagementTestFlows userManagementTestFlows;

    @Inject
    private GimCleanupFlows gimCleanupFlows;

    @Inject
    private LoginDelayFlow loginDelayFlow;

    @BeforeClass(groups = {"RFA", "RFA250"})
    public void setUp() throws TimeoutException, InterruptedException {
        context.addDataSource(USERS_TO_CREATE, TafDataSources.shared(TafDataSources.fromCsv(CSV_CREATE_USER)));

        final TestScenario scenario = scenario("Create user for Force Change Password Scenario")
                .addFlow(userManagementTestFlows.createUser())
                .build();

        start(scenario);

        context.removeDataSource(AVAILABLE_USERS);
    }

    @Test(groups = {"Acceptance", "RFA", "RFA250", "Force Change Password"})
    @TestSuite
    public void testForceChangePassword() throws Exception {

        context.addDataSource(DATA_SOURCE_FORCE_USER, TafDataSources.shared(TafDataSources.fromCsv(CSV_CREATE_USER)));
        context.addDataSource(FORCE_PASSWORD_CHANGE, TafDataSources.shared(TafDataSources.fromCsv(CSV_FORCE_CHANGE_PASSWORD)));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Force Change Password Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("8")))
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME_PAGIN_500))
                .addFlow(forceChangePasswordFlow.forceChangePassword())
                .addFlow(loginLogoutFlow.logout())
                .addFlow(loginDelayFlow.delayLogin())
                .addFlow(loginLogoutFlow.loginForceChangePassword())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

    @AfterClass(groups = {"RFA", "RFA250"}, alwaysRun = true)
    public void tearDown() {
        context.addDataSource(USER_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(CSV_CREATE_USER)));

        final TestScenario scenario = scenario("Delete users")
                .addFlow(gimCleanupFlows.cleanUp(USER))
                .build();

        start(scenario);

        context.removeDataSource(AVAILABLE_USERS);
    }
}
