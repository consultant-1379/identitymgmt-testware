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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.DataSource;

/**
 * This is a DataSource for methods which select users from an user list.
 */
public class TargetGroupsToCreate {
    private static final Logger LOGGER = LoggerFactory.getLogger(TargetGroupsToCreate.class);
    private static final int NUMBER_OF_TGS = Integer.parseInt(System.getProperties().getProperty("NUMBER_OF_TGS", "300"));

    @DataSource
    public List<Map<String, Object>> getExpectedData() {
        final List<Map<String, Object>> data = new ArrayList<>();
        LOGGER.info("NUMBER_OF_TGS to create: " + NUMBER_OF_TGS);
        for (int i = 1; i <= NUMBER_OF_TGS; i++) {
            final Map<String, Object> tg = new HashMap<>();

            tg.put("targetGroupName", "tg" + i);
            tg.put("targetGroupDescription", "tg" + i + " description");

            data.add(tg);
        }

        return data;
    }
}
