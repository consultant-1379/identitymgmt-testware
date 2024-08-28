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

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.TARGET_GROUP_TO_CLEAN_UP;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.TARGET_GROUP_TO_CREATE;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.TARGET_GROUP;
import static com.ericsson.oss.testware.security.targetgroupmanagement.utility.TGMUiUtils.APP_NAME_TARGET_GROUP_MANAGEMENT;
import static com.ericsson.oss.testware.security.targetgroupmanagement.utility.TGMUiUtils.getApplicationRelativeUrl;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.TargetGroupManagementTestFlows;
import com.ericsson.oss.testware.security.targetgroupmanagement.flows.TGMDisplayFlow;

public class TGMDisplayScenario extends TafTestBase {
    public static final String REDIRECT_TG_LIST_PAGE = "/" + getApplicationRelativeUrl(APP_NAME_TARGET_GROUP_MANAGEMENT);
    public static final String TARGET_GROUP_MGMT_CSV_PATH = "data/targetgroupmanagement/";
    public static final String TARGET_GROUPS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "testTG.csv";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "testDataSource.csv";

    @Inject
    private TestContext context;
    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;
    @Inject
    private TGMDisplayFlow tgmDisplayFlow;
    @Inject
    private GimCleanupFlows gimCleanupFlows;
    @Inject
    private TargetGroupManagementTestFlows targetGroupManagementTestFlows;

    @Test(groups = {"RFA"})
    @TestSuite
    public void testTargetGroupDisplay() {
        context.addDataSource(TARGET_GROUP_TO_CREATE, fromCsv(TARGET_GROUPS_CSV));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Target Group Management UI - Display Target Group")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("3")))
                .addFlow(targetGroupManagementTestFlows.createTargetGroup())
                .addFlow(loginLogoutFlow.loginWithRedirect(REDIRECT_TG_LIST_PAGE))
                .addFlow(tgmDisplayFlow.verifyDisplayingTargetGroup())
                .addFlow(tgmDisplayFlow.verifyDisplayingNonExistingTargetGroup())
                .addFlow(loginLogoutFlow.logout())
                .addFlow(loginLogoutFlow.cleanUpFlow())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        final TestScenarioRunner runner = runner().withListener(new LoggingScenarioListener()).build();
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
        context.addDataSource(TARGET_GROUP_TO_CLEAN_UP, fromCsv(TARGET_GROUPS_CSV));
        final TestScenario scenario = scenario("Clean up target group").addFlow(gimCleanupFlows.cleanUp(TARGET_GROUP)).build();
        start(scenario);
    }
}
