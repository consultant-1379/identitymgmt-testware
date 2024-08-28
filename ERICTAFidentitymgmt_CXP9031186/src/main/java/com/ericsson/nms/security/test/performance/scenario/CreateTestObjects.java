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
package com.ericsson.nms.security.test.performance.scenario;

//import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ROLE_TO_CREATE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.TARGET_GROUP_TO_CREATE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USERS_TO_CREATE;

import javax.inject.Inject;

import org.testng.annotations.BeforeSuite;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.nms.security.test.performance.data.RolesToCreate;
import com.ericsson.nms.security.test.performance.data.TargetGroupsToCreate;
import com.ericsson.nms.security.test.performance.data.UsersToCreate;
import com.ericsson.oss.testware.security.gim.flows.RoleManagementTestFlows;
import com.ericsson.oss.testware.security.gim.flows.TargetGroupManagementTestFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;

public class CreateTestObjects extends TafTestBase {

    @Inject
    private TestContext context;

    @Inject
    private UserManagementTestFlows userMgmtFlow;

    @Inject
    private RoleManagementTestFlows roleMgmtFlow;

    @Inject
    private TargetGroupManagementTestFlows targetGroupMgmtFlow;

    @BeforeSuite
    public void setUp() throws InterruptedException {
        context.addDataSource(USERS_TO_CREATE, TafDataSources.fromClass(UsersToCreate.class));
        context.addDataSource(ROLE_TO_CREATE, TafDataSources.fromClass(RolesToCreate.class));
        context.addDataSource(TARGET_GROUP_TO_CREATE, TafDataSources.fromClass(TargetGroupsToCreate.class));

        final TestScenario scenario = scenario("Create test target groups, roles and users")
                .addFlow(roleMgmtFlow.createRole())
                .addFlow(targetGroupMgmtFlow.createTargetGroup())
                .addFlow(userMgmtFlow.createUser())
                .build();

        start(scenario);
    }

}
