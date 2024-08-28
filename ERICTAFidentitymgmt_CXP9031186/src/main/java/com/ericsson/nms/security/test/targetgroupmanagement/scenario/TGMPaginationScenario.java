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
 -----------------------------------------------------------------------------*/
package com.ericsson.nms.security.test.targetgroupmanagement.scenario;


import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.nms.security.test.CommonUtils;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.oss.testware.enmbase.data.CommonDataSources;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType;
import com.ericsson.oss.testware.security.gim.flows.TargetGroupManagementTestFlows;
import com.ericsson.oss.testware.security.targetgroupmanagement.data.TGMListTestResult;
import com.ericsson.oss.testware.security.targetgroupmanagement.flows.TGMCommonFlow;
import com.ericsson.oss.testware.security.targetgroupmanagement.flows.TGMListFlow;
import com.ericsson.oss.testware.security.targetgroupmanagement.utility.TGMUiUtils;

public class TGMPaginationScenario extends TafTestBase {
    public static final String REDIRECT_TG_PAGINATION_LIST_PAGE = "/"
            + TGMUiUtils.getApplicationRelativeUrl(TGMUiUtils.APP_NAME_TARGET_GROUP_PAGINATION_FILTER_NAME);
    public static final String TARGET_GROUP_MGMT_CSV_PATH = "data/targetgroupmanagement/";
    public static final String TARGET_GROUPS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "testTG21.csv";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "testDataSource.csv";

    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;
    @Inject
    private TGMCommonFlow tgmCommonFlow;
    @Inject
    private TGMListFlow tgmListFlow;
    @Inject
    private TestContext context;
    @Inject
    private GimCleanupFlows gimCleanupFlows;
    @Inject
    private TargetGroupManagementTestFlows targetGroupManagementTestFlows;

    @Test(groups = {"RFA"})
    @TestSuite
    public void testTargetGroupList() {

        context.addDataSource(TGMUiUtils.EXPECTED_TEST_RESULTS,
                TafDataSources.merge(TafDataSources.cyclic(TafDataSources.fromClass(TGMListTestResult.class)),
                        TafDataSources.fromCsv(TGMUiUtils.CSV_PATH + TGMUiUtils.CSV_PAGINATION + TGMUiUtils.CSV_FILE_POSTFIX)));
        context.addDataSource(CommonDataSources.TARGET_GROUP_TO_CREATE, TafDataSources.fromCsv(TARGET_GROUPS_CSV));
        context.addDataSource(TEST_DS, TafDataSources.fromCsv(TEST_DS_CSV));

        final TestScenario scenario = TestScenarios.dataDrivenScenario("Target Group Management UI - List All Target Groups")
                .withScenarioDataSources(TestScenarios.dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("5")))
                .addFlow(targetGroupManagementTestFlows.createTargetGroup())
                .addFlow(loginLogoutFlow.loginWithRedirect(REDIRECT_TG_PAGINATION_LIST_PAGE))
                .addFlow(tgmListFlow.verifyDefaultElements())
                .addFlow(tgmListFlow.checkTGListWithPagination(TGMUiUtils.EXPECTED_TEST_RESULTS))
                .addFlow(loginLogoutFlow.logout())
                .addFlow(loginLogoutFlow.cleanUpFlow())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        final TestScenarioRunner runner = TestScenarios.runner().withListener(new LoggingScenarioListener()).build();
        runner.start(scenario);
    }

    @BeforeClass(groups = { "RFA" }, alwaysRun = true)
    public void setUp() {
        cleanUpTargetGroups();
    }

    @AfterClass(groups = { "RFA" }, alwaysRun = true)
    public void tearDown() {
        cleanUpTargetGroups();
    }

    private void cleanUpTargetGroups() {
        context.addDataSource(CommonDataSources.TARGET_GROUP_TO_CLEAN_UP, TafDataSources.fromCsv(TARGET_GROUPS_CSV));
        final TestScenario scenario = TestScenarios.scenario("Clean up target group").addFlow(gimCleanupFlows.cleanUp(EnmObjectType.TARGET_GROUP))
                .build();
        CommonUtils.start(scenario);
    }
}
