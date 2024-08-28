/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
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
import static com.ericsson.oss.testware.security.usermanagement.flows.CreateUserFlow.DATA_SOURCE_CREATE_USER;
import static com.ericsson.oss.testware.security.usermanagement.flows.DeleteUserFlow.DATA_SOURCE_DELETE_USER_ON_EDIT;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.nms.security.test.usermanagement.datasource.PredicatesExt;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutUiFlows;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.usermanagement.flows.CreateUserFlow;
import com.ericsson.oss.testware.security.usermanagement.flows.DeleteUserFlow;

public class CreateEditAndDeleteUserScenario extends TafTestBase {
    final static String USER_MGMT_CSV_PATH = "data/usermanagement_2_0/";
    final static String CREATE_USER_CSV = USER_MGMT_CSV_PATH + "CreateUser.csv";
    final static String CREATE_USER_FOR_DELETE_ON_EDIT_CSV = USER_MGMT_CSV_PATH + "CreateUserForDeleteOnEdit.csv";
    private final static String DELETE_USER_ON_EDIT_CSV = USER_MGMT_CSV_PATH + "DeleteUserOnEdit.csv";
    private final static String CANCEL_CREATE_USER_CSV = USER_MGMT_CSV_PATH + "CancelCreateUser.csv";
    private final static String ADMIN_USER_CSV = USER_MGMT_CSV_PATH + "AdminUser.csv";
    private final static String USER_MGMT_APP_NAME_FILTER = "/#usermanagement/?filter={\"username\"%3A\"UserManagement_CreateUser\"}";

    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @Inject
    private TestContext context;

    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;

    @Inject
    private CreateUserFlow createUserFlow;


    @Inject
    private DeleteUserFlow deleteUserFlow;

    @Inject
    private GimCleanupFlows gimCleanupFlows;



    @Test(groups = {"Acceptance", "RFA", "Create User"})
    @TestSuite
    public void testCreateAndDeleteUserFromEdit() throws Exception {
        context.addDataSource(DATA_SOURCE_CREATE_USER, TafDataSources.shared(TafDataSources.fromCsv(CREATE_USER_FOR_DELETE_ON_EDIT_CSV)));
        context.addDataSource(DATA_SOURCE_DELETE_USER_ON_EDIT, TafDataSources.shared(TafDataSources.fromCsv(DELETE_USER_ON_EDIT_CSV)));
        context.addDataSource(AVAILABLE_USERS, TafDataSources.shared(TafDataSources.fromCsv(ADMIN_USER_CSV)));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Create and Delete User Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("2")))
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME_FILTER))
                .addFlow(createUserFlow.createUser())
                .addFlow(deleteUserFlow.deleteUserOnEdit())
                .addFlow(loginLogoutFlow.logout())
                .addFlow(loginLogoutFlow.cleanUpFlow())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

    @Test(groups = { "Acceptance", "RFA", "Create User" })
    @TestSuite
    public void testCancelCreateUser() throws Exception {
        context.addDataSource(DATA_SOURCE_CREATE_USER, TafDataSources.shared(TafDataSources.fromCsv(CANCEL_CREATE_USER_CSV)));
        context.addDataSource(AVAILABLE_USERS, TafDataSources.shared(TafDataSources.fromCsv(ADMIN_USER_CSV)));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Cancel create User Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("3")))
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME_FILTER))
                .addFlow(createUserFlow.cancelCreateUser()).addFlow(loginLogoutFlow.logout()).addFlow(loginLogoutFlow.cleanUpFlow())
                .withExceptionHandler(new UiExceptionHandler(context)).build();

        start(scenario);
    }

}