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


import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.nms.security.test.CommonUtils;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.oss.testware.enmbase.data.CommonDataSources;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType;
import com.ericsson.oss.testware.security.gim.flows.RoleManagementTestFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.ericsson.oss.testware.security.usermanagement.flows.GroupEditFlow;


public class GroupEditNegativeScenario extends TafTestBase {
    final static String USER_FOR_GROUP_EDIT_NEGATIVE_CSV = "data/usermanagement_2_0/groupEdit/UserForGroupEditNegative.csv";
    private final static String GROUP_EDIT_NEGATIVE_CSV = "data/usermanagement_2_0/groupEdit/GroupEditNegative.csv";
    private final static String ROLES_FOR_GROUP_EDIT_NEGATIVE_CSV = "data/usermanagement_2_0/groupEdit/GroupEditRolesNegative.csv";
    private final static String USER_MGMT_APP_NAME = "/#usermanagement/?filter={\"username\"%3A\"UserManagement_GroupEditUser\"}";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @Inject
    private TestContext context;

    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;

    @Inject
    private UserManagementTestFlows userManagementTestFlows;

    @Inject
    private GroupEditFlow groupEditFlow;

    @Inject
    private GimCleanupFlows gimCleanupFlows;

    @Inject
    private RoleManagementTestFlows roleManagementTestFlows;

    @BeforeClass(groups = {"RFA"})
    public void setUp() throws TimeoutException, InterruptedException {
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.fromCsv(USER_FOR_GROUP_EDIT_NEGATIVE_CSV));
        context.addDataSource(CommonDataSources.ROLE_TO_CREATE, TafDataSources.fromCsv(ROLES_FOR_GROUP_EDIT_NEGATIVE_CSV));
        context.addDataSource(CommonDataSources.ROLE_TO_CLEAN_UP, TafDataSources.fromCsv(ROLES_FOR_GROUP_EDIT_NEGATIVE_CSV));
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP, TafDataSources.fromCsv(USER_FOR_GROUP_EDIT_NEGATIVE_CSV));

        final TestScenario scenario = TestScenarios.scenario("Create users for group edit Negative")
                .addFlow(gimCleanupFlows.cleanUp(EnmObjectType.USER))
                .addFlow(gimCleanupFlows.cleanUp(EnmObjectType.ROLE))
                .addFlow(roleManagementTestFlows.createRole())
                .addFlow(userManagementTestFlows.createUser())
                .build();

        CommonUtils.start(scenario);

        context.removeDataSource(CommonDataSources.AVAILABLE_USERS);
    }

    @Test(groups = { "Acceptance", "RFA", "Group Edit" })
    @TestSuite
    public void testGroupEdit() {
        context.addDataSource(GroupEditFlow.DATA_SOURCE_GROUP_EDIT_NEGATIVE, TafDataSources.shared(TafDataSources.fromCsv(GROUP_EDIT_NEGATIVE_CSV)));
        context.addDataSource(TEST_DS, TafDataSources.fromCsv(TEST_DS_CSV));
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Group Edit Scenario")
                .withScenarioDataSources(TestScenarios.dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("17")))
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME))
                .addFlow(groupEditFlow.groupEditNegative())
                .addFlow(loginLogoutFlow.logout())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        CommonUtils.start(scenario);
    }

    @AfterClass(groups = { "RFA" }, alwaysRun = true)
    public void tearDown() {
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(USER_FOR_GROUP_EDIT_NEGATIVE_CSV)));

        final TestScenario scenario = TestScenarios.scenario("Delete users")
                .addFlow(gimCleanupFlows.cleanUp(EnmObjectType.USER))
                .build();
        CommonUtils.start(scenario);
    }
}
