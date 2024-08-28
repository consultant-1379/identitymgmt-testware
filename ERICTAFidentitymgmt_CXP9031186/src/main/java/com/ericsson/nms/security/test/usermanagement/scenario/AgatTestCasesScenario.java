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

import static org.assertj.core.api.Assertions.assertThat;

import static com.google.common.primitives.Ints.max;
import static com.google.common.primitives.Ints.min;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestOptions;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordModifier;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.nms.security.test.CommonUtils;
import com.ericsson.nms.security.test.predicate.UserPredicates;
import com.ericsson.nms.security.test.usermanagement.AgatCommonUtility;
import com.ericsson.nms.security.test.usermanagement.LoggerMessageSet;
import com.ericsson.nms.security.test.usermanagement.flows.AgatUserManagementTestFlow;
import com.ericsson.nms.security.test.usermanagement.teststep.AgatUserManagementTestStep;
import com.ericsson.nms.security.test.usermanagement.xmlmanager.ImportDatasourceParser;
import com.ericsson.oss.testware.enmbase.data.CommonDataSources;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutRestFlows;
import com.ericsson.oss.testware.security.gic.flows.PasswordSettingsFlows;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType;
import com.ericsson.oss.testware.security.gim.flows.ImportExportFlows;
import com.ericsson.oss.testware.security.gim.flows.RoleManagementTestFlows;
import com.ericsson.oss.testware.security.gim.flows.TargetGroupManagementTestFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.ericsson.oss.testware.security.gim.steps.UserManagementTestSteps;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * <pre>
 * Class Name: AgatTestCasesScenario
 * Description: This class contain Test Cases for AGAT test execution.
 * </pre>
 **/
public class AgatTestCasesScenario extends TafTestBase {
    protected static final String USERS_TO_IMPORT = "usersToImpurtDataSource";
    protected static final String NEW_USERS_TO_CREATE = "usersNewToImportDataSource";
    private static final Logger LOGGER = LoggerFactory.getLogger(AgatTestCasesScenario.class);
    private static final String NOT_SUPPORTED = DataHandler.getConfiguration().getProperty("notSupported.name", "NOT APPLICABLE", String.class);
    private static final int MAX_DEFUSER = DataHandler.getConfiguration().getProperty("max.defaultUser.count", 10, Integer.class);
    private static final TestInfo testetCases = new TestInfo();
    private static final String SPLIT_CHR = " -> ";
    // Local Datasource Name
    private static final String INPUT_DATASOURCE = "InputDataSource";
    private static final String USER_INFO_UPDATE = "userInfoUpdateDataSource";
    private static final String USER_INFO_AGEING_UPDATE = "userAgeingInfoUpdateDataSource";
    private static final String USER_PASSWORD_UPDATE = "userPasswordUpdateDataSource";
    private static final String USER_XML_FILE_TO_IMPORT = "fileXmlToUploadForUserImport";
    private static final String UPDATE_USERS_TO_CREATE = "usersToUpdateForImportDataSource";
    private static final String NEW_USERS_TO_CHECK = "newUsersFromXmlFileToCheck";

    private static Boolean passwordDataSourceExistence = false;

    @Inject
    private AgatUserManagementTestStep agatUserManagementTestStep;
    @Inject
    private AgatUserManagementTestFlow agatUserManagementTestFlow;
    @Inject
    private TestContext context;
    @Inject
    private LoginLogoutRestFlows loginLogoutRestFlows;
    @Inject
    private UserManagementTestFlows userManagementTestFlows;
    @Inject
    private RoleManagementTestFlows roleManagementTestFlows;
    @Inject
    private TargetGroupManagementTestFlows targetGroupManagementTestFlows;
    @Inject
    private ImportExportFlows importExportFlows;
    @Inject
    private PasswordSettingsFlows passwordSettingsFlows;
    @Inject
    private GimCleanupFlows gimCleanupFlows;

    /***************************************************************************************************************.
     /*                         Setup and TearDown operation for this test class                                    .
     /**************************************************************************************************************.

     /**
     * <pre>
     * Name: testClassSetup()
     * Description: Specific SetUp operation for this TestCase Class.
     * </pre>
     **/
    @BeforeClass(groups = { "ENM_EXTERNAL_TESTWARE" })
    public void testClassSetup() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOGGER.info(
                LoggerMessageSet.getSection(String.format("Executing Specific Setup: <%s> from <%s> class", methodName, getClass().getSimpleName())));
        LOGGER.debug(LoggerMessageSet.getOperation("Checking DataSources existence for TestCases in this class..."));

        // Checking DataSource Existence and copy it din Delete DataSource (ROLE_TO_CREATE -> ROLE_TO_DELETE)
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("DataSource '%s' presence '%s'", CommonDataSources.ROLE_TO_CREATE,
                context.doesDataSourceExist(CommonDataSources.ROLE_TO_CREATE))));
        Preconditions
                .checkArgument(context.dataSource(CommonDataSources.ROLE_TO_CREATE).iterator().hasNext(), "No Data in Data Source (ROLE_TO_CREATE)");
        context.addDataSource(CommonDataSources.ROLE_TO_DELETE, context.dataSource(CommonDataSources.ROLE_TO_CREATE));

        // Checking DataSource Existence and copy it din Delete DataSource (TARGET_GROUP_TO_CREATE -> TARGET_GROUP_TO_DELETE)
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("DataSource '%s' presence '%s'", CommonDataSources.TARGET_GROUP_TO_CREATE,
                context.doesDataSourceExist(CommonDataSources.TARGET_GROUP_TO_CREATE))));
        Preconditions.checkArgument(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE).iterator().hasNext(),
                "No Data in Data Source (TARGET_GROUP_TO_CREATE)");
        context.addDataSource(CommonDataSources.TARGET_GROUP_TO_DELETE, context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE));

        // Checking existence of others DataSources used in Test suite.
        context.addDataSource(USER_INFO_UPDATE, TafDataSources.fromTafDataProvider(USER_INFO_UPDATE));
        LOGGER.debug(LoggerMessageSet
                .getOperation(String.format("DataSource '%s' presence '%s'", USER_INFO_UPDATE, context.doesDataSourceExist(USER_INFO_UPDATE))));
        Preconditions.checkArgument(context.dataSource(USER_INFO_UPDATE).iterator().hasNext(), "No Data in Data Source (USER_INFO_UPDATE)");
        context.addDataSource(USER_INFO_AGEING_UPDATE, TafDataSources.fromTafDataProvider(USER_INFO_AGEING_UPDATE));
        LOGGER.debug(LoggerMessageSet.getOperation(
                String.format("DataSource '%s' presence '%s'", USER_INFO_AGEING_UPDATE, context.doesDataSourceExist(USER_INFO_AGEING_UPDATE))));
        Preconditions
                .checkArgument(context.dataSource(USER_INFO_AGEING_UPDATE).iterator().hasNext(), "No Data in Data Source (USER_INFO_AGEING_UPDATE)");
        if (DataHandler.getConfiguration().getProperty("dataprovider." + USER_PASSWORD_UPDATE + ".type") != null) {
            context.addDataSource(USER_PASSWORD_UPDATE, TafDataSources.fromTafDataProvider(USER_PASSWORD_UPDATE));
            LOGGER.debug(LoggerMessageSet.getOperation(
                    String.format("DataSource '%s' presence '%s'", USER_PASSWORD_UPDATE, context.doesDataSourceExist(USER_PASSWORD_UPDATE))));
            passwordDataSourceExistence = context.dataSource(USER_PASSWORD_UPDATE).iterator().hasNext();
        }

        // Checking DataSource Existence and increment Users for Parallel Execution (agatAdm_AgatDefaultUser and agatAdm_AgatAgeingUser)
        Preconditions.checkArgument(context.dataSource(CommonDataSources.USERS_TO_CREATE).iterator().hasNext(),
                "No Data in Data Source (USERS_TO_CREATE)");
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.USERS_TO_CREATE), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source (USERS_TO_CREATE) filtered with 'securityAdmin' predicate");
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.USERS_TO_CREATE), UserPredicates.genericUser).iterator().hasNext(),
                "No Data in Data Source (USERS_TO_CREATE) filtered with 'genericUser' predicate");
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.USERS_TO_CREATE), UserPredicates.ageingUser).iterator().hasNext(),
                "No Data in Data Source (USERS_TO_CREATE) filtered with 'ageingUser' information");
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.USERS_TO_CREATE), UserPredicates.importedUserfromXml).iterator().hasNext(),
                "No Data in Data Source (USERS_TO_CREATE) filtered with 'importedUserfromXml' information");

        // Get Datarecord count for DataSources usd by 'generic user':
        final int maxDataRecordForGenericUser = max(Iterators.size(context.dataSource(CommonDataSources.ROLE_TO_CREATE).iterator()),
                Iterators.size(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE).iterator()),
                Iterators.size(context.dataSource(USER_INFO_UPDATE).iterator()),
                passwordDataSourceExistence ? Iterators.size(context.dataSource(USER_PASSWORD_UPDATE).iterator()) : 1);
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.shared(AgatScenarioUtility
                .replaceAndReplicateRecords(context.dataSource(CommonDataSources.USERS_TO_CREATE), UserPredicates.genericUser,
                        maxDataRecordForGenericUser)));

        // Get Datarecord count for DataSources usd by 'ageing user':
        final int maxDataRecordForAgeingUser = Iterators.size(context.dataSource(USER_INFO_AGEING_UPDATE).iterator());
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.shared(AgatScenarioUtility
                .replaceAndReplicateRecords(context.dataSource(CommonDataSources.USERS_TO_CREATE), UserPredicates.ageingUser,
                        maxDataRecordForAgeingUser)));

        // Print complete list of USERS_TO_CREATE ...
        LOGGER.debug("{}",
                LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.USERS_TO_CREATE), "User To create Datasource (complete)"));

        // DataSource to create XML file for {@link com.ericsson.nms.security.test.usermanagement.scenario.AgatTestCasesScenario#importUsersFromXml}
        context.addDataSource(USERS_TO_IMPORT, updateFieldValueFromCsv(updatePasswordFromCustomer(TafDataSources
                .combine(TafDataSources.filter(context.dataSource(CommonDataSources.USERS_TO_CREATE), UserPredicates.importedUserfromXml),
                        TafDataSources.fromTafDataProvider(NEW_USERS_TO_CREATE))), TafDataSources.fromTafDataProvider(UPDATE_USERS_TO_CREATE)));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(USERS_TO_IMPORT), "Users To Import for XML file Creation"));
        LOGGER.info(LoggerMessageSet.getOperation(String.format("Creating XML file from %s dataSource", USERS_TO_IMPORT)));
        context.addDataSource(USER_XML_FILE_TO_IMPORT, createAndSaveXmlFile(context.dataSource(USERS_TO_IMPORT)));

        // Datasources for {@link com.ericsson.nms.security.test.usermanagement.scenario.AgatTestCasesScenario#configureLoginLock}
        context.addDataSource(PasswordSettingsFlows.MODIFY_PASSWORDOBJECT_DATASOURCE,
                TafDataSources.fromTafDataProvider(PasswordSettingsFlows.MODIFY_PASSWORDOBJECT_DATASOURCE));
        context.addDataSource(AgatUserManagementTestFlow.LOCKOUT_SEQUENCE_DATASOURCE,
                TafDataSources.copy(TafDataSources.fromTafDataProvider(AgatUserManagementTestFlow.LOCKOUT_SEQUENCE_DATASOURCE)));

        // Preparing Setup Scenario and Execute it
        // TORF-345026 - Not deleted all Users Before scenario Run.
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP, TafDataSources.shared(AgatScenarioUtility
                .getSingleDataRecords(TafDataSources.copy(context.dataSource(CommonDataSources.USERS_TO_CREATE)),
                        TafDataSources.copy(context.dataSource(CommonDataSources.AVAILABLE_USERS)), "username")));
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP, TafDataSources.shared(AgatScenarioUtility
                .getSingleDataRecords(TafDataSources.copy(context.dataSource(CommonDataSources.USER_TO_CLEAN_UP)),
                        TafDataSources.copy(context.dataSource(USERS_TO_IMPORT)), "username")));

        final int vUsers = Iterators.size(context.dataSource(CommonDataSources.USER_TO_CLEAN_UP).iterator());
        final int vUsersEffective = MAX_DEFUSER < vUsers ? vUsers : MAX_DEFUSER;
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Cleanup effective UserList (%s/%s)", vUsers, vUsersEffective)));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.USER_TO_CLEAN_UP), "Users To Clean Up Again"));
        final TestScenario scenario = TestScenarios.scenario("Setup Test Environment (Remove Users)")
                .addFlow(gimCleanupFlows.cleanUp(EnmObjectType.USER)).withExceptionHandler(ScenarioExceptionHandler.LOGONLY)
                .withDefaultVusers(vUsersEffective).build();
        CommonUtils.start(scenario);
        LOGGER.info("TDM property '{}' is {}", "tdm.api.host", DataHandler.getConfiguration().getProperty("tdm.api.host", "", String.class));
        LOGGER.trace("... Completed Class Setup ...");
    }

    /**
     * <pre>
     * Name: testClassTearDown()
     * Description: Specific TearDown operation for this TestCase class.
     * </pre>
     *
     * @param iTestContext
     *         TestNg interface
     **/
    @AfterClass(groups = { "ENM_EXTERNAL_TESTWARE" })
    public void testClassTearDown(final ITestContext iTestContext) {
        // Search and delete created import file
        final String importXmlFile = context.dataSource(USER_XML_FILE_TO_IMPORT).iterator().next().getFieldValue("fileName");
        final List<String> files = FileFinder.findFile(importXmlFile);

        if (!files.isEmpty()) {
            final String filePath = files.get(0);
            LOGGER.info(LoggerMessageSet.getOperation(String.format("Deleting Created XML file (import Test Case): %s.", filePath)));
            final File fileToDelete = new File(filePath);
            fileToDelete.delete();
            LOGGER.debug("*** File Deleted ***");
        }
    }

    /**
     * <pre>
     * <B>Method Name</B>: createRoles
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_CREATEROLE}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_CREATEROLE}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: Demonstrate that a user with SECURITY_ADMIN role is able to create a role via CLI interface.
     *                     (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.12)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void createRoles() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source " + "(AVAILABLE_USERS) filtered with 'securityAdmin' predicate");
        Preconditions
                .checkArgument(context.dataSource(CommonDataSources.ROLE_TO_CREATE).iterator().hasNext(), "No Data in Data Source (ROLE_TO_CREATE)");

        // Print DataSource to be use in this Test Case
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.ROLE_TO_CREATE), "Roles To Create"));
        final int usersCountFromDataSource = Iterables.size(context.dataSource(CommonDataSources.ROLE_TO_CREATE));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), TafDataSources
                .shared(TafDataSources.merge(TafDataSources
                                .cyclic(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin)),
                        context.dataSource(CommonDataSources.ROLE_TO_CREATE))));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Execute Test '%s' with <%s> vUsers...", methodName, vUsersDataDrivenScenario)));
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Create Roles (Scenario)").addFlow(loginLogoutRestFlows.loginBuilder())
                .addFlow(roleManagementTestFlows.createRoleBasic()).addFlow(loginLogoutRestFlows.logoutBuilder()).doParallel(vUsersDataDrivenScenario)
                .withScenarioDataSources(
                        TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS).bindTo(CommonDataSources.ROLE_TO_CREATE))
                .withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);
    }

    /**
     * <pre>
     * <B>Method Name</B>: deleteRoles
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_DELETEROLE}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_DELETEROLE}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: Demonstrate that a user with SECURITY_ADMIN role is able to delete a role via CLI interface.
     *                      (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.21)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void deleteRoles() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source " + "(AVAILABLE_USERS) filtered with 'securityAdmin' predicate");
        Preconditions
                .checkArgument(context.dataSource(CommonDataSources.ROLE_TO_DELETE).iterator().hasNext(), "No Data in Data Source (ROLE_TO_DELETE)");

        // Print DataSource to be use in this Test Case
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.ROLE_TO_DELETE), "Roles To Delete"));
        final int usersCountFromDataSource = Iterables.size(context.dataSource(CommonDataSources.ROLE_TO_DELETE));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), TafDataSources
                .shared(TafDataSources.merge(TafDataSources
                                .cyclic(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin)),
                        context.dataSource(CommonDataSources.ROLE_TO_DELETE))));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Execute Test '%s' with <%s> vUsers...", methodName, vUsersDataDrivenScenario)));
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Delete Roles (Scenario)").addFlow(loginLogoutRestFlows.loginBuilder())
                .addFlow(roleManagementTestFlows.deleteRoleBasic()).addFlow(loginLogoutRestFlows.loginBuilder()).doParallel(vUsersDataDrivenScenario)
                .withScenarioDataSources(
                        TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS).bindTo(CommonDataSources.ROLE_TO_DELETE))
                .withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);
    }

    /**
     * <B>Method Name</B>: createTargetGroups <B>Test Case ID</B>:
     * {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_CREATETARGETGROUP} Test Name:
     * {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_CREATETARGETGROUP} <B>Test Group</B>:
     * ENM_EXTERNAL_TESTWARE <B>Description</B>: Demonstrate that a user with SECURITY_ADMIN role is able to create a target Group via CLI interface.
     * (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph: 3.13)
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void createTargetGroups() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source " + "(AVAILABLE_USERS) filtered with 'securityAdmin' predicate");
        Preconditions.checkArgument(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE).iterator().hasNext(),
                "No Data in Data Source (TARGET_GROUP_TO_CREATE)");

        // Print DataSource to be use in this Test Case
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE), "Target Group To Create"));
        final int usersCountFromDataSource = Iterables.size(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), TafDataSources
                .shared(TafDataSources.merge(TafDataSources
                                .cyclic(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin)),
                        context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE))));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Execute Test '%s' with <%s> vUsers...", methodName, vUsersDataDrivenScenario)));

        final TestScenario scenario = TestScenarios.dataDrivenScenario("Create Target Groups (Scenario)").addFlow(loginLogoutRestFlows.loginBuilder())
                .addFlow(targetGroupManagementTestFlows.createTargetGroupBasic()).addFlow(loginLogoutRestFlows.logoutBuilder())
                .doParallel(vUsersDataDrivenScenario).withScenarioDataSources(
                        TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS)
                                .bindTo(CommonDataSources.TARGET_GROUP_TO_CREATE)).withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);
    }

    /**
     * <B>Method Name</B>: deleteTargetGroups <B>Test Case ID</B>:
     * {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_DELETETARGETGROUP} Test Name:
     * {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_DELETETARGETGROUP} <B>Test Group</B>:
     * ENM_EXTERNAL_TESTWARE <B>Description</B>: Demonstrate that a user with SECURITY_ADMIN role is able to delete a target Group via CLI interface.
     * (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph: 3.17)
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void deleteTargetGroups() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source " + "(AVAILABLE_USERS) filtered with 'securityAdmin' predicate");
        Preconditions.checkArgument(context.dataSource(CommonDataSources.TARGET_GROUP_TO_DELETE).iterator().hasNext(),
                "No Data in Data Source (TARGET_GROUP_TO_DELETE)");

        // Print DataSource to be use in this Test Case
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.TARGET_GROUP_TO_DELETE), "Target Group To Delete"));
        final int usersCountFromDataSource = Iterables.size(context.dataSource(CommonDataSources.TARGET_GROUP_TO_DELETE));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), TafDataSources
                .shared(TafDataSources.merge(TafDataSources
                                .cyclic(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin)),
                        context.dataSource(CommonDataSources.TARGET_GROUP_TO_DELETE))));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Execute Test '%s' with <%s> vUsers...", methodName, vUsersDataDrivenScenario)));
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Delete Target Groups (Scenario)").addFlow(loginLogoutRestFlows.loginBuilder())
                .addFlow(targetGroupManagementTestFlows.deleteTargetGroupBasic()).addFlow(loginLogoutRestFlows.logoutBuilder())
                .doParallel(vUsersDataDrivenScenario).withScenarioDataSources(
                        TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS)
                                .bindTo(CommonDataSources.TARGET_GROUP_TO_DELETE)).withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);
    }

    /**
     * <pre>
     * <B>Method Name</B>: createUser
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_CREATEUSER}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_CREATEUSER}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: This test case should demonstrate that a user with SECURITY_ADMIN role is able to create a
     *                     user via CLI interface.
     *                     (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.4)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void createUser() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source " + "(AVAILABLE_USERS) filtered with 'securityAdmin' predicate");
        Preconditions.checkArgument(context.dataSource(CommonDataSources.USERS_TO_CREATE).iterator().hasNext(),
                "No Data in Data Source (USERS_TO_CREATE)");

        // Print DataSource to be use in this Test Case
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        final TestDataSource<DataRecord> userToCreate = TafDataSources
                .shared(TafDataSources.filter(context.dataSource(CommonDataSources.USERS_TO_CREATE), UserPredicates.genericUser));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(userToCreate, "Users to Create (Filtered)"));
        final int usersCountFromDataSource = Iterables.size(userToCreate);
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), AgatScenarioUtility
                .replicateDataRecord(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin, usersCountFromDataSource));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Execute Test '%s' with <%s> vUsers...", methodName, vUsersDataDrivenScenario)));
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Create User (Scenario)")
                .addFlow(loginLogoutRestFlows.loginBuilder().beforeFlow(AgatScenarioUtility.backupDataSource(CommonDataSources.AVAILABLE_USERS)))
                .addFlow(userManagementTestFlows.createEnmUser()
                        .beforeFlow(AgatScenarioUtility.backupAndReplace(CommonDataSources.USERS_TO_CREATE, userToCreate), AgatScenarioUtility
                                .removeDataSources(CommonDataSources.AVAILABLE_USERS, UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE))
                        .afterFlow(AgatScenarioUtility.restoreDataSource(CommonDataSources.USERS_TO_CREATE)))
                .addFlow(loginLogoutRestFlows.logoutBuilder().afterFlow(AgatScenarioUtility.restoreDataSource(CommonDataSources.AVAILABLE_USERS)))
                .addFlow(AgatCommonUtility.waitTime(5)) // Wait Time requested by User Security Teams for Dbase Aligment
                .doParallel(vUsersDataDrivenScenario)
                .withScenarioDataSources(TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS),
                        TestScenarios.dataSource(CommonDataSources.USERS_TO_CREATE)).withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);

        // Checking Created DataSource Existence
        LOGGER.debug(LoggerMessageSet.getOperation("Checking created DataSources..."));
        assertThat(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE).iterator().hasNext())
                .as("DataSource 'USERS_TO_VERIFY_DATASOURCE' was not created");
        final int createUserSize = Iterables.size(userToCreate);
        final int verifyUserSize = Iterables.size(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE));
        LOGGER.debug("{}", AgatCommonUtility.printDataSource(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE),
                UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE));
        LOGGER.debug("Checking DataSource size: 'USERS_TO_CREATE' (filtered) [{}], 'USERS_TO_VERIFY_DATASOURCE' [{}]", createUserSize,
                verifyUserSize);
        assertThat(createUserSize).isEqualTo(verifyUserSize)
                .as("DataSource 'USERS_TO_CREATE' (filtered) has not same size as 'USERS_TO_VERIFY_DATASOURCE'");
    }

    /**
     * <pre>
     * <B>Method Name</B>: checkUserCanLogin
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_USERCANLOGIN}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_USERCANLOGIN}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: Confirm that a user created with User Management can login to ENM via REST interface.
     *              (Show how the user is prompt to change this password the first time he logins to ENM [TODO])
     *                     (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.8)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void checkUserCanLogin() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE).iterator().hasNext(),
                "No Data in Data Source (USERS_TO_VERIFY_DATASOURCE)");

        // Print DataSource to be use in this Test Case
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        LOGGER.debug("{}",
                LoggerMessageSet.printDataSource(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE), "Users to verify"));
        final int usersCountFromDataSource = Iterables.size(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName),
                TafDataSources.shared(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE)));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Execute Test '%s' with <%s> vUsers...", methodName, vUsersDataDrivenScenario)));
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Verify User can Login (Scenario)").addFlow(
                userManagementTestFlows.verifyUserLoginWithAssert()
                        .beforeFlow(AgatScenarioUtility.backupDataSource(CommonDataSources.AVAILABLE_USERS),
                                AgatScenarioUtility.removeDataSources(CommonDataSources.AVAILABLE_USERS))
                        .afterFlow(AgatScenarioUtility.restoreCombinedDataSource(CommonDataSources.AVAILABLE_USERS)))
                .doParallel(vUsersDataDrivenScenario)
                .withScenarioDataSources(TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE))
                .withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);

        // Checking Created DataSource Existence
        LOGGER.debug("Checking Created DataSources");
        assertThat(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser).iterator().hasNext())
                .as("DataSource 'AVAILABLE_USERS' (Filtered - genericUser) was empty");
        final int verifyUserSize = Iterables
                .size(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser));
        LOGGER.debug("{}", AgatCommonUtility
                .printDataSource(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser),
                        "Available User (Filtered - genericUser)"));
        LOGGER.debug("Checking DataSource size: 'USERS_TO_VERIFY_DATASOURCE' (filtered) [{}], 'AVAILABLE_USERS' (Filtered - userPredicate) [{}]",
                usersCountFromDataSource, verifyUserSize);
        assertThat(usersCountFromDataSource).isEqualTo(verifyUserSize)
                .as("DataSource 'AVAILABLE_USERS' (Filtered - userPredicate) has not same size as 'USERS_TO_VERIFY_DATASOURCE'");
        context.removeDataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE);
        LOGGER.debug("{}", AgatCommonUtility.printDataSource(context.dataSource(CommonDataSources.AVAILABLE_USERS), "Available User"));
    }

    /**
     * <pre>
     * <B>Method Name</B>: deleteUsers
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_DELETEUSER}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_DELETEUSER}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: Show how a single or multiple users can be deleted from the system via CLI interface.
     *                     (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.22)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void deleteUsers() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source " + "(AVAILABLE_USERS) filtered with 'securityAdmin' predicate");
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser).iterator().hasNext(),
                "No Data in Data Source (USERS_TO_DELETE) filtered with 'genericUser' predicate");

        // Print DataSource to be use in this Test Case
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        context.addDataSource(CommonDataSources.USERS_TO_DELETE,
                TafDataSources.shared(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser)));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.USERS_TO_DELETE), "Users to Create (Filtered)"));
        final int usersCountFromDataSource = Iterables.size(context.dataSource(CommonDataSources.USERS_TO_DELETE));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), AgatScenarioUtility
                .replicateDataRecord(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin, usersCountFromDataSource));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Execute Test '%s' with <%s> vUsers...", methodName, vUsersDataDrivenScenario)));
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Delete User (Scenario)")
                .addFlow(loginLogoutRestFlows.loginBuilder().beforeFlow(AgatScenarioUtility.backupDataSource(CommonDataSources.AVAILABLE_USERS)))
                .addFlow(userManagementTestFlows.deleteEnmUser().beforeFlow(AgatScenarioUtility
                        .splitDataSourceContent(CommonDataSources.AVAILABLE_USERS + "_TEMP", CommonDataSources.USERS_TO_DELETE,
                                CommonDataSources.AVAILABLE_USERS + "_TEMP", UserPredicates.genericUser)))
                .addFlow(loginLogoutRestFlows.logoutBuilder().afterFlow(AgatScenarioUtility.restoreDataSource(CommonDataSources.AVAILABLE_USERS)))
                .doParallel(vUsersDataDrivenScenario)
                .withScenarioDataSources(TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS),
                        TestScenarios.dataSource(CommonDataSources.USERS_TO_DELETE)).withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);
    }

    /**
     * <pre>
     * <B>Method Name</B>: assignRoleAndTargeGroupToUser
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_ASSIGN_TARGETGROUP_ROLE}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_ASSIGN_TARGETGROUP_ROLE}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: Demonstrate that a user with SECURITY_ADMIN role is able to assign a Role and Target Group via CLI interface.
     *                      (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.14)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void assignRoleAndTargeGroupToUser() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source " + "(AVAILABLE_USERS) filtered with 'securityAdmin' predicate");
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser).iterator().hasNext(),
                "No Data in Data Source " + "(AVAILABLE_USERS) filtered with 'genericUser' predicate");
        Preconditions.checkArgument(context.dataSource(CommonDataSources.ROLE_TO_CREATE).iterator().hasNext(),
                "No Data in Data Source " + "(ROLE_TO_CREATE)");
        Preconditions.checkArgument(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE).iterator().hasNext(),
                "No Data in Data Source " + "(TARGET_GROUP_TO_CREATE)");

        // Print DataSource to be use in this Test Case
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        AgatScenarioUtility.removeDataSources(CommonDataSources.USERS_TO_UPDATE, UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE,
                UserManagementTestSteps.MODIFIED_AVAILABLE_USERS);
        final TestDataSource<DataRecord> genericOperator = TafDataSources
                .filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser);
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(genericOperator, "Available Users (Filtered)"));

        context.addDataSource(CommonDataSources.USERS_TO_UPDATE,
                mergingDataField(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser),
                        context.dataSource(CommonDataSources.ROLE_TO_CREATE), "name" + SPLIT_CHR + "roleName"));
        context.addDataSource(CommonDataSources.USERS_TO_UPDATE, TafDataSources
                .shared(mergingDataField(context.dataSource(CommonDataSources.USERS_TO_UPDATE),
                        context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE), "targetGroupName")));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.USERS_TO_UPDATE), "User Updating DataSource"));
        final int usersCountFromDataSource = Iterables.size(context.dataSource(CommonDataSources.USERS_TO_UPDATE));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), AgatScenarioUtility
                .replicateDataRecord(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin, usersCountFromDataSource));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Execute Test '%s' with <%s> vUsers...", methodName, vUsersDataDrivenScenario)));
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Assign Roles and Target Group to Users (Scenario)")
                .addFlow(loginLogoutRestFlows.loginBuilder()).addFlow(userManagementTestFlows.updateEnmUserRoleAndTargetGroup())
                .addFlow(loginLogoutRestFlows.logoutBuilder()).doParallel(vUsersDataDrivenScenario)
                .withScenarioDataSources(TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS),
                        TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE)).withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);
    }

    /**
     * <pre>
     * <B>Method Name</B>: unassignRoleAndTargeGroupToUser
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_UNASSIGN_TARGETGROUP_ROLE}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_UNASSIGN_TARGETGROUP_ROLE}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: Demonstrate that a user with SECURITY_ADMIN role is able to unassign a Role and Target Group via CLI interface.
     *                      (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.15)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void unassignRoleAndTargeGroupToUser() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source " + "(AVAILABLE_USERS) filtered with 'securityAdmin' predicate");
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser).iterator().hasNext(),
                "No Data in Data Source " + "(AVAILABLE_USERS) filtered with 'genericUser' predicate");
        Preconditions.checkArgument(context.dataSource(CommonDataSources.ROLE_TO_CREATE).iterator().hasNext(),
                "No Data in Data Source " + "(ROLE_TO_CREATE)");
        Preconditions.checkArgument(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE).iterator().hasNext(),
                "No Data in Data Source " + "(TARGET_GROUP_TO_CREATE)");

        // Print DataSource to be use in this Test Case
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        AgatScenarioUtility.removeDataSources(CommonDataSources.USERS_TO_UPDATE, UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE,
                UserManagementTestSteps.MODIFIED_AVAILABLE_USERS);
        final TestDataSource<DataRecord> genericOperator = TafDataSources
                .filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser);
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(genericOperator, "Available Users (Filtered)"));

        context.addDataSource(CommonDataSources.USERS_TO_UPDATE,
                mergingDataField(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser),
                        TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser),
                        "roles" + SPLIT_CHR + "roleName"));
        context.addDataSource(CommonDataSources.USERS_TO_UPDATE, TafDataSources
                .shared(mergingDataField(context.dataSource(CommonDataSources.USERS_TO_UPDATE), AgatScenarioUtility
                                .replicateDataRecord(createDataSourceRecord(null, "targetGroupName", "ALL"),
                                        min(Iterators.size(context.dataSource(CommonDataSources.ROLE_TO_CREATE).iterator()),
                                                Iterators.size(context.dataSource(CommonDataSources.TARGET_GROUP_TO_CREATE).iterator()))),
                        "targetGroupName")));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.USERS_TO_UPDATE), "User Updating DataSource"));
        final int usersCountFromDataSource = Iterables.size(context.dataSource(CommonDataSources.USERS_TO_UPDATE));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), AgatScenarioUtility
                .replicateDataRecord(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin, usersCountFromDataSource));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Execute Test '%s' with <%s> vUsers...", methodName, vUsersDataDrivenScenario)));
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Unassign Roles and Target Group to Users (Scenario)")
                .addFlow(loginLogoutRestFlows.loginBuilder()).addFlow(userManagementTestFlows.updateEnmUserRoleAndTargetGroup())
                .addFlow(loginLogoutRestFlows.logoutBuilder()).doParallel(vUsersDataDrivenScenario)
                .withScenarioDataSources(TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS),
                        TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE)).withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);
    }

    /**
     * <pre>
     * <B>Method Name</B>: updateExistingUserInformation
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_UPDATEUSER}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_UPDATEUSER}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: Demonstrate how a user can be updated with User Management via REST interface.
     *                      (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.5)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void updateExistingUserInformation() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source " + "(CommonDataSources.AVAILABLE_USERS) filtered with 'securityAdmin' predicate");
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser).iterator().hasNext(),
                "No Data in Data Source " + "(CommonDataSources.AVAILABLE_USERS) filtered with 'genericUser' predicate");
        Preconditions.checkArgument(context.dataSource(USER_INFO_UPDATE).iterator().hasNext(), "No Data in Data Source (USER_INFO_UPDATE)");

        // Print DataSource to be use in this Test Case
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        AgatScenarioUtility.removeDataSources(CommonDataSources.USERS_TO_UPDATE, UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE,
                UserManagementTestSteps.MODIFIED_AVAILABLE_USERS);
        final TestDataSource<DataRecord> genericOperator = TafDataSources
                .filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser);
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(genericOperator, "Available Users (Filtered)"));

        context.addDataSource(CommonDataSources.USERS_TO_UPDATE,
                TafDataSources.shared(TafDataSources.merge(genericOperator, context.dataSource(USER_INFO_UPDATE))));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.USERS_TO_UPDATE), "Users to Update"));
        final int usersCountFromDataSource = Iterables.size(context.dataSource(CommonDataSources.USERS_TO_UPDATE));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), AgatScenarioUtility
                .replicateDataRecord(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin, usersCountFromDataSource));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Execute Test '%s' with <%s> vUsers...", methodName, vUsersDataDrivenScenario)));
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Update ENM User Information (Scenario)")
                .addFlow(loginLogoutRestFlows.loginBuilder()).addFlow(userManagementTestFlows.updateEnmUserAndCheck())
                .addFlow(loginLogoutRestFlows.logoutBuilder()).doParallel(vUsersDataDrivenScenario)
                .withScenarioDataSources(TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS),
                        TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE)).withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);
    }

    /**
     * <pre>
     * <B>Method Name</B>: changeUserPassword
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_CHANGEPASSWORD}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_CHANGEPASSWORD}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: Show how Security Administrator can change the password of a user.
     *                     (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.7)
     *                    Demonstrate how an ENM user can change his/her own profile password.
     *                     (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.11)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "180000")
    public void changeUserPassword() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source " + "(CommonDataSources.AVAILABLE_USERS) filtered with 'securityAdmin' predicate");
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser).iterator().hasNext(),
                "No Data in Data Source " + "(CommonDataSources.AVAILABLE_USERS) filtered with 'genericUser' predicate");

        // Print DataSource to be use in this Test Case
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        AgatScenarioUtility.removeDataSources(CommonDataSources.USERS_TO_UPDATE, UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE);
        final TestDataSource<DataRecord> genericOperator = TafDataSources
                .filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser);
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(genericOperator, "Available Users (Filtered)"));

        // Checking if Password should be updated by embedded method, or from external dataSource.
        if (passwordDataSourceExistence) {
            context.addDataSource(CommonDataSources.USERS_TO_UPDATE, TafDataSources
                    .shared(newPasswordAndExitCode(TafDataSources.merge(genericOperator, context.dataSource(USER_PASSWORD_UPDATE)), false)));
        } else {
            context.addDataSource(CommonDataSources.USERS_TO_UPDATE, TafDataSources.shared(newPasswordAndExitCode(genericOperator)));
        }
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(context.dataSource(CommonDataSources.USERS_TO_UPDATE), "Users to modify"));
        final int usersCountFromDataSource = Iterables.size(context.dataSource(CommonDataSources.USERS_TO_UPDATE));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), AgatScenarioUtility
                .replicateDataRecord(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin, usersCountFromDataSource));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Execute Test '%s' with <%s> vUsers...", methodName, vUsersDataDrivenScenario)));
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Update Password (Scenario)").addFlow(loginLogoutRestFlows.loginBuilder())
                .addFlow(userManagementTestFlows.changePassword()
                        .beforeFlow(AgatScenarioUtility.removeDataSources(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS)).afterFlow(
                                AgatScenarioUtility.hardBindTo(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS,
                                        UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE)).withDataSources(
                                TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE).bindColumn("originalPassword", "password")
                                        .bindColumn("password_01", "newPassword").bindColumn("expectedResponse_01", "expectedResult")))
                .addFlow(loginLogoutRestFlows.logoutBuilder()).addFlow(userManagementTestFlows.verifyUserLoginWithAssertAndNoDataSource().beforeFlow(
                        AgatScenarioUtility
                                .hardBindTo(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS, UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE))
                        .beforeFlow(AgatCommonUtility.printDataSourceRunnable(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE, LOGGER))
                        .beforeFlow(AgatScenarioUtility.backupDataSource(CommonDataSources.AVAILABLE_USERS),
                                AgatScenarioUtility.removeDataSources(CommonDataSources.AVAILABLE_USERS))
                        .afterFlow(AgatScenarioUtility.restoreDataSource(CommonDataSources.AVAILABLE_USERS))
                        .withDataSources(TestScenarios.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE)))
                .addFlow(loginLogoutRestFlows.logoutBuilder()).addFlow(loginLogoutRestFlows.loginBuilder().withDataSources(
                        TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE).bindColumn("password_01", "password")
                                .bindTo(CommonDataSources.AVAILABLE_USERS))).addFlow(userManagementTestFlows.changePassword()
                        .beforeFlow(AgatScenarioUtility.removeDataSources(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS)).afterFlow(
                                AgatScenarioUtility.hardBindTo(UserManagementTestSteps.MODIFIED_AVAILABLE_USERS,
                                        UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE)).withDataSources(
                                TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE).bindColumn("password_01", "password")
                                        .bindColumn("originalPassword", "newPassword").bindColumn("originalExpectedResult", "expectedResult")))
                .addFlow(loginLogoutRestFlows.logoutBuilder()).addFlow(userManagementTestFlows.verifyUserLoginWithAssertAndNoDataSource()
                        .beforeFlow(AgatCommonUtility.printDataSourceRunnable(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE, LOGGER))
                        .beforeFlow(AgatScenarioUtility.backupDataSource(CommonDataSources.AVAILABLE_USERS),
                                AgatScenarioUtility.removeDataSources(CommonDataSources.AVAILABLE_USERS))
                        .afterFlow(AgatScenarioUtility.restoreDataSource(CommonDataSources.AVAILABLE_USERS))
                        .withDataSources(TestScenarios.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE)))
                .addFlow(loginLogoutRestFlows.logoutBuilder())
                /* TODO - Add flows for Forced password update (After library Update) */.doParallel(vUsersDataDrivenScenario)
                .withScenarioDataSources(TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS))
                .withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);
    }

    /**
     * <pre>
     * <B>Method Name</B>: createUserWithPasswordAgeing
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_CREATEUSER_AGEING}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_CREATEUSER_AGEING}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: Demonstrate that a user with SECURITY_ADMIN role is able to create a user via CLI interface: the user will have customized
     *               Password Ageing.
     *                     (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.23)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void createUserWithPasswordAgeing() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source " + "(AVAILABLE_USERS) filtered with 'securityAdmin' predicate");
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.USERS_TO_CREATE), UserPredicates.ageingUser).iterator().hasNext(),
                "No Data in Data Source (USERS_TO_CREATE) filtered with 'ageingUser' predicate");

        // Print DataSource to be use in this Test Case
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        final TestDataSource<DataRecord> userToCreate = TafDataSources
                .shared(TafDataSources.filter(context.dataSource(CommonDataSources.USERS_TO_CREATE), UserPredicates.ageingUser));
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(userToCreate, "Users to Create (Filtered)"));
        final int usersCountFromDataSource = Iterables.size(userToCreate);
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), AgatScenarioUtility
                .replicateDataRecord(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin, usersCountFromDataSource));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug(LoggerMessageSet.getOperation(String.format("Execute Test '%s' with <%s> vUsers...", methodName, vUsersDataDrivenScenario)));
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Create User Password Ageing (Scenario)")
                .addFlow(loginLogoutRestFlows.loginBuilder().beforeFlow(AgatScenarioUtility.backupDataSource(CommonDataSources.AVAILABLE_USERS)))
                .addFlow(userManagementTestFlows.createEnmUser()
                        .beforeFlow(AgatScenarioUtility.backupAndReplace(CommonDataSources.USERS_TO_CREATE, userToCreate))
                        .afterFlow(AgatScenarioUtility.restoreDataSource(CommonDataSources.USERS_TO_CREATE)))
                .addFlow(loginLogoutRestFlows.logoutBuilder().afterFlow(AgatScenarioUtility.restoreDataSource(CommonDataSources.AVAILABLE_USERS)))
                .doParallel(vUsersDataDrivenScenario)
                .withScenarioDataSources(TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS))
                .withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);

        // Checking Scenario result
        LOGGER.debug("Checking Created DataSources");
        assertThat(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE).iterator().hasNext())
                .as("DataSource 'USERS_TO_VERIFY_DATASOURCE' was not created");
        final int createUserSize = Iterables.size(userToCreate);
        final int verifyUserSize = Iterables.size(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE));
        LOGGER.debug("{}", AgatCommonUtility.printDataSource(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE),
                UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE));
        LOGGER.debug("Checking DataSource size: 'USERS_TO_CREATE' (filtered) [], 'USERS_TO_VERIFY_DATASOURCE' []", createUserSize, verifyUserSize);
        assertThat(createUserSize).isEqualTo(verifyUserSize)
                .as("DataSource 'USERS_TO_CREATE' (filtered) has not same size as 'USERS_TO_VERIFY_DATASOURCE'");
        LOGGER.debug("{}", AgatCommonUtility.printDataSource(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE),
                UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE));
    }

    /**
     * <pre>
     * <B>Method Name</B>: updateExistingUserAgeingInformation
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_UPDATEUSER_AGEING}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_UPDATEUSER_AGEING}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: Demonstrate how customized Password Ageing information for a user can be updated via CLI interface by user with
     *               SECURITY_ADMIN role.
     *                     (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.24)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void updateExistingUserAgeingInformation() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source " + "(AVAILABLE_USERS) filtered with 'securityAdmin' predicate");
        Preconditions.checkArgument(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE).iterator().hasNext(),
                "No Data in Data Source (USERS_TO_VERIFY_DATASOURCE)");
        Preconditions
                .checkArgument(context.dataSource(USER_INFO_AGEING_UPDATE).iterator().hasNext(), "No Data in Data Source (USER_INFO_AGEING_UPDATE)");

        // Print original DataSource, prepare for DataDriven scenario and print resulting DataSource.
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        context.removeDataSource(CommonDataSources.USERS_TO_UPDATE);
        LOGGER.debug("{}", AgatCommonUtility
                .printDataSource(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE), "Users to Verify DataSource"));
        LOGGER.debug("{}",
                AgatCommonUtility.printDataSource(context.dataSource(USER_INFO_AGEING_UPDATE), "User Information to Update Password Ageing"));
        context.addDataSource(CommonDataSources.USERS_TO_UPDATE, TafDataSources
                .shared(mergeToUser(context.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE),
                        context.dataSource(USER_INFO_AGEING_UPDATE))));
        LOGGER.debug("{}", AgatCommonUtility
                .printDataSource(context.dataSource(CommonDataSources.USERS_TO_UPDATE), "User Information to Update Password Ageing"));
        final int usersCountFromDataSource = Iterables.size(context.dataSource(CommonDataSources.USERS_TO_UPDATE));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), AgatScenarioUtility
                .replicateDataRecord(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin, usersCountFromDataSource));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug("Creating and Executing Scenario...");
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Update User Password Ageing Information (Scenario)")
                .addFlow(loginLogoutRestFlows.loginBuilder()).addFlow(userManagementTestFlows.updateEnmUserAndCheck())
                .addFlow(loginLogoutRestFlows.logoutBuilder())
                .withScenarioDataSources(TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS),
                        TestScenarios.dataSource(CommonDataSources.USERS_TO_UPDATE)).doParallel(vUsersDataDrivenScenario)
                .withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);
    }

    /**
     * <pre>
     * <B>Method Name</B>: importUsersFromXml
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_IMPORTUSERFROMXML}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_IMPORTUSERFROMXML}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: Demonstrate that a user with SECURITY_ADMIN role is able to import bunch of users from an XML file to the ENM system.
     *                     (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.3)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "180000")
    public void importUsersFromXml() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);

        // Checking datasource existence (if not present, test fail)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source (AVAILABLE_USERS) filtered with 'securityAdmin' predicate)");
        LOGGER.debug("{}", AgatCommonUtility
                .printDataSource(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin),
                        "securityAdmin DataSource"));
        LOGGER.debug("{}",
                AgatCommonUtility.printDataSource(context.dataSource(CommonDataSources.AVAILABLE_USERS), "Available Users (before import Scenario)"));

        // Print original DataSource, prepare for DataDriven scenario and print resulting DataSource.
        context.addDataSource(USER_XML_FILE_TO_IMPORT, TafDataSources.fromTafDataProvider(USER_XML_FILE_TO_IMPORT));
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        Preconditions
                .checkArgument(context.dataSource(USER_XML_FILE_TO_IMPORT).iterator().hasNext(), "No Data in Data Source (USER_XML_FILE_TO_IMPORT)");
        LOGGER.debug("{}", AgatCommonUtility.printDataSource(context.dataSource(USER_XML_FILE_TO_IMPORT), "File XML to import Users"));
        context.addDataSource(NEW_USERS_TO_CHECK,
                TafDataSources.shared(TafDataSources.filter(context.dataSource(USERS_TO_IMPORT), UserPredicates.newImportedUsers())));
        LOGGER.debug("{}", AgatCommonUtility.printDataSource(context.dataSource(NEW_USERS_TO_CHECK), "New Users to import from XML file"));

        final int usersCountFromDataSource = Iterables.size(context.dataSource(USER_XML_FILE_TO_IMPORT));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Print original DataSource, prepare for DataDriven scenario and print resulting DataSource.
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), AgatScenarioUtility
                .replicateDataRecord(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin, vUsersDataDrivenScenario));
        addNodetypeInfo(INPUT_DATASOURCE);
        context.addDataSource(INPUT_DATASOURCE,
                TafDataSources.merge(context.dataSource(INPUT_DATASOURCE), context.dataSource(USER_XML_FILE_TO_IMPORT)));

        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug("Creating and Executing Scenario...");
        final TestScenario scenario = TestScenarios.dataDrivenScenario("Import users from XML file (Scenario)")
                .addFlow(importExportFlows.loginUploadAndCheckNewUsersFlow()).addFlow(TestScenarios.flow("Verify User Can Login after Import XML")
                        .addSubFlow(userManagementTestFlows.verifyUserLoginWithAssertAndNoDataSource().alwaysRun())
                        .beforeFlow(AgatScenarioUtility.backupDataSource(CommonDataSources.AVAILABLE_USERS),
                                AgatScenarioUtility.removeDataSources(CommonDataSources.AVAILABLE_USERS),
                                AgatScenarioUtility.hardBindTo(NEW_USERS_TO_CHECK, UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE))
                        .afterFlow(AgatScenarioUtility.restoreCombinedDataSource(CommonDataSources.AVAILABLE_USERS))
                        .withVusers(Iterables.size(context.dataSource(NEW_USERS_TO_CHECK)))
                        .withDataSources(TestScenarios.dataSource(UserManagementTestSteps.USERS_TO_VERIFY_DATASOURCE))).withScenarioDataSources(
                        TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS)
                                .bindTo(ImportExportFlows.FILE_NAME_TO_IMPORT_USERS)).doParallel(vUsersDataDrivenScenario)
                .withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);

        LOGGER.debug("{}",
                AgatCommonUtility.printDataSource(context.dataSource(CommonDataSources.AVAILABLE_USERS), "Available Users (after import Scenario)"));
    }

    /**
     * <pre>
     * <B>Method Name</B>: configureLoginLock
     * <B>Test Case ID</B>: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#ID_USERMNG_CONFIGURELOGINLOCK}
     * Test Name: {@value com.ericsson.nms.security.test.usermanagement.scenario.TestInfo#NAME_USERMNG_CONFIGURELOGINLOCK}
     * <B>Test Group</B>: ENM_EXTERNAL_TESTWARE
     * <B>Description</B>: Demonstrates that a user with SECURITY_ADMIN role is able to configure account lockout attributes with the System
     *               Security Configuration CLI.
     *                     (From document GAT_IDAM_&amp;_ComAC - BNEP-16:000588 Uen, Paragraph:  3.3)
     * </pre>
     */
    @TestSuite
    @Test(groups = { "ENM_EXTERNAL_TESTWARE" })
    @TestOptions(timeout = "120000")
    public void configureLoginLock() {
        final String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        // Print Start Section information and Check DataSource availability
        beforeTestLoggin(methodName);
        LOGGER.debug("{}", AgatCommonUtility.printDataSource(context.dataSource(CommonDataSources.AVAILABLE_USERS), "'AVAILABLE_USERS' DataSource"));

        // Checking datasource existence (if not present, test fails)
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin).iterator().hasNext(),
                "No Data in Data Source (AVAILABLE_USERS) filtered with 'securityAdmin' predicate)");
        LOGGER.debug("{}", AgatCommonUtility
                .printDataSource(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin),
                        "securityAdmin DataSource"));
        Preconditions.checkArgument(
                TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser).iterator().hasNext(),
                "No Data in Data Source (AVAILABLE_USERS) filtered with 'genericUser' predicate)");
        LOGGER.debug("{}", AgatCommonUtility
                .printDataSource(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser),
                        "genericUser DataSource"));

        // Checking existence of DataSource to modify Account Lockout settings
        Preconditions.checkArgument(context.dataSource(PasswordSettingsFlows.MODIFY_PASSWORDOBJECT_DATASOURCE).iterator().hasNext(),
                "No Data in Data Source (MODIFY_PASSWORDOBJECT_DATASOURCE)");
        LOGGER.debug("{}", AgatCommonUtility.printDataSource(context.dataSource(PasswordSettingsFlows.MODIFY_PASSWORDOBJECT_DATASOURCE),
                "'MODIFY_PASSWORDOBJECT_DATASOURCE' DataSource"));

        // Checking existence of Login sequence (invalid credential (lockout) -> valid credential (deny) -> (wait) -> valid credental (login))
        Preconditions.checkArgument(context.dataSource(AgatUserManagementTestFlow.LOCKOUT_SEQUENCE_DATASOURCE).iterator().hasNext(),
                "No Data in Data Source (LOCKOUT_SEQUENCE_DATASOURCE)");
        LOGGER.debug("{}", AgatCommonUtility.printDataSource(context.dataSource(AgatUserManagementTestFlow.LOCKOUT_SEQUENCE_DATASOURCE),
                "'LOCKOUT_SEQUENCE_DATASOURCE' DataSource"));

        final int usersCount = Iterables
                .size(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.genericUser));
        final int vUsers = (boolean) context.getAttribute("parallelExecution") ? usersCount : 1;

        // Print original DataSource, prepare for DataDriven scenario and print resulting DataSource.
        LOGGER.debug(LoggerMessageSet.getOperation("reading ad updating Scenario Datasources...."));
        final int usersCountFromDataSource = Iterables
                .size(TafDataSources.filter(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin));
        final int vUsersDataDrivenScenario = (boolean) context.getAttribute("parallelExecution") ? usersCountFromDataSource : 1;

        // Prepare DataDriven DataSource and Print
        prepareDataDrivenDataSource(INPUT_DATASOURCE, testetCases.getTestId(methodName), testetCases.getTestName(methodName), AgatScenarioUtility
                .replicateDataRecord(context.dataSource(CommonDataSources.AVAILABLE_USERS), UserPredicates.securityAdmin, usersCountFromDataSource));
        addNodetypeInfo(INPUT_DATASOURCE);
        LOGGER.debug("{}", LoggerMessageSet
                .printDataSource(context.dataSource(INPUT_DATASOURCE), String.format("Input DataSource for testCase <%s>", methodName)));

        // Preparing and Executing Test Scenario
        LOGGER.debug("Creating and Executing Scenario...");
        LOGGER.debug("DataDriven Scenario run with {} vUsers, Lockout tests run with {} vusers.", vUsersDataDrivenScenario, vUsers);
        final TestScenario scenario = TestScenarios.dataDrivenScenario("User LockOut for failed password (Scenario)").addFlow(
                TestScenarios.flow("Modify Password Settings Objects").addSubFlow(passwordSettingsFlows.loginBackupAndModifyPasswordSettings()))
                .addFlow(TestScenarios.flow("User Login check sequence").beforeFlow(TafDataSources.shareDataSource(CommonDataSources.AVAILABLE_USERS))
                        .addSubFlow(agatUserManagementTestFlow.loginLockSequence())
                        .withDataSources(TestScenarios.dataSource(CommonDataSources.AVAILABLE_USERS).withFilter(UserPredicates.genericUser))
                        .withVusers(vUsers))
                .addFlow(TestScenarios.flow("Logout users").addSubFlow(loginLogoutRestFlows.logoutBuilder()).withVusers(vUsers))
                .withScenarioDataSources(TestScenarios.dataSource(INPUT_DATASOURCE).bindTo(CommonDataSources.AVAILABLE_USERS))
                .doParallel(vUsersDataDrivenScenario).withExceptionHandler(ScenarioExceptionHandler.PROPAGATE).build();
        CommonUtils.start(scenario);
    }

    /**************************************************************************************************************.
     /*                            Some private and protected Methods used in this Scenario                         .
     /**************************************************************************************************************.

     /**
     * Name: addNodetypeInfo()             [protected]
     * Description: This method get DataSource selected from Context and Add in every DataRecord a field with NodeType value.
     *
     * N.B. DataSource are unmodifiable object, so I need to create a new one and add NodeType field to every record; at the end I need
     * to replace old DataSource with the new one.
     *
     * @param dataSourceName
     *         - DataSource name
     * @param nodeType
     *         (Optional) - Nodetype to add in every DataRecord. If not present, node type value will be 'NOT_SUPPORTED'.
     */
    protected void addNodetypeInfo(final String dataSourceName, final String nodeType) {
        final String nodetypeField = "nodeType";
        final TestDataSource<DataRecord> originalDataSource = context.dataSource(dataSourceName);
        final TestDataSource<DataRecord> resultingDataSource = TestDataSourceFactory.createDataSource();

        final Iterator<DataRecord> fieldDataSourceIterator = originalDataSource.iterator();
        while (fieldDataSourceIterator.hasNext()) {
            final DataRecord thisDataRecord = fieldDataSourceIterator.next();
            resultingDataSource.addRecord().setFields(thisDataRecord).setField(nodetypeField, nodeType);
        }

        context.removeDataSource(dataSourceName);
        context.addDataSource(dataSourceName, TafDataSources.shared(resultingDataSource));
    }

    protected void addNodetypeInfo(final String dataSourceName) {
        addNodetypeInfo(dataSourceName, NOT_SUPPORTED);
    }

    /**
     * Name: prepareDataDrivenDataSource()             [private]
     * Description: This method should be use to Add TestId (and TestName) to selected Datasource. We can get DataSource to modify from Context or
     * get it prom method Parameter, then write it in Taf Context. Method can Add only Test ID or TestID and Test Name.
     * N.B. DataSource are unmodifiable object, so I need to create a new one and add NodeType field to every record; at the end I need
     * to replace old DataSource with the new one.
     *
     * @param dataSourceName
     *         - DataSource to write in Context
     * @param testId
     *         - Test Id field to add
     * @param testName
     *         - Test Name to add (optional)
     * @param originalDataSource
     *         - DataSource to upgrade (optional); if not present, source data will be get from 'DataSourceName' from Context.
     */
    private void prepareDataDrivenDataSource(final String dataSourceName, final String testId, final String testName,
            Iterable<? extends DataRecord> originalDataSource) {
        // Check if 'values' datasource is present in parameters (not null)
        if (originalDataSource == null) {
            originalDataSource = context.dataSource(dataSourceName);
            context.removeDataSource(dataSourceName);
        }

        // Creating destination DataSource
        final TestDataSource<DataRecord> resultingDataSource = TestDataSourceFactory.createDataSource();

        // Loop through Original DataSource dataRecords and add selected fields
        for (final DataRecord singleDataRecord : originalDataSource) {
            final DataRecordModifier dataRecordModifier = resultingDataSource.addRecord().setFields(singleDataRecord);
            dataRecordModifier.setField(DataDrivenTestScenarioBuilder.TEST_CASE_ID, testId);
            if (testName != null) {
                dataRecordModifier.setField(DataDrivenTestScenarioBuilder.TEST_CASE_TITLE, testName);
            }
        }
        context.addDataSource(dataSourceName, TafDataSources.shared(resultingDataSource));
    }

    /**
     * Name: mergingDataField()             [private]
     * Description: This method should be use to merge two datasources adding only some fields from second one.
     *
     * @param baseDataSource
     *         - Datasource where I should put fields from second one.
     * @param addedDataSource
     *         - Data Source where fields should be getted.
     * @param fieldsToAdd
     *         - List of fields to get. If Source and Destination has different name, format of element should be:
     *         source -> destination.
     *         If field name do not change, you can put only one string. Here tou can put more than one field separated by comma.
     *
     * @return Generated DataSource
     */
    private TestDataSource<? extends DataRecord> mergingDataField(final TestDataSource<DataRecord> baseDataSource,
            final TestDataSource<DataRecord> addedDataSource, final String... fieldsToAdd) {
        final TestDataSource<DataRecord> generatedDataSource = TestDataSourceFactory.createDataSource();

        // Generating addedDataSource fields
        for (final DataRecord singleDataRecord : addedDataSource) {
            final DataRecordModifier dataRecordModifier = generatedDataSource.addRecord();

            for (final String singleField : fieldsToAdd) {
                final String[] splitted = singleField.split(SPLIT_CHR);
                dataRecordModifier.setField(splitted[splitted.length == 1 ? 0 : 1], singleDataRecord.getFieldValue(splitted[0]));
            }
        }

        // merging operation
        return TafDataSources.merge(baseDataSource, generatedDataSource);
    }

    /**
     * Name: createDataSourceRecord()             [private]
     * Description: This method could be use to add fields in a record into a New dataSource or in existin one.
     *
     * @param resultingDataSource
     *         DataSource to modify
     * @param field
     *         field name to add
     * @param value
     *         field value
     *
     * @return Modified dataSource
     */
    private TestDataSource<DataRecord> createDataSourceRecord(TestDataSource<DataRecord> resultingDataSource, final String field,
            final String value) {
        if (resultingDataSource == null) {
            resultingDataSource = TestDataSourceFactory.createDataSource();
        }
        final DataRecordModifier dataRecordModifier = resultingDataSource.addRecord();
        dataRecordModifier.setField(field, value);
        return resultingDataSource;
    }

    /**
     * Name: newPasswordAndExitCode()             [private]
     * Description: This method modify dataSource password field, replacing value with new one, and preparing (if flag 'isNewPassword' is set)
     * datasource for operation check.
     *
     * @param originalDataSource
     *         Datasource to Modify
     *
     * @return Modified DataSource
     */
    private TestDataSource<DataRecord> newPasswordAndExitCode(final TestDataSource<DataRecord> originalDataSource, final Boolean generatePassword) {
        final TestDataSource<DataRecord> resultingDataSource = TestDataSourceFactory.createDataSource();
        // Loop through Original DataSource dataRecords and add selected fields
        for (final DataRecord oneDataRecord : originalDataSource) {
            final DataRecordModifier dataRecordModifier = resultingDataSource.addRecord().setFields(oneDataRecord);
            final String originalPassword = oneDataRecord.getFieldValue("password");
            String fieldValue = "";
            dataRecordModifier.setField("originalPassword", originalPassword);
            dataRecordModifier.setField("originalExpectedResult", "NO_CONTENT");
            fieldValue = oneDataRecord.getFieldValue("password_01");
            if (generatePassword || fieldValue == null || fieldValue.isEmpty()) {
                dataRecordModifier.setField("password_01", agatUserManagementTestStep.generateNewPassword(originalPassword));
                dataRecordModifier.setField("expectedResponse_01", "NO_CONTENT");
            }
            fieldValue = oneDataRecord.getFieldValue("password_02");
            if (generatePassword || fieldValue == null || fieldValue.isEmpty()) {
                dataRecordModifier.setField("password_02", agatUserManagementTestStep.generateNewPassword(originalPassword));
                dataRecordModifier.setField("expectedResponse_02", "NO_CONTENT");
            }
        }
        return resultingDataSource;
    }

    private TestDataSource<DataRecord> newPasswordAndExitCode(final TestDataSource<DataRecord> originalDataSourc) {
        return newPasswordAndExitCode(originalDataSourc, true);
    }

    /**
     * Name: mergeToUser()             [private]
     * Description: This method should be use to create a new DataSource with User Field followed by other field from selected datasource.
     *
     * @param userDataSource
     *         - Datasource where get UserName
     * @param dataToAppend
     *         - DataSource where get fields to append
     *
     * @return result DataSource
     */
    private TestDataSource<DataRecord> mergeToUser(final TestDataSource<DataRecord> userDataSource, final TestDataSource<DataRecord> dataToAppend) {
        final TestDataSource<DataRecord> userToUpdateDatasource = TestDataSourceFactory.createDataSource();
        if (Iterators.size(userDataSource.iterator()) == Iterators.size(dataToAppend.iterator())) {
            final Iterator<DataRecord> userIterator = userDataSource.iterator();
            final Iterator<DataRecord> dataIterator = dataToAppend.iterator();
            while (userIterator.hasNext()) {
                final DataRecord userDataRecord = userIterator.next();
                final DataRecord dataDataRecord = dataIterator.next();
                userToUpdateDatasource.addRecord().setField("username", userDataRecord.getFieldValue("username")).setFields(dataDataRecord);
            }
        }
        return userToUpdateDatasource;
    }

    /**
     * Name: beforeTestLoggin()             [private]
     * Description: This method should be use to give a common Logger to describe Test Case.
     *
     * @param methodName
     *         - Method neme for Test Case
     */
    private void beforeTestLoggin(final String methodName) {
        LOGGER.info(LoggerMessageSet.getSection(String.format(
                "Executing TestCase <%s> from <%s> class" + LoggerMessageSet.getLF() + LoggerMessageSet.getTAB() + "Test Id <%s>, Test Name <%s>"
                        + LoggerMessageSet.getLF() + LoggerMessageSet.getTAB() + "Test Description: %s", methodName, getClass().getSimpleName(),
                testetCases.getTestId(methodName), testetCases.getTestName(methodName), testetCases.getTestDescription(methodName))));
    }

    /**
     * Name: updatePasswordFromCustomer()             [private]
     * Description: This method should be use to Update 'password' field with password getted from Customer 'desiderata': we can get this password
     * from records with 'specialAction' = 'update'.
     *
     * @param originalDataSource
     *         - DataSource to be modified
     *
     * @return Modified DataSource
     */
    private TestDataSource<? extends DataRecord> updatePasswordFromCustomer(final TestDataSource originalDataSource) {
        Iterator<DataRecord> originalDatasourceItarator = originalDataSource.iterator();
        final TestDataSource<DataRecord> resultDataSource = TestDataSourceFactory.createDataSource();
        String customerPassword = null;

        // Searching customer password...
        while (originalDatasourceItarator.hasNext()) {
            final DataRecord thisDataRecord = originalDatasourceItarator.next();
            if ("update".equalsIgnoreCase(thisDataRecord.getFieldValue("specialAction").toString())) {
                customerPassword = (String) thisDataRecord.getFieldValue("password");
                break;
            }
        }

        // Replacing password field with customer password
        originalDatasourceItarator = originalDataSource.iterator();
        while (originalDatasourceItarator.hasNext()) {
            final DataRecord thisDataRecord = originalDatasourceItarator.next();
            resultDataSource.addRecord().setFields(thisDataRecord).setField("password", customerPassword);
        }

        // Return updated DataSource (With Password Updated)
        return resultDataSource;
    }

    /**
     * Name: updateFieldValueFromCsv()             [private]
     * Description: This method should be use to modify DataSource Content with values from Selected DataSource (fieldsToUpdate).
     *
     * @param originalDataSource
     *         - DataSource to modify
     * @param fieldsToUpdate
     *         - Field to update
     *
     * @return Modified DataSource
     */
    private TestDataSource<? extends DataRecord> updateFieldValueFromCsv(final TestDataSource originalDataSource,
            final TestDataSource fieldsToUpdate) {
        final TestDataSource<DataRecord> resultDataSource = TestDataSourceFactory.createDataSource();
        final Iterator<DataRecord> originalDatasourceItarator = originalDataSource.iterator();
        Integer userCount = 1;
        while (originalDatasourceItarator.hasNext()) {
            final DataRecord thisDataRecord = originalDatasourceItarator.next();
            final DataRecordModifier dataRecordModifier = resultDataSource.addRecord().setFields(thisDataRecord);
            if ("update".equalsIgnoreCase(thisDataRecord.getFieldValue("specialAction").toString())) {
                final Iterator<DataRecord> fieldsToUpdateItarator = fieldsToUpdate.iterator();
                while (fieldsToUpdateItarator.hasNext()) {
                    final DataRecord fieldDataRecord = fieldsToUpdateItarator.next();
                    if (userCount.equals(Integer.parseInt((String) fieldDataRecord.getFieldValue("userCount")))) {
                        final String keyValue = fieldDataRecord.getFieldValue("fieldName");
                        final String fieldValue = fieldDataRecord.getFieldValue("fieldValue");
                        if (fieldValue.startsWith("[") && fieldValue.endsWith("]")) {
                            // Executing requested method to modify selected Paramether
                            dataRecordModifier.setField(keyValue, executingMethod(fieldValue.replaceAll("\\[", "").replaceAll("\\]", ""),
                                    (String) thisDataRecord.getFieldValue(keyValue)));
                        } else {
                            dataRecordModifier.setField(keyValue, fieldValue);
                        }
                    }
                }
                userCount++;
            }
        }
        return resultDataSource;
    }

    /**
     * Name: executingMethod()             [private]
     * Description: This method use 'Reflection' to execute a given method (methodName) with one parameter (stringToModify). If 'methodName' was
     * not found in classPath, the action will be performed by default method (generateNewPassword).
     *
     * @param methodName
     *         - Name of method to execute
     * @param stringToModify
     *         - Argument for method to execute
     *
     * @return - Result of Method Execution (modified String)
     */
    private String executingMethod(final String methodName, final String stringToModify) {
        Method method = null;
        try {
            method = this.getClass().getMethod(methodName, String.class);
            return (String) method.invoke(this, stringToModify);
        } catch (final NoSuchMethodException e) {
            LOGGER.debug("No Such Method Exception: \n{}", e.getStackTrace());
        } catch (final IllegalAccessException e) {
            LOGGER.debug("Illegal Access Exception: \n{}", e.getStackTrace());
        } catch (final InvocationTargetException e) {
            LOGGER.debug("Invocation Target Exception: \n{}", e.getStackTrace());
        } finally {
            return agatUserManagementTestStep.generateNewPassword(stringToModify);
        }
    }

    /**
     * Name: createAndSaveXmlFile()             [private]
     * Description: This method should be use for creation of XML file for import operation: it  prepare and save XML file in filesystem and fill
     * DataSource with import operation specification (field: fileName,mode,toBeCreated,toBeUpdated,failedAmount).
     *
     * @param usersDataSource
     *         - This Datasource contains Data of users to be created or updated via XML to import
     *
     * @return - Content of DataSource for Import File (field: fileName,mode,toBeCreated,toBeUpdated,failedAmount)
     */
    private TestDataSource<? extends DataRecord> createAndSaveXmlFile(final TestDataSource<DataRecord> usersDataSource) {
        final TestDataSource<DataRecord> resultDataSource = TestDataSourceFactory.createDataSource();
        final String importTestcaseFilename = DataHandler.getConfiguration()
                .getProperty("import.xml.filename", "Agat_Local_UsersToImportDataSource.xml", String.class);
        File fileObject = null;

        // Debug Option: Browsing DataSource....
        LOGGER.debug(ImportDatasourceParser.printUserDatasourceStructure(usersDataSource, "Users Data Source for XML"));
        final DOMSource domSource = ImportDatasourceParser.userDataSourceToXml(usersDataSource);

        try {
            fileObject = new File(importTestcaseFilename);
            final StreamResult file = new StreamResult(fileObject);
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, file);
        } catch (final TransformerException exc) {
            LOGGER.error("\nError in XML transformation: {}\n{}", exc.getMessage(), exc.getStackTrace());
            assertThat(false).as("Error in XML transformation").isTrue();
        }

        // Get information for 'resultDataSource' from uersDataSource.
        final Iterator<DataRecord> originalDatasourceItarator = usersDataSource.iterator();
        final String fileName = fileObject.getAbsolutePath();
        Integer usersToCreate = 0;
        Integer usersToModify = 0;
        final Integer failedImportUsers = 0;
        // Loop through Datasource and check 'specialAction' field
        while (originalDatasourceItarator.hasNext()) {
            final DataRecord thisDataRecord = originalDatasourceItarator.next();
            if ("update".equalsIgnoreCase(thisDataRecord.getFieldValue("specialAction").toString())) {
                usersToModify++;
            }
            if ("new".equalsIgnoreCase(thisDataRecord.getFieldValue("specialAction").toString())) {
                usersToCreate++;
            }
        }

        // SetFields from previous operation
        resultDataSource.addRecord().setField("fileName", fileName).setField("mode", "addNew").setField("toBeCreated", usersToCreate)
                .setField("toBeUpdated", usersToModify).setField("failedAmount", failedImportUsers);
        return resultDataSource;
    }
}
