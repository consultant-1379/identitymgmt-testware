/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
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
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.FORCE_PASSWORD_CHANGE;
import static com.ericsson.oss.testware.security.usermanagement.flows.CreateUserFlow.DATA_SOURCE_CREATE_USER;
import static com.ericsson.oss.testware.security.usermanagement.flows.DeleteUserFlow.DATA_SOURCE_DELETE_USER_ON_LIST;
import static com.ericsson.oss.testware.security.usermanagement.flows.EditUserFlow.DATA_SOURCE_EDIT_USER;

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
import com.ericsson.oss.testware.security.usermanagement.flows.EditUserFlow;

public class NewEditUserScenario extends TafTestBase {
    final static String USER_MGMT_CSV_PATH = "data/usermanagement_2_0/";
    final static String CREATE_USER_CSV = USER_MGMT_CSV_PATH + "CreateUser.csv";
    final static String CREATE_USER_FOR_DELETE_ON_EDIT_CSV = USER_MGMT_CSV_PATH + "CreateUserForDeleteOnEdit.csv";
    private final static String CREATE_USER_CHANGE_PASSWORD_CSV = "CreateUserChangePassword.csv";
    private final static String EDIT_USER_CSV = USER_MGMT_CSV_PATH + "EditUser.csv";
    private final static String DELETE_USER_ON_LIST_CSV = USER_MGMT_CSV_PATH + "DeleteUserOnList.csv";
    private final static String ADMIN_USER_CSV = USER_MGMT_CSV_PATH + "AdminUser.csv";
    private final static String USER_MGMT_APP_NAME_FILTER = "/#usermanagement/?filter={\"username\"%3A\"UserManagement_CreateUser\"}";

    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @Inject
    private TestContext context;

    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;

    @Inject
    private EditUserFlow editUserFlow;

    @Test(groups = { "Acceptance", "RFA", "RFA250", "Edit User" })
    @TestSuite
    public void testEditUser() throws Exception {
        context.addDataSource(DATA_SOURCE_CREATE_USER, TafDataSources.shared(TafDataSources.fromCsv(CREATE_USER_CSV)));
        context.addDataSource(FORCE_PASSWORD_CHANGE, TafDataSources.shared(TafDataSources.fromCsv(CREATE_USER_CHANGE_PASSWORD_CSV)));
        context.addDataSource(DATA_SOURCE_EDIT_USER, TafDataSources.shared(TafDataSources.fromCsv(EDIT_USER_CSV)));
        context.addDataSource(DATA_SOURCE_DELETE_USER_ON_LIST, TafDataSources.shared(TafDataSources.fromCsv(DELETE_USER_ON_LIST_CSV)));
        context.addDataSource(AVAILABLE_USERS, TafDataSources.shared(TafDataSources.fromCsv(ADMIN_USER_CSV)));
        context.addDataSource(TEST_DS, fromCsv(TEST_DS_CSV));

        final TestScenario scenario = dataDrivenScenario("Edit User Scenario")
                .withScenarioDataSources(dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("41")))
                .addFlow(loginLogoutFlow.loginWithRedirect(USER_MGMT_APP_NAME_FILTER))
                .addFlow(editUserFlow.editUser())
                .addFlow(loginLogoutFlow.logout())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        start(scenario);
    }

}