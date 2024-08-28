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

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_CREATE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USER_TO_CLEAN_UP;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.USER;
import static com.ericsson.oss.testware.security.usermanagement.flows.TerminateSessionsFlow.DATA_SOURCE_TERMINATE_SESSIONS;

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
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.ericsson.oss.testware.security.usermanagement.flows.TerminateSessionsFlow;


public class TerminateSessionsScenario extends TafTestBase{
    final static String TERMINATE_SESSIONS_CSV = "data/usermanagement_2_0/TerminateSessions.csv";
    private final static String USER_MGMT_APP_NAME_PAGIN_500 = "/#usermanagement/?pagenumber=1&pagesize=500";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @Inject
    private TestContext context;

    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;

    @Inject
    private UserManagementTestFlows userManagementTestFlows;

    @Inject
    private TerminateSessionsFlow terminateSessionsFlow;

    @Inject
    private GimCleanupFlows gimCleanupFlows;

    @BeforeClass
    public void setUp() throws TimeoutException, InterruptedException {
        context.addDataSource(USERS_TO_CREATE, TafDataSources.shared(TafDataSources.fromCsv(TERMINATE_SESSIONS_CSV)));

        final TestScenario scenario = scenario("Create users")
                .addFlow(userManagementTestFlows.createUser())
                .build();

        start(scenario);

        context.removeDataSource(AVAILABLE_USERS);
    }

    @Test(groups = {"Acceptance", "RFA"})
    @TestSuite
    public void terminateSessionsFromActionBar()throws Exception {
        context.addDataSource(DATA_SOURCE_TERMINATE_SESSIONS, TafDataSources.shared(TafDataSources.fromCsv(TERMINATE_SESSIONS_CSV)));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Terminate Sessions From Action Bar")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("11")))
                .addFlow(terminateSessionsFlow.loginUserForTerminateSessions())
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME_PAGIN_500))
                .addFlow(terminateSessionsFlow.terminateSessionsFromActionBar())
                .addFlow(loginLogoutFlow.logout())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }


    @Test(groups = {"Acceptance", "RFA"})
    @TestSuite
    public void terminateSessionsFromProfileSummary()throws Exception {
        context.addDataSource(DATA_SOURCE_TERMINATE_SESSIONS, TafDataSources.shared(TafDataSources.fromCsv(TERMINATE_SESSIONS_CSV)));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Terminate Sessions From Profile Summary Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("12")))
                .addFlow(terminateSessionsFlow.loginUserForTerminateSessions())
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME_PAGIN_500))
                .addFlow(terminateSessionsFlow.terminateSessionsFromProfileSummary())
                .addFlow(loginLogoutFlow.logout())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

    @AfterClass(alwaysRun = true)
    void tearDown(){
        context.addDataSource(USER_TO_CLEAN_UP, TafDataSources.fromCsv(TERMINATE_SESSIONS_CSV));

        final TestScenario scenario = scenario("Delete users")
                .addFlow(gimCleanupFlows.cleanUp(USER))
                .build();

        start(scenario);

        context.removeDataSource(AVAILABLE_USERS);
    }

}
