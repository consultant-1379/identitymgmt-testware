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

import static com.ericsson.cifwk.taf.datasource.TafDataSources.combine;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.copy;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.filter;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;
import static com.ericsson.nms.security.test.usermanagement.AgatCommonUtility.printDataSource;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.ericsson.nms.security.test.usermanagement.LoggerMessageSet;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

/**
 * <pre>
 * Class Name: AgatScenarioUtility
 * Description: .
 * </pre>
 */
public final class AgatScenarioUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgatScenarioUtility.class);

    private AgatScenarioUtility() {
    }

    /**
     * Name: backupDataSource()             [protected - Runnable]
     * Description: .
     *
     * @param dataSourceName - DataSource to backup
     * @param filterToUse    - filter to use for filtered datasource (true if all dataRecord)
     * @return runnable Object
     */
    protected static Runnable backupDataSource(final String dataSourceName, final Predicate filterToUse) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext contextLocal = TafTestContext.getContext();
                contextLocal.addDataSource(dataSourceName + "_TEMP", copy(contextLocal.dataSource(dataSourceName)));
                contextLocal.addDataSource(dataSourceName, filter(contextLocal.dataSource(dataSourceName), filterToUse));
            }
        };
    }

    protected static Runnable backupDataSource(final String dataSourceName) {
        return backupDataSource(dataSourceName, Predicates.alwaysTrue());
    }

    protected static Runnable duplicateDataSource(final String originalDataSource, final String duplicatedDataSoure) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext contextLocal = TafTestContext.getContext();
                contextLocal.addDataSource(duplicatedDataSoure, copy(contextLocal.dataSource(originalDataSource)));
            }
        };
    }

    /**
     * Name: restoreDataSource()             [protected - Runnable]
     * Description: .
     *
     * @param dataSourceName - DataSource to restore
     * @return runnable Object
     */
    protected static Runnable restoreDataSource(final String dataSourceName) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext contextLocal = TafTestContext.getContext();
                contextLocal.addDataSource(dataSourceName, copy(contextLocal.dataSource(dataSourceName + "_TEMP")));
            }
        };
    }

    /**
     * Name: restoreCombinedDataSource()             [protected - Runnable]
     * Description: .
     *
     * @param dataSourceName - DataSource to restore
     * @return runnable Object
     */
    protected static Runnable restoreCombinedDataSource(final String dataSourceName) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext contextLocal = TafTestContext.getContext();
                LOGGER.trace("{}", printDataSource(contextLocal.dataSource(dataSourceName), "Available Users (created)"));
                LOGGER.trace("{}", printDataSource(contextLocal.dataSource(dataSourceName + "_TEMP"), "Available Users (backup)"));
                contextLocal.addDataSource(dataSourceName,
                        combine(contextLocal.dataSource(dataSourceName), contextLocal.dataSource(dataSourceName + "_TEMP")));
            }
        };
    }

    /**
     * Name: backupAndReplace()             [protected - Runnable]
     * Description: This runnable method is used to create a backup version of selected context DataSource and replacing it with a given one.
     *
     * @param dataSourceName      - DataSource to backup.
     * @param replacingDataSource Replacing DataSource
     * @return runnable method.
     */
    protected static Runnable backupAndReplace(final String dataSourceName, final TestDataSource<DataRecord> replacingDataSource) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext contextLocal = TafTestContext.getContext();
                contextLocal.addDataSource(dataSourceName + "_TEMP", copy(contextLocal.dataSource(dataSourceName)));
                contextLocal.addDataSource(dataSourceName, replacingDataSource);
            }
        };
    }

    /**
     * Name: removeDataSources()             [protected - Runnable]
     * Description: This runnable method is used to remove, from context, selected DataSources.
     *
     * @param dataSourceNames - list of DataSource to remove
     * @return runnable method.
     */
    public static Runnable removeDataSources(final String... dataSourceNames) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext contextLocal = TafTestContext.getContext();
                for (final String dataSourceName : dataSourceNames) {
                    contextLocal.removeDataSource(dataSourceName);
                }
            }
        };
    }

    /**
     * Name: hardBindTo()             [protected - Runnable]
     * Description: This runnable method is used to create a binding between DataSources.
     *
     * @param bindFromDataSource - DataSource to bind (Source)
     * @param bindToDataSource   - DataSource binded (Destination)
     * @return runnable method.
     */
    public static Runnable hardBindTo(final String bindFromDataSource, final String bindToDataSource) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext contextLocal = TafTestContext.getContext();
                contextLocal.addDataSource(bindToDataSource, shared(contextLocal.dataSource(bindFromDataSource)));
            }
        };
    }

    /**
     * Name: splitDataSourceContent()             [protected - Runnable]
     * Description: This method could be use to 'split' DataSource in two different datasource, using a predicate.
     *
     * @param originalDataSource         - DataSource to split
     * @param destinationTrueDataSource  - dataSource for datarecor that meet predicate
     * @param destinationFalseDataSource - datasource for records that doesn't meet predicate
     * @param predicateForSplit          = filter for splitting
     * @return - runnable method.
     */
    protected static Runnable splitDataSourceContent(final String originalDataSource, final String destinationTrueDataSource,
                                                     final String destinationFalseDataSource, final Predicate predicateForSplit) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext contextLocal = TafTestContext.getContext();
                LOGGER.debug("{}", LoggerMessageSet.printDataSource(contextLocal.dataSource(originalDataSource), "Original DataSource"));
                contextLocal.addDataSource(destinationTrueDataSource, shared(filter(contextLocal.dataSource(originalDataSource), predicateForSplit)));
                contextLocal.addDataSource(destinationFalseDataSource, shared(filter(contextLocal.dataSource(originalDataSource),
                        Predicates.not(predicateForSplit))));
                LOGGER.debug("{}", LoggerMessageSet.printDataSource(contextLocal.dataSource(destinationTrueDataSource), "Destination DataSource "
                        + "(TRUE)"));
                LOGGER.debug("{}", LoggerMessageSet.printDataSource(contextLocal.dataSource(destinationFalseDataSource), "Destination DataSource "
                        + "(FALSE)"));
            }
        };
    }

    /**
     * Name: replicateDataRecord()             [protected]
     * Description: This method could be use to replicate DataRecord from specific DataSource (filtered or not) and return it in result DataSorce.
     *
     * @param dataSourceName  - datasource to use
     * @param filterToUse     - predicate to filter DataRecords
     * @param replicateNumber = number of replication of DataRecord
     * @param updateUserName  - flag to select username value
     * @return resultDataSource
     */
    protected static TestDataSource<DataRecord> replicateDataRecord(final TestDataSource<DataRecord> dataSourceName, final Predicate filterToUse,
                                                                    final int replicateNumber, final boolean updateUserName) {
        final String userNameField = "username";
        final String replicateNumberField = "replicateNumber";

        final TestDataSource<DataRecord> originalDataSource = filter(dataSourceName, filterToUse);
        final Iterator<DataRecord> fieldDataSourceIterator = originalDataSource.iterator();
        final TestDataSource<DataRecord> resultDataSource = TestDataSourceFactory.createDataSource();

        while (fieldDataSourceIterator.hasNext()) {
            final DataRecord thisDataRecord = fieldDataSourceIterator.next();
            for (int replicateCounter = 1; replicateCounter <= replicateNumber; replicateCounter++) {
                final String thisUsername = updateUserName ? String.format("%s_%02d", thisDataRecord.getFieldValue(userNameField),
                        replicateCounter) : (String) thisDataRecord.getFieldValue(userNameField);
                resultDataSource.addRecord().setFields(thisDataRecord).setField(replicateNumberField, replicateCounter)
                        .setField(userNameField, thisUsername);
            }
        }
        return resultDataSource;
    }

    protected static TestDataSource<DataRecord> replicateDataRecord(final TestDataSource<DataRecord> dataSourceName,
                                                                    final Predicate filterToUse, final int replicateNumber) {
        return replicateDataRecord(dataSourceName, filterToUse, replicateNumber, false);
    }

    protected static TestDataSource<DataRecord> replicateDataRecord(final TestDataSource<DataRecord> dataSourceName, final int replicateNumber) {
        return replicateDataRecord(dataSourceName, Predicates.alwaysTrue(), replicateNumber, false);
    }

    protected static TestDataSource<DataRecord> replaceAndReplicateRecords(final TestDataSource<DataRecord> originalDataSource,
                                                                           final Predicate filterToUse, final int replicateNumber) {
        final TestDataSource<DataRecord> resultDataSource = filter(originalDataSource, Predicates.not(filterToUse));
        return combine(resultDataSource, replicateDataRecord(originalDataSource, filterToUse, replicateNumber, true));
    }

    /**
     * <pre>
     * Name: getSingleDataRecords()       [public]
     * Description: This method return a new Datasource with datarecord present only in the first DataSource, checking 'checkField' field.
     * </pre>
     *
     * @param first
     *         - Main dataSource (bigger one)
     * @param second
     *         - Datasource with a subset of DataRecords
     * @param checkField
     *         - Field to check
     *
     * @return Resulting DataSource
     */
    protected static TestDataSource<? extends DataRecord> getSingleDataRecords(final TestDataSource<DataRecord> first,
            final TestDataSource<DataRecord> second,
            final String checkField) {
        final List<Map<String, Object>> reorederedDataSource = Lists.newArrayList();
        final Iterator<DataRecord> firstDatasourceIterator = first.iterator();
        while (firstDatasourceIterator.hasNext()) {
            final DataRecord firstRecord = firstDatasourceIterator.next();
            Boolean isPresent = firstRecord.getFieldValue(checkField) == null;
            final Iterator<DataRecord> secondDatasourceIterator = second.iterator();
            while (secondDatasourceIterator.hasNext()) {
                final DataRecord secondRecord = secondDatasourceIterator.next();
                if (secondRecord.getFieldValue(checkField) != null
                        && firstRecord.getFieldValue(checkField) == secondRecord.getFieldValue(checkField)) {
                    isPresent = true;
                    break;
                }
            }
            if (!isPresent) {
                reorederedDataSource.add(firstRecord.getAllFields());
            }
        }
        return TestDataSourceFactory.createDataSource(reorederedDataSource);
    }
}
