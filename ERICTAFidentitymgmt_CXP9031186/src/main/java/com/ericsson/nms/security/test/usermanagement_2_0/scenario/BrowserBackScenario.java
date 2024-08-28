/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
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
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.nms.security.test.usermanagement.flows.BrowserBackOnUserManagementFlow;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;

public class BrowserBackScenario extends TafTestBase {

    @Inject
    private TestContext context;

    @Inject
    private BrowserBackOnUserManagementFlow browserBackOnUserManagementFlow;

    private final static String CSV_ADMIN_USER = "data/usermanagement_2_0/AdminUser.csv";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @Test(groups = {"Acceptance", "RFA"})
    @TestSuite
    public void testBrowserGoBack() {
        context.addDataSource(AVAILABLE_USERS, TafDataSources.shared(TafDataSources.fromCsv(CSV_ADMIN_USER)));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));
        final TestScenario scenario = dataDrivenScenario("Check Browser Back Button")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("19")))
                .addFlow(browserBackOnUserManagementFlow.checkBrowserBackOnUserManagementFlow())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }
}
