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

package com.ericsson.nms.security.test.targetgroupmanagement.scenario;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.nms.security.test.CommonUtils.start;

import javax.inject.Inject;

import org.testng.annotations.AfterSuite;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.oss.testware.enmbase.data.CommonDataSources;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType;
import com.ericsson.oss.testware.security.targetgroupmanagement.data.TGMDataSources;
import com.ericsson.oss.testware.security.targetgroupmanagement.flows.TGMNodeIntegrationFlows;

public class CleanupNodesScenario extends TafTestBase {

    public static final String TARGET_GROUP_MGMT_CSV_PATH = "data/targetgroupmanagement/";
    public static final String TARGET_GROUPS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "testTG.csv";
    public static final String NODES_TO_ADD_TARGET_GROUPS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "nodeListNetEx.csv";
    public static final String VNFM_NODE_TO_ADD_TARGET_GROUPS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "vnfmNode.csv";
    public static final String TARGET_GROUPS_EDIT_ADD_USERS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "editTgmUsers.csv";
    // For DataDriven
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = TARGET_GROUP_MGMT_CSV_PATH + "testDataSource.csv";

    @Inject
    private TestContext context;
    @Inject
    private GimCleanupFlows gimCleanupFlows;
    @Inject
    private TGMNodeIntegrationFlows tgmNodeIntegrationFlows;

    @TestSuite
    @AfterSuite(groups = { "RFA" }, alwaysRun = true)
    public void tearDown() {
        context.addDataSource(CommonDataSources.TARGET_GROUP_TO_CLEAN_UP, fromCsv(TARGET_GROUPS_CSV));
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(TARGET_GROUPS_EDIT_ADD_USERS_CSV)));
        context.addDataSource(CommonDataSources.NODES_TO_ADD, fromCsv(NODES_TO_ADD_TARGET_GROUPS_CSV));
        context.addDataSource(TGMDataSources.VNFM_NODE_TO_ADD, fromCsv(VNFM_NODE_TO_ADD_TARGET_GROUPS_CSV));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));
        final TestScenario scenario = dataDrivenScenario("Clean up users target groups and nodes")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("8")))
                .addFlow(gimCleanupFlows.cleanUp(EnmObjectType.USER, EnmObjectType.TARGET_GROUP))
                .addFlow(tgmNodeIntegrationFlows.removeVNFMTargets(CommonDataSources.AVAILABLE_USERS, TGMDataSources.VNFM_NODE_TO_ADD))
                .addFlow(tgmNodeIntegrationFlows.removeTargets(CommonDataSources.AVAILABLE_USERS, CommonDataSources.NODES_TO_ADD)).build();
        start(scenario);
        context.removeDataSource(CommonDataSources.AVAILABLE_USERS);
    }

}
