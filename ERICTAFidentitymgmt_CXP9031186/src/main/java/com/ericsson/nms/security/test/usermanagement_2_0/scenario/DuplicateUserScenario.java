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

import static com.ericsson.cifwk.taf.datasource.TafDataSources.combine;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_CREATE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USER_TO_CLEAN_UP;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.USER;
import static com.ericsson.oss.testware.security.usermanagement.flows.DuplicateUserFlow.DATA_SOURCE_DUPLICATED_USER_DATA;
import static com.ericsson.oss.testware.security.usermanagement.flows.DuplicateUserFlow.DATA_SOURCE_USER_TO_DUPLICATE;

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
import com.ericsson.oss.testware.security.gim.steps.UserManagementTestSteps;
import com.ericsson.oss.testware.security.usermanagement.flows.DuplicateUserFlow;


public class DuplicateUserScenario extends TafTestBase {
    final static String USER_TO_DUPLICATE_CSV = "data/usermanagement_2_0/UserToDuplicate.csv";
    final static String DUPLICATED_USER_DATA_CSV = "data/usermanagement_2_0/DuplicatedUserData.csv";
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
    private DuplicateUserFlow duplicateUserFlow;

    @Inject
    private GimCleanupFlows gimCleanupFlows;

    @BeforeClass(groups = {"RFA", "RFA250"})
    public void setUp() throws TimeoutException, InterruptedException {
        context.addDataSource(USERS_TO_CREATE, shared(fromCsv(USER_TO_DUPLICATE_CSV)));

        final TestScenario scenario = scenario("Create user to duplicate")
                .addFlow(userManagementTestFlows.createUser())
                .build();

        start(scenario);

        context.removeDataSource(AVAILABLE_USERS);
        context.removeDataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE);
    }

    @Test(groups = {"Acceptance", "RFA", "RFA250", "Duplicate User"})
    @TestSuite
    public void testDuplicateUser() throws Exception {
        context.addDataSource(DATA_SOURCE_USER_TO_DUPLICATE, shared(fromCsv(USER_TO_DUPLICATE_CSV)));
        context.addDataSource(DATA_SOURCE_DUPLICATED_USER_DATA, shared(fromCsv(DUPLICATED_USER_DATA_CSV)));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Duplicate User Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("4")))
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME_PAGIN_500))
                .addFlow(duplicateUserFlow.duplicateUser())
                .addFlow(loginLogoutFlow.logout())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

    @AfterClass(groups = {"RFA", "RFA250"},alwaysRun = true)
    public void tearDown() {
        context.addDataSource(USER_TO_CLEAN_UP, shared(combine(
                fromCsv(USER_TO_DUPLICATE_CSV),
                fromCsv(DUPLICATED_USER_DATA_CSV))));

        final TestScenario scenario = scenario("Delete users")
                .addFlow(gimCleanupFlows.cleanUp(USER))
                .build();

        start(scenario);
    }

}
