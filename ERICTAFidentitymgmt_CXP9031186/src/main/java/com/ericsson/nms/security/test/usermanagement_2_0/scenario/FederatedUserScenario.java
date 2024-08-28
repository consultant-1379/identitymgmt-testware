/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
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
import static com.ericsson.nms.security.test.CommonUtils.start;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.nms.security.test.usermanagement.flows.FederatedUserManagementFlow;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;


public class FederatedUserScenario extends TafTestBase {
    private final static String USER_MGMT_FEDERATED_USERS_APP_NAME_PAGIN_500 = "/#usermanagement?federated=true/?pagenumber=1&pagesize=500";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @Inject
    private TestContext context;

    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;

    @Inject
    private FederatedUserManagementFlow federatedUserManagementFlow;

    @Test(groups = { "RFA" })
    @TestSuite
    public void testFederatedUser() throws Exception {
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Federated User Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("23")))
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_FEDERATED_USERS_APP_NAME_PAGIN_500))
                .addFlow(federatedUserManagementFlow.checkFederatedUsersTabFlow())
                .addFlow(loginLogoutFlow.logout())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

}
