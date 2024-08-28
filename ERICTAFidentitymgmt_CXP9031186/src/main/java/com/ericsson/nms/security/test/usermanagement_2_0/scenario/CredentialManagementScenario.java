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

import org.openqa.selenium.firefox.FirefoxProfile;
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
import com.ericsson.oss.testware.security.authentication.operators.LoginLogoutUiOperator;
import com.ericsson.oss.testware.security.generic.utility.UiExceptionHandler;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.ericsson.oss.testware.security.sls.flows.SlsHelperFlows;
import com.ericsson.oss.testware.security.sls.teststeps.SlsHelperTestSteps;
import com.ericsson.oss.testware.security.usermanagement.flows.CredentialManagementFlow;

/**
 * Created by mborek on 7/12/16.
 */
public class CredentialManagementScenario extends TafTestBase {
    final static String USERS_CSV = "data/usermanagement_2_0/CredentialUser.csv";
    private final static String CREDENTIALS_CSV = "data/usermanagement_2_0/Credentials.csv";
    private final static String ENTITY_CSV = "data/usermanagement_2_0/CredentialPrepareEntity.csv";
    private final static String ADMIN_USER_CSV = "data/usermanagement_2_0/AdminUser.csv";
    private static final String TEST_DS = "testDataSource";
    private static final String TEST_DS_CSV = "data/usermanagement_2_0/testDataSource.csv";

    @Inject
    private TestContext context;

    @Inject
    private LoginLogoutUiFlows loginLogoutFlow;

    @Inject
    private UserManagementTestFlows userManagementTestFlows;

    @Inject
    private GimCleanupFlows gimCleanupFlows;

    @Inject
    private CredentialManagementFlow credentialManagementFlow;

    @Inject
    private SlsHelperFlows slsHelperFlows;


    @BeforeClass(groups = {"RFA"})
    public void setUp() throws TimeoutException, InterruptedException {
        final FirefoxProfile automaticDownloadFirefox = new FirefoxProfile();
        automaticDownloadFirefox.setAcceptUntrustedCertificates(true);
        automaticDownloadFirefox.setPreference("browser.helperApps.alwaysAsk.force", false);
        automaticDownloadFirefox.setPreference("browser.download.manager.showWhenStarting", false);
        automaticDownloadFirefox.setPreference("browser.download.folderList", 2); // download to default directory
        automaticDownloadFirefox.setPreference("browser.download.dir", "/tmp"); // default directory
        automaticDownloadFirefox.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/html,text/plain,application/octet-stream,application/xml,application/p12"); // don't show dialog for save
        context.setAttribute(LoginLogoutUiOperator.FIREFOX_PROFILE_FOR_AUTODOWNLOAD, automaticDownloadFirefox);

        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.shared(TafDataSources.fromCsv(USERS_CSV)));
        context.addDataSource(SlsHelperTestSteps.PREPARE_ENTITY, TafDataSources.shared(TafDataSources.fromCsv(ENTITY_CSV)));

        final TestScenario scenario = TestScenarios.scenario("Create EntityUser & Entity")
                .addFlow(userManagementTestFlows.createUser())
                .addFlow(slsHelperFlows.prepareEntity())
                .build();

        CommonUtils.start(scenario);

        context.removeDataSource(CommonDataSources.AVAILABLE_USERS);
    }


    @AfterClass(groups = {"RFA"}, alwaysRun = true)
    public void tearDown() {
        context.addDataSource(CommonDataSources.USER_TO_CLEAN_UP, TafDataSources.shared(TafDataSources.fromCsv(USERS_CSV)));
        context.addDataSource(SlsHelperTestSteps.CLEAN_ENTITY, TafDataSources.shared(TafDataSources.fromCsv(ENTITY_CSV)));

        final TestScenario scenario = TestScenarios.scenario("Clean up users check profile")
                .addFlow(gimCleanupFlows.cleanUp(EnmObjectType.USER))
                .addFlow(slsHelperFlows.cleanEntity())
                .build();

        CommonUtils.start(scenario);

        context.removeDataSource(CommonDataSources.AVAILABLE_USERS);
    }


    @Test(groups = {"Acceptance", "RFA", "GetCredentialTest"})
    @TestSuite
    public void testGetUserCredentials() throws Exception {
        context.addDataSource(CredentialManagementFlow.DATA_USERS, TafDataSources.shared(TafDataSources.fromCsv(USERS_CSV)));
        context.addDataSource(CredentialManagementFlow.DATA_CREDENTIALS, TafDataSources.shared(TafDataSources.fromCsv(CREDENTIALS_CSV)));
        context.addDataSource(CommonDataSources.AVAILABLE_USERS, TafDataSources.shared(TafDataSources.fromCsv(ADMIN_USER_CSV)));
        context.addDataSource(TEST_DS, TafDataSources.fromCsv(TEST_DS_CSV));

        final TestScenario scenario = TestScenarios.dataDrivenScenario("Generate and Download Credentials")
                .withScenarioDataSources(TestScenarios.dataSource(TEST_DS).withFilter(PredicatesExt.testCaseIdFilter("15")))
                .addFlow(credentialManagementFlow.getCredential())
                .addFlow(loginLogoutFlow.loginWithRedirect("#usermanagement"))
                .addFlow(credentialManagementFlow.revokeCertificate())
                .withExceptionHandler(new UiExceptionHandler(context))
                .build();

        CommonUtils.start(scenario);
    }

}
