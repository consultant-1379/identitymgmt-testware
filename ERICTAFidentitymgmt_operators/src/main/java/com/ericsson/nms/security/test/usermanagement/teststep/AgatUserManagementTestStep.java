/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.nms.security.test.usermanagement.teststep;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;

import static com.ericsson.nms.security.test.usermanagement.teststep.AgatUserManagementTestStep.TestParameters.CHANGE_PASSWORD_FLAG;
import static com.ericsson.nms.security.test.usermanagement.teststep.AgatUserManagementTestStep.TestParameters.WAIT_TIME_SECONDS;
import static com.ericsson.nms.security.test.usermanagement.teststep.AgatUserManagementTestStep.TestStepId.CHANGE_PASSWORD_TESTSTEP;
import static com.ericsson.nms.security.test.usermanagement.teststep.AgatUserManagementTestStep.TestStepId.WAIT_TESTSTEP;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.ericsson.oss.testware.enmbase.data.ENMUser;

public class AgatUserManagementTestStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgatUserManagementTestStep.class);

    /**
     * <pre>
     * Name:
     *      getPasswordComplexitySettingsTestStep()
     * Test Step ID:
     *      {@value com.ericsson.nms.security.test.usermanagement.teststep.AgatUserManagementTestStep.TestStepId#CHANGE_PASSWORD_TESTSTEP}
     * Description:
     *      This Test Step should be used to change (on the fly) the password of selected user.
     * </pre>
     * @param user User to change password
     * @param shouldChangePassword boolean for password changing
     * @return Changed dadtarecord
     */
    @TestStep(id = CHANGE_PASSWORD_TESTSTEP)
    public DataRecord modifyPasswordValue(@Input(AVAILABLE_USERS) final ENMUser user,
            @Input(CHANGE_PASSWORD_FLAG) final Boolean shouldChangePassword) {
        final Map<String, Object> valueMap = user.getAllFields();
        if (shouldChangePassword) {
            valueMap.put("password", generateNewPassword((String) valueMap.get("password")));
        }
        return TestDataSourceFactory.createDataRecord(valueMap);
    }

    /**
     * <pre>
     * Name:
     *      waitSomeTime()
     * Test Step ID:
     *      {@value com.ericsson.nms.security.test.usermanagement.teststep.AgatUserManagementTestStep.TestStepId#WAIT_TESTSTEP}
     * Description:
     *      This Test Step should be used to wait for {@value TestParameters#WAIT_TIME_SECONDS} seconds.
     * </pre>
     * @param waitInSeconds Amount of seconds to wait.
     */
    @TestStep(id = WAIT_TESTSTEP)
    public void waitSomeTime(@Input(WAIT_TIME_SECONDS) final Integer waitInSeconds) {
        LOGGER.debug("Sleeping for {} seconds!!", waitInSeconds);
        try {
            TimeUnit.SECONDS.sleep(waitInSeconds);
        } catch (final InterruptedException e) {
            LOGGER.warn("WARNING: sleep interrupted ({} seconds) ...\r\n{}", waitInSeconds, e);
        }
    }

    /**
     * Name: generateNewPassword()             [public]
     * Description: This method should be used to generate new password from given one: this could be useful to generate password using (more or less)
     * password rules from customer.
     *
     * @param oldPassword - Password that meet rules
     * @return - generated password
     */
    public String generateNewPassword(final String oldPassword) {
        final Random random = new Random();
        final StringBuilder passWd = new StringBuilder(oldPassword);
        char charToAdd = ' ';
        for (int charCount = 0; charCount < oldPassword.length(); charCount++) {
            final char charAtPosition = passWd.charAt(charCount);
            if (isLowerCase(charAtPosition)) {              // LowerCase alphabetical Characters
                charToAdd = (char) ('a' + random.nextInt(26));
            } else if (isUpperCase(charAtPosition)) {      // UpperCase alphabetical Characters
                charToAdd = (char) ('A' + random.nextInt(26));
            } else if (isDigit(charAtPosition)) {      // Numeric Characters
                charToAdd = (char) ('0' + random.nextInt(10));
            } else {            // Other Characters
                charToAdd = charAtPosition;
            }
            passWd.setCharAt(charCount, charToAdd);
        }
        return passWd.toString();
    }

    /**
     * <pre>
     * Class Name:
     *      TestStepId
     * Description:
     *      Class to give a name to Test Step.
     * </pre>
     */
    public static class TestStepId {
        public static final String CHANGE_PASSWORD_TESTSTEP = "changePasswordTestStep";
        public static final String WAIT_TESTSTEP = "waitTestStep";
    }

    /**
     * <pre>
     * Class Name:
     *      TestParameters
     * Description:
     *      Static Class to give a name to Test Step Parameters.
     * </pre>
     */
    public static class TestParameters {
        public static final String WAIT_TIME_SECONDS = "waitTimeInSeconds";
        public static final String CHANGE_PASSWORD_FLAG = "ChangePasswordFlag";
    }
}
