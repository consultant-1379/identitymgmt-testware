/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.nms.security.test.usermanagement.scenario;


import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.cifwk.taf.testng.CompositeTestNGListener;
import com.ericsson.nms.security.test.CommonUtils;
import com.ericsson.nms.security.test.predicate.UserPredicates;
import com.ericsson.nms.security.test.usermanagement.LoggerMessageSet;
import com.ericsson.nms.security.test.usermanagement.TestReportObject;
import com.ericsson.oss.testware.enmbase.data.CommonDataSources;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutRestFlows;
import com.ericsson.oss.testware.security.gic.flows.PasswordSettingsFlows;
import com.ericsson.oss.testware.security.gim.data.UserMapper;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType;
import com.ericsson.oss.testware.security.gim.flows.RoleManagementTestFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

/**
 * <pre>
 * Class Name: AgatSetupAndTeardownScenario
 * Description: This class contain methods for setting up (and for tearing down) test environment for AGAT test case execution.
 * </pre>
 */
public class AgatSetupAndTeardownScenario extends TafTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgatSetupAndTeardownScenario.class);

    @Inject
    private TestContext context;
    @Inject
    private LoginLogoutRestFlows loginLogoutRestFlows;
    @Inject
    private GimCleanupFlows gimCleanupFlows;
    @Inject
    private UserManagementTestFlows userManagementTestFlows;
    @Inject
    private RoleManagementTestFlows roleManagementTestFlows;
    @Inject
    private PasswordSettingsFlows passwordSettingsFlows;

    /**
     * <pre>
     * Name: onBeforeSuite()            [public]
     * Description: This method is used to setup testware environment for AGAT test cases. This mean:
     *                  Get DataSource from provider;
     *                  Clean up Users
     *                  Clean up Roles
     *                  Clean up Target Groups;
     *                  Create initial user with SECURITY_ADMIN role.
     *              Some further information are getted from suite (XML) file.
     * </pre>
     *
     * @param userProvidedName
     *            Name of provided Datasource for Users (optional)
     * @param parallelFlows
     *            Flag to set parallel/non parallel test execution.
     * @param iTestContext
     *            TestNg interface
     * @param createUserParallel
     *            indicate if the user is created in parallel mode with others
     */
    @Parameters({"userProvidedName", "parallelFlows", "createuserParallelFlows"})
    @BeforeSuite(groups = {"ENM_EXTERNAL_TESTWARE", "AGAT_BUILD_ISO"})
    public void onBeforeSuite(final ITestContext iTestContext, @Optional() final String userProvidedName, @Optional() final String parallelFlows,
            @Optional("false") final String createUserParallel) {
        // Adding Listner for report
        CompositeTestNGListener.addListener(new TestReportObject(), 1);

        // Print in LOG output some informations related to TestSuite execution
        LOGGER.info(LoggerMessageSet.getSection(String.format("Executing Setup (%s)", getClass().getSimpleName())));
        context.setAttribute("parallelExecution",
                (parallelFlows == null || parallelFlows.isEmpty() || "false".equalsIgnoreCase(parallelFlows)) ? false : true);
        context.setAttribute("createUserInParallel", "TRUE".contentEquals(createUserParallel.toUpperCase()) ? true : false);
        LOGGER.debug("Get parameter from XML file (Parallel Flow): {} - Attribute Set: parallelExecution -> {}",
                parallelFlows, context.getAttribute("parallelExecution"));
        LOGGER.debug("Get parameter from XML file (Create User in parallel): {} - Attribute Set: createUserInParallel -> {}",
                createUserParallel, context.getAttribute("createUserInParallel"));

        // Creating DataSources for testware execution ed print content
        setupDataSources(userProvidedName);
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.ROLE_TO_CREATE), "Roles To Create"));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE), "Target Group To Create"));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.USERS_TO_CREATE), "Users To Create"));

        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.ROLE_TO_CLEAN_UP), "Roles To Clean Up"));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CLEAN_UP), "Target Group To Clean Up"));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.USER_TO_CLEAN_UP), "Users To Clean Up"));

        // Preparing Setup Scenario and Execute it
        LOGGER.debug(LoggerMessageSet.getOperation("Preparing Setup Scenario..."));
        final TestScenario scenario = TestScenarios.scenario("Setup Test Environment")
                .addFlow(this.cleanUpOperation(1))
                .addFlow(this.createUserSubFlow((boolean) context.getAttribute("createUserInParallel")))
                .addFlow(TestScenarios.flow("Backup Password Settings")
                        .addSubFlow(passwordSettingsFlows.loginAndBackupPasswordSettings(true)))
                .withDefaultVusers(1)
                .build();

        LOGGER.debug(LoggerMessageSet
                .getOperation(String.format("... Executing setup scenarioScenario (Name: %s, Type: %s)", scenario.getName(), scenario.getType())));
        CommonUtils.start(scenario);
        LOGGER.trace("... Completed Setup ...");
    }

    /**
     * <pre>
     * Name: onAfterSuite()             [public]
     * Description: This method is used to teardown testware environment for AGAT test cases. This mean:
     *                  Clean up Users
     *                  Clean up Roles
     *                  Clean up Target Groups;
     *
     * </pre>
     *
     * @param iTestContext
     *            TestNg interface
     */
    @AfterSuite(alwaysRun = true, groups = {"ENM_EXTERNAL_TESTWARE", "AGAT_BUILD_ISO"})
    public void onAfterSuite(final ITestContext iTestContext) {
        // Print in LOG output some informations related to TestSuite execution
        LOGGER.info(LoggerMessageSet.getSection(String.format("Executing TearDown (%s)", getClass().getSimpleName())));

        // DataSource For CleanUp operation
        setupDataSourceForCleanUp();
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.ROLE_TO_CLEAN_UP), "Roles To Clean Up"));
        LOGGER.debug("{}",
                LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CLEAN_UP), "Target Group To Clean Up"));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.USER_TO_CLEAN_UP), "Users To Clean Up"));

        // Preparing Setup Scenario and Execute it
        LOGGER.debug(LoggerMessageSet.getOperation("CleanUp Test Scenario..."));
        final TestScenario scenario = TestScenarios.scenario("TearDown Test Environment")
                .addFlow(TestScenarios.flow("Restore Password Settings")
                        .addSubFlow(passwordSettingsFlows.loginAndRestorePasswordSettings(true)))
                .addFlow(this.cleanUpOperation(1))
                .withDefaultVusers(1)
                .build();

        LOGGER.debug(LoggerMessageSet
                .getOperation(String.format("... Executing TearDown scenarioScenario (Name: %s, Type: %s)", scenario.getName(), scenario.getType())));
        CommonUtils.start(scenario);
        LOGGER.trace("... Completed Teardown ...");
    }

    /**
     * Name: createUserSubFlow()             [private]
     * Description: Test flow for user Clean Up.
     *
     * @param vUser - number of parallel execution
     * @return - Flow Builder
     */
    private TestStepFlowBuilder cleanUpOperation(final int vUser) {
        return TestScenarios.flow("Clean Up Test Environment [flow]")
                .addSubFlow(gimCleanupFlows.cleanUp(EnmObjectType.USER)).alwaysRun()
                .split(gimCleanupFlows.cleanUp(EnmObjectType.ROLE), gimCleanupFlows.cleanUp(EnmObjectType.TARGET_GROUP)).alwaysRun()
                .withExceptionHandler(ScenarioExceptionHandler.LOGONLY)
                .withVusers(vUser);
    }

    /**
     * Name: createUserSubFlow()             [private]
     * Description: Test Flow for Create user operation. This create action is a setup operation for Tests execution: it create users that should
     * be used in Test Case execution.
     *
     * @param parallelMode - Flag to enable parallel execution
     * @return Flow Builder
     */
    private TestStepFlowBuilder createUserSubFlow(final boolean parallelMode) {
        final Predicate filterForCreation = Predicates.or(UserPredicates.adminUser, UserPredicates.securityAdmin, UserPredicates.importedUserfromXml);
        final int vUser = parallelMode
                ? Iterables.size(TafDataSources.filter(context.dataSource(CommonDataSources.USERS_TO_CREATE), filterForCreation))
                        : 1;
                LOGGER.debug("Create user in {} mode (vUser Count = {})", parallelMode ? "parallel" : "serial", vUser);
                return TestScenarios.flow("Create ENM users [flow]")
                        .beforeFlow(AgatScenarioUtility.backupDataSource(CommonDataSources.USERS_TO_CREATE, filterForCreation))
                        .afterFlow(AgatScenarioUtility.restoreDataSource(CommonDataSources.USERS_TO_CREATE))
                        .addSubFlow(loginLogoutRestFlows.loginDefaultUser())
                        .addSubFlow(userManagementTestFlows.createEnmUser())
                        .addSubFlow(loginLogoutRestFlows.logout())
                        .addSubFlow(userManagementTestFlows.verifyUserLoginWithAssert())
                        .addSubFlow(roleManagementTestFlows.verifyUserHasAssignedRoles())
                        .alwaysRun().withExceptionHandler(ScenarioExceptionHandler.PROPAGATE)
                        .withVusers(vUser);
    }

    /**
     * Name: setupDataSources()             [private]
     * Description: This method is used to create Datasources used in Test Suite execution (initial Ones).
     *
     * @param userProvidedName - Name of DataProvider datasource for {@link com.ericsson.oss.testware.enmbase.data.CommonDataSources#USERS_TO_CREATE}
     */
    private void setupDataSources(final String userProvidedName) {
        LOGGER.info(LoggerMessageSet.getOperation(String.format("Setup DataSources (%s)", getClass().getSimpleName())));

        // Getting Profile used for test execution
        final String profile = DataHandler.getConfiguration().getProperty("taf.profiles", "", String.class);
        LOGGER.debug(LoggerMessageSet.getOperation("Getting profile..."
                + LoggerMessageSet.getLF() + String.format("... profile selected: <%s>", profile)));

        // Getting DataSource 'ROLE_TO_CREATE
        LOGGER.debug(
                LoggerMessageSet.getOperation(
                        String.format("... getting DataDource <%s> from DataProvider and put in Context", CommonDataSources.ROLE_TO_CREATE)));
        context.addDataSource(CommonDataSources.ROLE_TO_CREATE, TafDataSources.fromTafDataProvider(CommonDataSources.ROLE_TO_CREATE));
        Preconditions.checkArgument(context.dataSource(CommonDataSources.ROLE_TO_CREATE).iterator().hasNext(),
                "No Data in Data Source (ROLE_TO_CREATE)");

        // Getting DataSource 'TARGET_GROUP_TO_CREATE
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("... getting DataDource <%s> from DataProvider and put in Context",
                CommonDataSources.TARGET_GROUP_TO_CREATE)));
        context.addDataSource(CommonDataSources.TARGET_GROUP_TO_CREATE, TafDataSources.fromTafDataProvider(CommonDataSources.TARGET_GROUP_TO_CREATE));
        Preconditions.checkArgument(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE).iterator().hasNext(),
                "No Data in Data Source (TARGET_GROUP_TO_CREATE)");

        // Getting DataSource 'USERS_TO_CREATE
        final String userProvideName = (userProvidedName == null || userProvidedName.isEmpty()) ? CommonDataSources.USERS_TO_CREATE
                : userProvidedName;
        LOGGER.debug(LoggerMessageSet.getOperation(
                String.format("... getting DataDource <%s> from DataProvider (%s) and put in Context", CommonDataSources.USERS_TO_CREATE,
                        userProvideName)));
        context.addDataSource(CommonDataSources.USERS_TO_CREATE,
                TafDataSources.copy(TafDataSources.fromTafDataProvider(userProvideName, UserMapper.class)));
        Preconditions.checkArgument(context.dataSource(CommonDataSources.USERS_TO_CREATE).iterator().hasNext(),
                "No Data in Data Source (USERS_TO_CREATE)");

        // DataSource For CleanUp operation
        setupDataSourceForCleanUp();
    }

    private void setupDataSourceForCleanUp() {
        // Setup DataSources for Cleanup Operation
        LOGGER.debug(LoggerMessageSet.getOperation(
                String.format("Setup DataSources for clean up operation: " + LoggerMessageSet.getLF()
                + "  - <%s> ->> <%s>" + LoggerMessageSet.getLF()
                + "  - <%s> ->> <%s>" + LoggerMessageSet.getLF()
                + "  - <%s> ->> <%s>" + LoggerMessageSet.getLF(),
                CommonDataSources.ROLE_TO_CREATE, CommonDataSources.ROLE_TO_CLEAN_UP,
                CommonDataSources.TARGET_GROUP_TO_CREATE, CommonDataSources.TARGET_GROUP_TO_CLEAN_UP, CommonDataSources.USERS_TO_CREATE,
                CommonDataSources.USER_TO_CLEAN_UP)));
        context.addDataSource(CommonDataSources.ROLE_TO_CLEAN_UP, TafDataSources.shared(context.dataSource(CommonDataSources.ROLE_TO_CREATE)));
        Preconditions.checkArgument(context.dataSource(CommonDataSources.ROLE_TO_CLEAN_UP).iterator().hasNext(),
                "No Data in Data Source (ROLE_TO_CLEAN_UP)");
        context.addDataSource(CommonDataSources.TARGET_GROUP_TO_CLEAN_UP,
                TafDataSources.shared(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE)));
        Preconditions.checkArgument(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CLEAN_UP).iterator().hasNext(),
                "No Data in Data Source (TARGET_GROUP_TO_CLEAN_UP)");
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP,
                TafDataSources.shared(TafDataSources.combine(context.dataSource(CommonDataSources.USERS_TO_CREATE),
                        context.dataSource(CommonDataSources.AVAILABLE_USERS),
                        context.dataSource(AgatTestCasesScenario.USERS_TO_IMPORT),
                        TafDataSources.fromTafDataProvider(AgatTestCasesScenario.NEW_USERS_TO_CREATE))));
        Preconditions.checkArgument(context.dataSource(CommonDataSources.USER_TO_CLEAN_UP).iterator().hasNext(),
                "No Data in Data Source (USER_TO_CLEAN_UP)");
    }

}
