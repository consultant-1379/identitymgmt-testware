/*
 * ------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------
 */

package com.ericsson.nms.security.test.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.nms.security.test.usermanagement.LoggerMessageSet;
import com.google.common.collect.Iterables;

/**
 * <pre>
 * Class Name: AgatUserToCreate
 * Description: This class contain methods to Create datasource for 'UserToCreate', getting information from remote/tdm/local (as profile setting)
 *              and generating datarecords suitable for test cases.
 * </pre>
 */
public class AgatUserToCreate {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgatUserToCreate.class);

    private static final String USERSTOCREATE_TEMP = "usersToCreateTemp";

    @DataSource
    public List<Map<String, Object>> createUsers() {
        LOGGER.info("\n\n | Creating USERS from first DataRecord of <{}> DataSource with correct field '{}' for Testware execution\n",
                USERSTOCREATE_TEMP, UserFields.ROLES);
        final List<Map<String, Object>> usersToCreate = new ArrayList<>();

        // Getting DataSource from DataProvider
        final TestDataSource<DataRecord> userList = TafDataSources.fromTafDataProvider(USERSTOCREATE_TEMP);
        LOGGER.debug("{}", LoggerMessageSet.printDataSource(userList, "From DataProvider: " + USERSTOCREATE_TEMP));

        // Getting First DataRecord
        final DataRecord firstUser = Iterables.getFirst(userList, null);

        // Create Administrator User
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put(UserFields.USERNAME, firstUser.getFieldValue(UserFields.USERNAME) + "_AgatAdmin");
        tempMap.put(UserFields.ROLES, Arrays.asList("ADMINISTRATOR").toArray(new String[0]));
        usersToCreate.add(updateUserData(firstUser, tempMap));

        // Create Security_Admin User
        tempMap = new HashMap<>();
        tempMap.put(UserFields.USERNAME, firstUser.getFieldValue(UserFields.USERNAME) + "_AgatSecurityAdmin");
        tempMap.put(UserFields.ROLES, Arrays.asList("SECURITY_ADMIN").toArray(new String[0]));
        usersToCreate.add(updateUserData(firstUser, tempMap));

        // Create Security_Admin User
        tempMap = new HashMap<>();
        tempMap.put(UserFields.USERNAME, firstUser.getFieldValue(UserFields.USERNAME) + "_AgatDefaultUser");
        tempMap.put(UserFields.ROLES, Arrays.asList("OPERATOR").toArray(new String[0]));
        usersToCreate.add(updateUserData(firstUser, tempMap));

        // Create User for Ageing Test
        tempMap = new HashMap<>();
        tempMap.put(UserFields.USERNAME, firstUser.getFieldValue(UserFields.USERNAME) + "_AgatAgeingUser");
        tempMap.put(UserFields.ROLES, Arrays.asList("OPERATOR").toArray(new String[0]));
        tempMap.put(UserFields.CUSTOMIZEDPASSWORDAGEINGENABLE, "true");
        tempMap.put(UserFields.PASSWORDAGEINGENABLE, "false");
        tempMap.put(UserFields.PWDMAXAGE, 0);
        tempMap.put(UserFields.PWDEXPIREWARNING, 0);
        tempMap.put(UserFields.GRACELOGINCOUNT, 0);
        tempMap.put(UserFields.PASSWORDRESETFLAG, "false");
        usersToCreate.add(updateUserData(firstUser, tempMap));

        // Create Users for Import Test Case (Update existing Users from XML file (4 User Profile)
        tempMap = new HashMap<>();
        tempMap.put(UserFields.USERNAME, firstUser.getFieldValue(UserFields.USERNAME) + "_AgatUpdatingUser_01");
        // tempMap.put(UserFields.ROLES, Arrays.asList("Cmedit_Operator").toArray(new String[0]));
        final List roles = Arrays.asList("Cmedit_Operator,Credm_Operator,Amos_Operator,FM_Operator,FMX_Operator,PM_Operator,Shm_Operator".split(","));
        tempMap.put(UserFields.ROLES, roles.toArray(new String[roles.size()]));
        tempMap.put(UserFields.FIRSTNAME, "AgatUpdatingUser-01_FirstName");
        tempMap.put(UserFields.LASTNAME, "AgatUpdatingUser-01_LastName");
        tempMap.put(UserFields.EMAIL, "agatupdatinguser-01_firstname.agatupdatinguser-01_lastname@agat.email.test");
        tempMap.put(UserFields.DESCRIPTION, "User imported from XML file");
        tempMap.put(UserFields.SPECIAL_ACTION, "update");
        usersToCreate.add(updateUserData(firstUser, tempMap));

        tempMap = new HashMap<>();
        tempMap.put(UserFields.USERNAME, firstUser.getFieldValue(UserFields.USERNAME) + "_AgatUpdatingUser_02");
        tempMap.put(UserFields.ROLES, Arrays.asList("Cmedit_Operator").toArray(new String[0]));
        tempMap.put(UserFields.FIRSTNAME, "AgatUpdatingUser-02_FirstName");
        tempMap.put(UserFields.LASTNAME, "AgatUpdatingUser-02_LastName");
        tempMap.put(UserFields.EMAIL, "agatupdatinguser-02_firstname.agatupdatinguser-02_lastname@agat.email.test");
        tempMap.put(UserFields.DESCRIPTION, "User imported from XML file");
        tempMap.put(UserFields.SPECIAL_ACTION, "update");
        usersToCreate.add(updateUserData(firstUser, tempMap));

        tempMap = new HashMap<>();
        tempMap.put(UserFields.USERNAME, firstUser.getFieldValue(UserFields.USERNAME) + "_AgatUpdatingUser_03");
        tempMap.put(UserFields.ROLES, Arrays.asList("Credm_Operator").toArray(new String[0]));
        tempMap.put(UserFields.FIRSTNAME, "AgatUpdatingUser-03_FirstName");
        tempMap.put(UserFields.LASTNAME, "AgatUpdatingUser-03_LastName");
        tempMap.put(UserFields.EMAIL, "agatupdatinguser-03_firstname.agatupdatinguser-03_lastname@agat.email.test");
        tempMap.put(UserFields.DESCRIPTION, "User imported from XML file");
        tempMap.put(UserFields.SPECIAL_ACTION, "update");
        usersToCreate.add(updateUserData(firstUser, tempMap));

        tempMap = new HashMap<>();
        tempMap.put(UserFields.USERNAME, firstUser.getFieldValue(UserFields.USERNAME) + "_AgatUpdatingUser_04");
        tempMap.put(UserFields.ROLES, Arrays.asList("Amos_Operator").toArray(new String[0]));
        tempMap.put(UserFields.FIRSTNAME, "AgatUpdatingUser-04_FirstName");
        tempMap.put(UserFields.LASTNAME, "AgatUpdatingUser-04_LastName");
        tempMap.put(UserFields.EMAIL, "agatupdatinguser-04_firstname.agatupdatinguser-041_lastname@agat.email.test");
        tempMap.put(UserFields.CUSTOMIZEDPASSWORDAGEINGENABLE, "true");
        tempMap.put(UserFields.PASSWORDAGEINGENABLE, "false");
        tempMap.put(UserFields.PWDMAXAGE, 0);
        tempMap.put(UserFields.PWDEXPIREWARNING, 0);
        tempMap.put(UserFields.GRACELOGINCOUNT, 0);
        tempMap.put(UserFields.PASSWORDRESETFLAG, "false");
        tempMap.put(UserFields.DESCRIPTION, "User imported from XML file");
        tempMap.put(UserFields.SPECIAL_ACTION, "update");
        usersToCreate.add(updateUserData(firstUser, tempMap));

        return usersToCreate;
    }

    private Map<String, Object> updateUserData(final DataRecord firstUser, final Map<String, Object> mapList) {
        final Map<String, Object> mapObject = firstUser.getAllFields();

        // Replacing fields from input Parameters
        for (final String singleKey : mapList.keySet()) {
            mapObject.put(singleKey, mapList.get(singleKey));
        }
        return mapObject;
    }

    private final class UserFields {
        static final String USERNAME = "username";
        static final String PASSWORD = "password";
        static final String FIRSTNAME = "firstName";
        static final String LASTNAME = "lastName";
        static final String EMAIL = "email";
        static final String ROLES = "roles";
        static final String ENABLED = "enabled";
        static final String DESCRIPTION = "description";
        static final String CUSTOMIZEDPASSWORDAGEINGENABLE = "customizedPasswordAgeingEnable";
        static final String PASSWORDAGEINGENABLE = "passwordAgeingEnable";
        static final String PWDMAXAGE = "pwdMaxAge";
        static final String PWDEXPIREWARNING = "pwdExpireWarning";
        static final String GRACELOGINCOUNT = "graceLoginCount";
        static final String PASSWORDRESETFLAG = "passwordResetFlag";
        static final String SPECIAL_ACTION = "specialAction";

    }
}
