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
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.ericsson.oss.testware.security.targetgroupmanagement.data.TGMDataSources;
import com.ericsson.oss.testware.security.targetgroupmanagement.flows.TGMEditFlow;
import com.ericsson.oss.testware.security.targetgroupmanagement.flows.TGMNodeIntegrationFlows;
import com.ericsson.oss.testware.security.targetgroupmanagement.utility.TGMUiUtils;

public class TGMEditScenario extends TafTestBase {
    public static final String REDIRECT_TG_LIST_PAGE = "/"
            + TGMUiUtils.getApplicationRelativeUrl(TGMUiUtils.APP_NAME_TARGET_GROUP_MANAGEMENT_FILTER_DOT_NAME);
    public static final String TARGET_GROUP_MGMT_CSV_PATH = "data/targetgroupmanagement/";
    public static final String TARGET_GROUPS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "testTG.csv";
    public static final String TARGET_GROUPS_EDIT_ADD_REMOVE_CSV = TARGET_GROUP_MGMT_CSV_PATH + "removeTargets.csv";
    public static final String NODES_TO_ADD_TARGET_GROUPS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "nodeListNetEx.csv";
    public static final String VNFM_NODE_TO_ADD_TARGET_GROUPS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "vnfmNode.csv";
    public static final String NODES_TO_REMOVE_TARGET_GROUPS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "nodeToRemove.csv";
    public static final String TARGET_GROUPS_EDIT_ADD_USERS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "editTgmUsers.csv";
    public static final String NODES_TO_REMOVE = "nodesToRemove";
    public static final String ALL_NODES_TO_REMOVE = "allNodesToRemove";
    public static final String USERS_TO_LOGIN_DS = "usersToLogin";
    // For DataDriven
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "testDataSource.csv";

    @Inject
    private TestContext context;
    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;
    @Inject
    private TargetGroupManagementTestFlows targetGroupManagementTestFlows;
    @Inject
    private TGMEditFlow tgmEditFlow;
    @Inject
    private GimCleanupFlows gimCleanupFlows;
    @Inject
    private TGMNodeIntegrationFlows tgmNodeIntegrationFlows;
    @Inject
    private UserManagementTestFlows userManagementTestFlows;

    @Test(groups = { "RFA" })
    @TestSuite
    public void testTargetGroupEdit() {
        context.addDataSource(CommonDataSources.TARGET_GROUP_TO_CREATE, TafDataSources.fromCsv(TARGET_GROUPS_CSV));
        context.addDataSource(TGMDataSources.TARGET_GROUP_TO_EDIT, TafDataSources.fromCsv(TARGET_GROUPS_EDIT_ADD_REMOVE_CSV));
        context.addDataSource(TEST_DS, TafDataSources.fromCsv(TEST_DS_CSV));
        context.addDataSource(USERS_TO_LOGIN_DS, TafDataSources.shared(TafDataSources.fromCsv(TARGET_GROUPS_EDIT_ADD_USERS_CSV)));

        final TestScenario scenario = TestScenarios.dataDrivenScenario("Target Group Management UI - Edit Target Group Assign/Unassign Target(s)")
                .withScenarioDataSources(TestScenarios.dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("6")))
                .addFlow(targetGroupManagementTestFlows.createTargetGroup())
                .addFlow(loginLogoutFlow.loginWithRedirect(REDIRECT_TG_LIST_PAGE, USERS_TO_LOGIN_DS))
                .addFlow(tgmEditFlow.verifyEditTargetGroup())
                .addFlow(tgmEditFlow.verifyAssignTargetsToTargetGroup())
                .addFlow(tgmEditFlow.verifyRemovingSingleTargetFromTargetGroup())
                .addFlow(tgmEditFlow.verifyRemovingAllTargetsFromTargetGroup())
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
        context.addDataSource(CommonDataSources.NODES_TO_ADD, TafDataSources.fromCsv(NODES_TO_ADD_TARGET_GROUPS_CSV));
        context.addDataSource(TGMDataSources.VNFM_NODE_TO_ADD, TafDataSources.fromCsv(VNFM_NODE_TO_ADD_TARGET_GROUPS_CSV));
        context.addDataSource(NODES_TO_REMOVE, TafDataSources.fromCsv(NODES_TO_REMOVE_TARGET_GROUPS_CSV));
        context.addDataSource(ALL_NODES_TO_REMOVE, TafDataSources.fromCsv(NODES_TO_REMOVE_TARGET_GROUPS_CSV));
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(TARGET_GROUPS_EDIT_ADD_USERS_CSV)));
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.shared(TafDataSources.fromCsv(TARGET_GROUPS_EDIT_ADD_USERS_CSV)));
        final TestScenario scenario = TestScenarios.scenario("Target Group Management UI - Edit Target Group Assign/Unassign Target(s) clean up and setup")
                .addFlow(gimCleanupFlows.cleanUp(EnmObjectType.USER))
                .addFlow(tgmNodeIntegrationFlows.addTargets(CommonDataSources.AVAILABLE_USERS, CommonDataSources.NODES_TO_ADD))
                .addFlow(tgmNodeIntegrationFlows.addVNFMTargets(CommonDataSources.AVAILABLE_USERS, TGMDataSources.VNFM_NODE_TO_ADD))
                .addFlow(userManagementTestFlows.createUser())
                .build();
        final TestScenarioRunner runner = TestScenarios.runner().withListener(new LoggingScenarioListener()).build();
        runner.start(scenario);

    }

    @AfterClass(groups = { "RFA" }, alwaysRun = true)
    public void tearDown() {
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(TARGET_GROUPS_EDIT_ADD_USERS_CSV)));
        context.addDataSource(CommonDataSources.TARGET_GROUP_TO_CLEAN_UP, TafDataSources.fromCsv(TARGET_GROUPS_CSV));
        final TestScenario scenario = TestScenarios.scenario("Clean up users check profile")
                .addFlow(gimCleanupFlows.cleanUp(EnmObjectType.USER, EnmObjectType.TARGET_GROUP))
                .build();
        CommonUtils.start(scenario);
        context.removeDataSource(CommonDataSources.AVAILABLE_USERS);
    }

    private void cleanUpTargetGroups() {
        context.addDataSource(CommonDataSources.TARGET_GROUP_TO_CLEAN_UP, TafDataSources.fromCsv(TARGET_GROUPS_CSV));
        context.addDataSource(CommonDataSources.NODES_TO_ADD, TafDataSources.fromCsv(NODES_TO_ADD_TARGET_GROUPS_CSV));
        context.addDataSource(TGMDataSources.VNFM_NODE_TO_ADD, TafDataSources.fromCsv(VNFM_NODE_TO_ADD_TARGET_GROUPS_CSV));
        final TestScenario scenario = TestScenarios.scenario("Clean up target group and targets")
                .addFlow(gimCleanupFlows.cleanUp(EnmObjectType.TARGET_GROUP))
                .addFlow(tgmNodeIntegrationFlows.removeVNFMTargets(CommonDataSources.AVAILABLE_USERS, TGMDataSources.VNFM_NODE_TO_ADD))
                .addFlow(tgmNodeIntegrationFlows.removeTargets(CommonDataSources.AVAILABLE_USERS, CommonDataSources.NODES_TO_ADD)).build();
        CommonUtils.start(scenario);
    }
}
