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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * UsersToCreateDataSource class for users creation data.
 */
public class UsersToCreateTimeStampDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersToCreateTimeStampDataSource.class);
    private static final String USER_PATH_TEMP = "usersToCreateTemp";
    private static final String ROLES = "roles";
    private static final String USERNAME = "username";

    private static final String USRMNG_FISTNAME = "%sFN";
    private static final String USRMNG_LASTNAME = "%sLN";
    private static final String USRMNG_EMAIL = "%s@Agat.com";

    /**
     * Input for user to create DataSource.
     *
     * @return input for TestDataSource class
     */
    @DataSource
    public List<Map<String, Object>> createUser() {
        final List<Map<String, Object>> result = new ArrayList<>();
        final TestDataSource<DataRecord> userList = TafDataSources.fromTafDataProvider(USER_PATH_TEMP);
        final DataRecord firstUser = Iterables.getFirst(userList, null);
        final String passwd = firstUser.getFieldValue("password");
        //ADMINISTRATOR
        String username = "adminAgat";
        result.add(getUser(username, passwd, String.format(USRMNG_FISTNAME, username), String.format(USRMNG_LASTNAME, username),
                String.format(USRMNG_EMAIL, username), true, Arrays.asList("ADMINISTRATOR").toArray(new String[0])));
        //SECURITY_ADMIN
        username = "securityAdmAgat";
        result.add(getUser(username, passwd, String.format(USRMNG_FISTNAME, username), String.format(USRMNG_LASTNAME, username),
                String.format(USRMNG_EMAIL, username), true, Arrays.asList("SECURITY_ADMIN").toArray(new String[0])));
        //USER
        username = "userAgat";
        final List<String> rolesList = new ArrayList<String>();
        if (firstUser.getFieldValue(ROLES) instanceof String[]) {
            final String[] userRoles = firstUser.getFieldValue(ROLES);
            rolesList.addAll(Arrays.asList(userRoles));
        } else if (firstUser.getFieldValue(ROLES) instanceof String) {
            final String userRoles = firstUser.getFieldValue(ROLES);
            rolesList.addAll(Arrays.asList(userRoles.split(",")));
        }
        rolesList.remove("ADMINISTRATOR");
        rolesList.remove("SECURITY_ADMIN");
        final String[] newRoles = rolesList.toArray(new String[0]);
        result.add(getUser(username, passwd, String.format(USRMNG_FISTNAME, username), String.format(USRMNG_LASTNAME, username),
                String.format(USRMNG_EMAIL, username), true, newRoles));
        //USER AGEING
        username = "userAgeingAgat";
        final Map<String, Object> ageingUser = getUser(username, passwd, String.format(USRMNG_FISTNAME, username),
                String.format(USRMNG_LASTNAME, username), String.format(USRMNG_EMAIL, username), true,
                Arrays.asList("OPERATOR").toArray(new String[0]));
        ageingUser.put("customizedPasswordAgeingEnable", "true");
        ageingUser.put("passwordAgeingEnable", "false");
        ageingUser.put("pwdMaxAge", 0);
        ageingUser.put("pwdExpireWarning", 0);
        ageingUser.put("graceLoginCount", 0);
        ageingUser.put("passwordResetFlag", "false");
        result.add(ageingUser);
        return result;
    }

    /**
     * Returns the user.
     *
     * @param username
     *         the username
     * @param password
     *         the password
     * @param firstName
     *         the first name
     * @param lastName
     *         the last name
     * @param email
     *         the email
     * @param enabled
     *         user enable state
     * @param roles
     *         the user access rights
     *
     * @return the user
     */
    private Map<String, Object> getUser(final String username, final String password, final String firstName, final String lastName,
            final String email, final boolean enabled, final String[] roles) {
        final Map<String, Object> user = Maps.newHashMap();
        user.put(USERNAME, username);
        user.put("password", password);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("email", email);
        user.put(ROLES, roles);
        user.put("enabled", enabled);
        return user;
    }
}
