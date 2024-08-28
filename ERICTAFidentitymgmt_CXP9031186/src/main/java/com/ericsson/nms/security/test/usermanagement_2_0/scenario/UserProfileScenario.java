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
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_CREATE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USER_TO_CLEAN_UP;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.USER;
import static com.ericsson.oss.testware.security.usermanagement.flows.UserProfileFlow.DATA_SOURCE_CHECK_USER_PROFILE;

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
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.ericsson.oss.testware.security.usermanagement.flows.UserProfileFlow;


public class UserProfileScenario extends TafTestBase {
    static final String USER_TO_CHECK_PROFILE_CSV = "data/usermanagement_2_0/UserToCheckProfile.csv";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @Inject
    private TestContext context;

    @Inject
    private UserManagementTestFlows userManagementTestFlows;

    @Inject
    private UserProfileFlow userProfileFlow;

    @Inject
    private GimCleanupFlows gimCleanupFlows;

    @BeforeClass(groups = {"RFA"})
    public void setUp() throws TimeoutException, InterruptedException {
        context.addDataSource(USERS_TO_CREATE, TafDataSources.shared(TafDataSources.fromCsv(USER_TO_CHECK_PROFILE_CSV)));

        final TestScenario scenario = scenario("Create user to check profile")
                .addFlow(userManagementTestFlows.createUser())
                .build();

        start(scenario);

        context.removeDataSource(AVAILABLE_USERS);
    }

    @Test(groups = {"Acceptance", "RFA", "Check User Profile"})
    @TestSuite
    public void testCheckUserProfile() throws Exception {
        context.addDataSource(DATA_SOURCE_CHECK_USER_PROFILE, TafDataSources.shared(TafDataSources.fromCsv(USER_TO_CHECK_PROFILE_CSV)));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Check User Profile")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("13")))
                .addFlow(userProfileFlow.checkUserProfileDisplayingAndEditing())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

    @AfterClass(groups = { "RFA" }, alwaysRun = true)
    public void tearDown() {
        context.addDataSource(USER_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(USER_TO_CHECK_PROFILE_CSV)));

        final TestScenario scenario = scenario("Clean up users check profile")
                .addFlow(gimCleanupFlows.cleanUp(USER))
                .build();

        start(scenario);
    }

}
