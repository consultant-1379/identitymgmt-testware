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

package com.ericsson.nms.security.test.performance.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.DataSource;

public class RolesToCreate {
    private static final Logger LOGGER = LoggerFactory.getLogger(RolesToCreate.class);
    private static final int NUMBER_OF_COM_ALIAS_ROLES = Integer.parseInt(System.getProperties().getProperty("NUMBER_OF_COM_ALIAS_ROLES", "300"));
    private static final int NUMBER_OF_CUSTOM_ROLES = Integer.parseInt(System.getProperties().getProperty("NUMBER_OF_CUSTOM_ROLES", "300"));
    private static int NUMBER_OF_COM_ROLES = Integer.parseInt(System.getProperties().getProperty("NUMBER_OF_COM_ROLES", "300"));

    @DataSource
    public List<Map<String, Object>> getExpectedData() {
        if (NUMBER_OF_COM_ROLES == 0 && (NUMBER_OF_COM_ALIAS_ROLES != 0 || NUMBER_OF_CUSTOM_ROLES != 0)) {
            NUMBER_OF_COM_ROLES = 3;
        }

        final List<Map<String, Object>> data = new ArrayList<>();
        LOGGER.info("NUMBER_OF_COM_ROLES to create: " + NUMBER_OF_COM_ROLES);
        LOGGER.info("NUMBER_OF_COM_ALIAS_ROLES to create: " + NUMBER_OF_COM_ALIAS_ROLES);
        LOGGER.info("NUMBER_OF_CUSTOM_ROLES to create: " + NUMBER_OF_CUSTOM_ROLES);

        for (int i = 1; i <= NUMBER_OF_COM_ROLES; i++) {
            final Map<String, Object> role = new ConcurrentHashMap<>();

            role.put("type", "com");
            role.put("name", "comrole" + i);
            role.put("description", "comrole" + i + " description");
            role.put("status", "ENABLED");
            role.put("policy", "");
            role.put("assignedRoles", "");

            data.add(role);
        }
        for (int i = 1; i <= NUMBER_OF_COM_ALIAS_ROLES; i++) {
            final Map<String, Object> role = new ConcurrentHashMap();

            role.put("type", "comalias");
            role.put("name", "comaliasrole" + i);
            role.put("description", "comaliasrole" + i + " description");
            role.put("status", "ENABLED");
            role.put("policy", "");
            role.put("assignedRoles", "comrole1|comrole2|comrole3");

            data.add(role);
        }
        for (int i = 1; i <= NUMBER_OF_CUSTOM_ROLES; i++) {
            final Map<String, Object> role = new ConcurrentHashMap();

            role.put("type", "custom");
            role.put("name", "customrole" + i);
            role.put("description", "customrole" + i + " description");
            role.put("status", "ENABLED");
            role.put("policy", "");
            role.put("assignedRoles", "comrole1|comrole2|comrole3");

            data.add(role);
        }

        return data;
    }
}
