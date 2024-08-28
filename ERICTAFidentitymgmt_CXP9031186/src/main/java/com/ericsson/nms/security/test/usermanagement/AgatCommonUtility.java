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

package com.ericsson.nms.security.test.usermanagement;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Optional;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordModifier;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public final class AgatCommonUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgatCommonUtility.class);
    private static final String SQUAREBRACKET_REMOVER = "[\\[\\]]";
    private static final String TAG_TO_SEARCH_01 = "Data value:";
    private static final String TAG_TO_REPLACE_01 = "\n\tData value:";

    private AgatCommonUtility() {
    }

    /**
     * <pre>
     * Name: printDataSource()       [public]
     * Description: This method return a formatted String with content of selected DataSource. This string could be use to print content of
     *              DataSource records.
     * </pre>
     *
     * @param dataSource
     *         DataSource object (TestDataSource )
     * @param dataSourceName
     *         Name of selected DataSource
     *
     * @return String with formatted DataSource List
     */
    public static String printDataSource(final Iterable<DataRecord> dataSource, final String dataSourceName) {
        return String.format("%n%n -- Print DataSource Content: <%s> %s%n", dataSourceName,
                Iterables.toString(dataSource).replaceAll(SQUAREBRACKET_REMOVER, "").replaceAll(TAG_TO_SEARCH_01, TAG_TO_REPLACE_01));
    }

    /**
     * <p>
     * <b>Description</b>: This <i>private</i> method could be use to print TestCaseId and TestCaseTitle in LOG. It get these informations from
     * annotation on top of TestCase selected.
     * </p>
     *
     * @param thisClass
     *         - class where TestCase is Stored
     * @param methodTestName
     *         - method of TestCase
     * @param logger
     *         - logger used for print LOG content.
     */
    public static void printTestHeader(final Class thisClass, final String methodTestName, final Logger logger) {
        final String testCaseId = getTestId(thisClass, methodTestName);
        final String testCaseTitle = getTestTitle(thisClass, methodTestName);
        final String cr = "\n";
        final String tab = "\t";
        final String border = "||";

        logger.info(cr + border + "================================================================================" + cr + border + tab
                + "Executing Test Case: ID = <{}>, TITLE: <{}>" + cr + border
                + "================================================================================", testCaseId, testCaseTitle);
    }

    public static String getTestId(final Class thisClass, final String methodName) {
        String returnValue = null;
        try {
            returnValue = thisClass.getMethod(methodName).getAnnotation(TestId.class).id();
        } catch (final NoSuchMethodException e) {
            LOGGER.error("ERROR !! - Get TestId failed." + "\n" + "Strack Trace:\n {}", e.getStackTrace());
        }
        return returnValue;
    }

    public static String getTestTitle(final Class thisClass, final String methodName) {
        String returnValue = null;
        try {
            returnValue = thisClass.getMethod(methodName).getAnnotation(TestId.class).title();
        } catch (final NoSuchMethodException e) {
            LOGGER.error("ERROR !! - Get Test Title failed." + "\n" + "Strack Trace:\n {}", e.getStackTrace());
        }
        return returnValue;
    }

    public static TestDataSource<DataRecord> resizeDataSource(final TestDataSource<DataRecord> dataSourceToGrown, final int recordCount) {
        final List<Map<String, Object>> growingDataSource = Lists.newArrayList();
        final Iterator<DataRecord> dataRecordIterator = Iterables.cycle(dataSourceToGrown).iterator();
        for (int recordCounter = 1; recordCounter <= recordCount; recordCounter++) {
            growingDataSource.add(dataRecordIterator.next().getAllFields());
        }
        return TestDataSourceFactory.createDataSource(growingDataSource);
    }

    /**
     * <pre>
     * Name: waitTime()
     * Description: this method introduce a Wait time of 'waitTime' seconds .
     * </pre>
     *
     * @param waitTime
     *         - Number of sseconds to wait
     *
     * @return A teststep flow
     */
    public static TestStepFlow waitTime(final Integer waitTime) {
        return flow("Wait Flow").pause(waitTime, TimeUnit.SECONDS).build();
    }

    /**
     * <pre>
     * Name: removeDuplicatedDatarecord()       [public]
     * Description: This method return a new Datasource with only one record for each selected field value (no duplicated field values).
     * </pre>
     *
     * @param originalDataSource
     *         - Datasource to elaborate
     * @param checkingField
     *         - not repetitive field value
     *
     * @return Elaborated DataSource.
     */
    public TestDataSource<? extends DataRecord> removeDuplicatedDatarecord(final TestDataSource<DataRecord> originalDataSource,
            final String checkingField) {
        final List<String> keywordListValues = null;
        final List<Map<String, Object>> reorederedDataSource = Lists.newArrayList();
        final Iterator<DataRecord> orignalDatasourceIterator = originalDataSource.iterator();
        while (orignalDatasourceIterator.hasNext()) {
            final DataRecord originalRecord = orignalDatasourceIterator.next();
            if (keywordListValues == null || !keywordListValues.contains(originalRecord.getFieldValue(checkingField))) {
                keywordListValues.add((String) originalRecord.getFieldValue(checkingField));
                reorederedDataSource.add(originalRecord.getAllFields());
            }
        }
        return TestDataSourceFactory.createDataSource(reorederedDataSource);
    }

    /**
     * <pre>
     * Name: cleanUnchangedFieldRunnable()       [public]
     * Description: This runnable method get two Datasources and remove (null in related field) column with same value: comparison result will be
     * put in third DataSource. Optionally you can define a list of column named that shouldn't remove from returned DataSource.
     * </pre>
     *
     * @param dataSourceOne
     *         - First DataSource (The one with all Column)
     * @param dataSourceTwo
     *         - Secon DataSource
     * @param destinationDataSource
     *         - destination Context DataSource
     * @param untachableField
     *         List of fields that should't remove from destination DataSource.
     *
     * @return - Runnable
     */
    public static Runnable cleanUnchangedFieldRunnable(final String dataSourceOne, final String dataSourceTwo, final String destinationDataSource,
            @Optional final String... untachableField) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext context = TafTestContext.getContext();
                final TestDataSource<DataRecord> one = context.dataSource(dataSourceOne);
                final TestDataSource<DataRecord> two = context.dataSource(dataSourceTwo);
                final TestDataSource<DataRecord> destination = TestDataSourceFactory.createDataSource();
                final Iterator twoIterator = two.iterator();
                final Iterator oneIterator = one.iterator();

                // Loop through DataRecords
                int recordCounter = 1;
                while (oneIterator.hasNext() && twoIterator.hasNext()) {
                    final DataRecord oneElement = (DataRecord) oneIterator.next();
                    final DataRecord twoElement = (DataRecord) twoIterator.next();
                    AgatCommonUtility.LOGGER.debug("{})DataRecord [One]: {}", recordCounter, oneElement.toString());
                    AgatCommonUtility.LOGGER.debug("{})DataRecord [Two]: {}", recordCounter++, twoElement.toString());
                    final Iterator fieldIterator = oneElement.getAllFields().entrySet().iterator();
                    final DataRecordModifier dataRecordModifier = destination.addRecord();

                    // Loop through Fields
                    int fieldCounter = 1;
                    while (fieldIterator.hasNext()) {
                        final Map.Entry oneField = (Map.Entry) fieldIterator.next();
                        final String key = (String) oneField.getKey();
                        if ((untachableField != null && Arrays.asList(untachableField).contains(key)) || oneField.getValue() != twoElement
                                .getFieldValue(key)) {
                            AgatCommonUtility.LOGGER.debug("\n\t{})Fields are different: {} <> {} \n\t\tor field is untachable: {}", fieldCounter++,
                                    oneElement.getFieldValue(key), twoElement.getFieldValue(key),
                                    (untachableField == null) ? "false" : Arrays.toString(untachableField));
                            dataRecordModifier.setField(key, oneField.getValue());
                        }
                    }
                }
                context.addDataSource(destinationDataSource, destination);
            }
        };
    }

    /**
     * <pre>
     * Name: mergeDataSource()       [public]
     * Description: This runnable method merge two DataSources and put result in Context as 'destinationDataSource' datasource.
     * </pre>
     *
     * @param dataSourceOne
     *         - First DataSource
     * @param dataSourceTwo
     *         - Secon DataSource
     * @param destinationDataSource
     *         - destination Context DataSource
     */
    public static void mergeDataSource(final String dataSourceOne, final String dataSourceTwo, final String destinationDataSource) {
        final TestContext context = TafTestContext.getContext();
        context.addDataSource(destinationDataSource, TafDataSources.merge(context.dataSource(dataSourceOne), context.dataSource(dataSourceTwo)));
    }

    /**
     * <pre>
     * Name: mergeDataSourceRunnable()       [public]
     * Description: This runnable method merge two DataSources and put result in Context as 'destinationDataSource' datasource.
     * </pre>
     *
     * @param dataSourceOne
     *         - First DataSource
     * @param dataSourceTwo
     *         - Secon DataSource
     * @param destinationDataSource
     *         - destination Context DataSource
     *
     * @return - Runnable
     */
    public static Runnable mergeDataSourceRunnable(final String dataSourceOne, final String dataSourceTwo, final String destinationDataSource) {
        return new Runnable() {
            @Override
            public void run() {
                mergeDataSource(dataSourceOne, dataSourceTwo, destinationDataSource);
            }
        };
    }

    /**
     * <pre>
     * Name: printDataSourceRunnable()       [public]
     * Description: This runnable method could be use to print content of dataSources in 'before' or 'after' test flow environment (Runnable version).
     * </pre>
     *
     * @param dataSourceToPrint
     *         Name of DataSource to print (DataSource should e stored in context)
     * @param log
     *         Logger used to show DataSource Content
     *
     * @return Runnable object.
     */
    public static Runnable printDataSourceRunnable(final String dataSourceToPrint, final Logger log) {
        return new Runnable() {
            @Override
            public void run() {
                final TestContext context = TafTestContext.getContext();
                log.debug("{}", printDataSource(context.dataSource(dataSourceToPrint), dataSourceToPrint));
            }
        };
    }

    /**
     * <pre>
     * Name: beforeDeleteUsers()
     * Description: Prepare DataSource for Delete operation (USERS_TO_DELETE in not yet available).
     * </pre>
     *
     * @param targetDataSourceName
     *         datasource in output
     * @param sourceDataSourceName
     *         datasource in input
     *
     * @return runnable
     */
    public static Runnable replaceDataSourceRunnable(final String targetDataSourceName, final String sourceDataSourceName) {
        return new Runnable() {
            @Override
            public void run() {
                TafTestContext.getContext().removeDataSource(targetDataSourceName);
                TafTestContext.getContext().addDataSource(targetDataSourceName, TafTestContext.getContext().dataSource(sourceDataSourceName));
            }
        };
    }

    /**
     * <pre>
     * Name: shareDataSourceRunnable()
     * Description: Make selected DataSource shared.
     * </pre>
     *
     * @param targetDataSourceName
     *         datasource in output
     *
     * @return runnable
     */
    public static Runnable shareDataSourceRunnable(final String targetDataSourceName) {
        return new Runnable() {
            @Override
            public void run() {
                TafDataSources.shareDataSource(targetDataSourceName);
            }
        };
    }

    /**
     * <pre>
     * Name: removeDataSourceRunnable()
     * Description: Remove selected DataSource from Context.
     * </pre>
     *
     * @param targetDataSourceName
     *         datasource in output
     *
     * @return runnable
     */
    public static Runnable removeDataSourceRunnable(final String targetDataSourceName) {
        return new Runnable() {
            @Override
            public void run() {
                TafTestContext.getContext().removeDataSource(targetDataSourceName);
            }
        };
    }
}
