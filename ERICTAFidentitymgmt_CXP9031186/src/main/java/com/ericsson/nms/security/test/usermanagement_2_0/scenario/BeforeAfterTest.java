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

package com.ericsson.nms.security.test.usermanagement_2_0.scenario;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ROLE_TO_CLEAN_UP;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ROLE_TO_CREATE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.TARGET_GROUP_TO_CLEAN_UP;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.TARGET_GROUP_TO_CREATE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.USER_TO_CLEAN_UP;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.ROLE;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.TARGET_GROUP;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.USER;

import javax.inject.Inject;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.RoleManagementTestFlows;
import com.ericsson.oss.testware.security.gim.flows.TargetGroupManagementTestFlows;

public class BeforeAfterTest extends TafTestBase {
    private final static String USER_MGMT_CSV_PATH = "data/usermanagement_2_0/";
    private final static String TARGET_GROUPS_CSV = USER_MGMT_CSV_PATH + "TargetGroups.csv";
    private final static String ROLES_CSV = USER_MGMT_CSV_PATH + "Roles.csv";
    private final static String ROLES_DELETE_CSV = USER_MGMT_CSV_PATH + "RolesDelete.csv";

    @Inject
    private TestContext context;

    @Inject
    private TargetGroupManagementTestFlows targetGroupManagementTestFlows;

    @Inject
    private RoleManagementTestFlows roleManagementTestFlows;

    @Inject
    private GimCleanupFlows gimCleanupFlows;


    @BeforeSuite(groups = {"RFA", "RFA250"})
    @SuppressWarnings("unchecked")
    public void setUp() throws TimeoutException, InterruptedException {
        context.addDataSource(TARGET_GROUP_TO_CREATE, TafDataSources.shared(TafDataSources.fromCsv(TARGET_GROUPS_CSV)));
        context.addDataSource(ROLE_TO_CREATE, TafDataSources.shared(TafDataSources.fromCsv(ROLES_CSV)));
        context.addDataSource(TARGET_GROUP_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(TARGET_GROUPS_CSV)));
        context.addDataSource(ROLE_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(ROLES_DELETE_CSV)));
        context.addDataSource(USER_TO_CLEAN_UP,  TafDataSources.shared(TafDataSources.combine(
                TafDataSources.fromCsv(ChangePasswordScenario.CSV_CREATE_USER),
                TafDataSources.fromCsv(CreateEditAndDeleteUserScenario.CREATE_USER_CSV),
                TafDataSources.fromCsv(CreateEditAndDeleteUserScenario.CREATE_USER_FOR_DELETE_ON_EDIT_CSV),
                TafDataSources.fromCsv(CredentialManagementScenario.USERS_CSV),
                TafDataSources.fromCsv(DuplicateUserScenario.USER_TO_DUPLICATE_CSV),
                TafDataSources.fromCsv(DuplicateUserScenario.DUPLICATED_USER_DATA_CSV),
                TafDataSources.fromCsv(ForceChangePasswordScenario.CSV_CREATE_USER),
                TafDataSources.fromCsv(TerminateSessionsScenario.TERMINATE_SESSIONS_CSV),
                TafDataSources.fromCsv(UserAccessDeniedScenario.USER_TO_CHECK_PROFILE_CSV),
                TafDataSources.fromCsv(UserListScenario.USERS_CSV),
                TafDataSources.fromCsv(UserProfileScenario.USER_TO_CHECK_PROFILE_CSV)
            )));


        final TestScenario scenario = scenario("Create test target groups and roles")
                .addFlow(gimCleanupFlows.cleanUp(USER, ROLE, TARGET_GROUP))
                .addFlow(roleManagementTestFlows.createRole())
                .addFlow(targetGroupManagementTestFlows.createTargetGroup())
                .withExceptionHandler(ScenarioExceptionHandler.PROPAGATE)
                .build();

        start(scenario);
    }

    @AfterSuite(groups = {"RFA", "RFA250"},alwaysRun = true)
    public void tearDown() {
        context.addDataSource(TARGET_GROUP_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(TARGET_GROUPS_CSV)));
        context.addDataSource(ROLE_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(ROLES_DELETE_CSV)));

        final TestScenario scenario = scenario("Delete test target groups and roles")
                .addFlow(gimCleanupFlows.cleanUp(ROLE, TARGET_GROUP))
                .withExceptionHandler(ScenarioExceptionHandler.PROPAGATE)
                .build();

        start(scenario);
    }
}
