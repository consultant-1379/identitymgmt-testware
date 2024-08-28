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
 -----------------------------------------------------------------------------*/
package com.ericsson.nms.security.test.rolemanagement.scenario.utility;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.combine;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.nms.security.test.CommonUtils.start;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ROLE_TO_CLEAN_UP;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ROLE_TO_CREATE;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.ROLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.AbstractCompositeDataSource;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordModifier;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.RoleManagementTestFlows;
import com.google.common.base.Function;
import com.google.common.collect.Lists;


public class RMUtility {
    public final static Logger LOGGER = LoggerFactory.getLogger(RMUtility.class);

    @Inject
    private GimCleanupFlows gimCleanupFlows;

    @Inject
    private TestContext context;

    @Inject
    private RoleManagementTestFlows roleManagementTestFlows;

    /**
     * Performs clean up of Roles in ENM using GIM REST interface. Uses specified data file(s).
     * @param dataFileNames full paths to CSV data source file with roles to delete. CSV must contain at least one column: "name"
     */
    public void cleanUpRolesUsingDataFiles(final String... dataFileNames) {
        prepareCleanUpDataSource(dataFileNames);

        final TestScenario cleanUpScenario = scenario("Clean up")
                .addFlow(gimCleanupFlows.cleanUp(ROLE))
                .build();

        start(cleanUpScenario);
    }

    /**
     * Performs clean up and create of Roles in ENM using GIM REST interface. Uses specified data file for both clean up and create.
     * @param createAndCleanUpRolesDataFileName full path to CSV data source file with roles to clean up and create.
     */
    public void cleanUpAndCreateRolesUsingDataFiles(final String createAndCleanUpRolesDataFileName) {
        cleanUpAndCreateRolesUsingDataFiles(createAndCleanUpRolesDataFileName, createAndCleanUpRolesDataFileName);
    }

    /**
     * Performs clean up and create of Roles in ENM using GIM REST interface. Uses specified data file(s).
     * @param createRolesDataFileName full path to CSV data source file with roles to create.
     * @param cleanUpDataFileNames full path to CSV data source files with roles to clean. CSV must contain at least one column: "name"
     */
    public void cleanUpAndCreateRolesUsingDataFiles(final String createRolesDataFileName, final String... cleanUpDataFileNames) {
        final TestDataSource<DataRecord> createRolesDataSource = TafDataSources.fromCsv(createRolesDataFileName);

        context.addDataSource(ROLE_TO_CREATE, createRolesDataSource);
        prepareCleanUpDataSource(cleanUpDataFileNames);

        final TestScenario cleanUpAndCreateRoleScenario = scenario("Clean up and create test roles")
                .addFlow(gimCleanupFlows.cleanUp(ROLE))
                .addFlow(roleManagementTestFlows.createRole())
                .build();

        start(cleanUpAndCreateRoleScenario);
    }

    /**
     * Prepares data source for clean up - reverses the order of the data records in data source retrieved from specified data files.
     * @param dataFileNames full paths to CSV data source file with roles to delete. CSV must contain at least one column: "name"
     */
    private void prepareCleanUpDataSource(final String... dataFileNames) {
        final TestDataSource<DataRecord> reversedDataSource = reverse(combineFromCsv(dataFileNames));

        context.removeDataSource(ROLE_TO_CLEAN_UP);
        for (final DataRecord dataRecord : reversedDataSource) {
            LOGGER.debug("Adding role record to role clean up data source.");
            final DataRecordModifier dataRecordModifier = context.dataSource(ROLE_TO_CLEAN_UP).addRecord();
            dataRecordModifier.setFields(dataRecord);
        }
    }

    /*
     * See https://jira-nam.lmera.ericsson.se/browse/CIS-31600 to see whether the below functions:
     * combineFromCsv, toList, fromList and reverse, are included in the TAF framework.
     */
    private static TestDataSource<DataRecord> combineFromCsv(final String... locations) {
        final Function<String, TestDataSource<DataRecord>> fromCSVFunction = new Function<String, TestDataSource<DataRecord>>() {
            @Override
            public TestDataSource<DataRecord> apply(final String input) {
                return TafDataSources.fromCsv(input);
            }
        };
        final List<TestDataSource<DataRecord>> list = Lists.transform(Arrays.asList(locations), fromCSVFunction);
        return combine(list.toArray((TestDataSource<? extends DataRecord>[]) new TestDataSource[locations.length]));
    }

    private static TestDataSource<DataRecord> reverse(final TestDataSource<DataRecord> dataSource) {
        return fromList(Lists.reverse(toList(dataSource)));
    }

    private static List<DataRecord> toList(final TestDataSource<DataRecord> dataSource) {
        final List<DataRecord> recordList = new ArrayList<>();
        for (final DataRecord aDataSource : dataSource) {
            recordList.add(aDataSource);
        }
        return recordList;
    }

    private static TestDataSource<DataRecord> fromList(final List<DataRecord> recordList) {
        return new AbstractCompositeDataSource<DataRecord>() {
            @Override
            public Iterator<DataRecord> iterator() {
                return new ArrayList<>(recordList).iterator();
            }
        };
    }

    public static void dataDrivenDataSource(final String dataSourceNew, final String testId, final TestDataSource<? extends DataRecord> values) {
        final TestContext context = TafTestContext.getContext();
        final TestDataSource<DataRecord> valueNew = TestDataSourceFactory.createDataSource();
        for (final Iterator iterator = values.iterator(); iterator.hasNext();) {
            final DataRecord next = (DataRecord) iterator.next();
            valueNew.addRecord().setFields(next).setField("testCaseId", testId);
        }
        context.addDataSource(dataSourceNew, shared(valueNew));
    }

}
