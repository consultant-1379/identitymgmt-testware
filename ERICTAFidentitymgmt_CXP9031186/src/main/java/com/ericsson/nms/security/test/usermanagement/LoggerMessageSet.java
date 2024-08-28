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

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.google.common.collect.Iterables;

/**
 * Class Name: LoggerMessageSet
 * Description: This class should be use to create Standard LOG output messages:
 * Section message: usually for main testware section (setup/Teardown/testcases)
 * Operation message: usually to describe subSection operation.
 */
public final class LoggerMessageSet {
    static final char LF = '\n';
    static final char CR = '\r';
    static final char TAB = '\t';
    static final char FRAME_CHAR = '#';
    static final int FRAME_WIDTH = 80;
    static final char NULL_CHARACTER = '\0';
    static final String SQUAREBRACKET_REMOVER = "[\\[\\]]";
    static final String TAG_TO_SEARCH_01 = "Data value:";
    static final String TAG_TO_REPLACE_01 = "\n\tData value:";

    private LoggerMessageSet() {
    }

    /**
     * Name: getSection()    [public]
     * Description: This method return a standard format for Section Log section.
     *
     * @param text - Text to show in Section Log
     * @return Formatted Text
     */
    public static String getSection(final String text) {
        final StringBuilder frameBuilder = new StringBuilder();
        frameBuilder.append(LF).append(LF).append(new String(new char[FRAME_WIDTH]).replace(NULL_CHARACTER, FRAME_CHAR)).append(multiLine(text))
                .append(LF).append(new String(new char[FRAME_WIDTH]).replace(NULL_CHARACTER, FRAME_CHAR)).append(LF);
        return frameBuilder.toString();
    }

    /**
     * Name: getOperation()    [public]
     * Description: This method return a standard format for Operation Log section.
     *
     * @param text - Text to show in Section Log
     * @return Formatted Text
     */
    public static String getOperation(final String text) {
        final StringBuilder frameBuilder = new StringBuilder();
        frameBuilder.append(LF).append(multiLine(text)).append(LF);
        return frameBuilder.toString();
    }

    public static char getLF() {
        return LF;
    }

    public static char getCR() {
        return CR;
    }

    public static char getTAB() {
        return TAB;
    }

    /**
     * <pre>
     * Name: printDataSource()       [public]
     * Description: This method return a formatted String with content of selected DataSource. This string could be use to print content of
     *              DataSource records.
     * </pre>
     *
     * @param dataSource - DataSource object (TestDataSource )
     * @param dataSourceName -  Name of selected DataSource
     * @return - String with formatted DataSource List
     */
    public static String printDataSource(final Iterable<DataRecord> dataSource, final String dataSourceName) {
        final StringBuilder frameBuilder = new StringBuilder();
        frameBuilder.append(LF).append(LF).append(String.format("# Print DataSource Content: <%s>", dataSourceName));
        frameBuilder.append(multiLine(String.format("%s", Iterables.toString(dataSource)
                .replaceAll(SQUAREBRACKET_REMOVER, "").replaceAll(TAG_TO_SEARCH_01, TAG_TO_REPLACE_01))));
        frameBuilder.append(LF);
        return frameBuilder.toString();
    }

    private static String multiLine(final String text) {
        final StringBuilder frameBuilder = new StringBuilder();
        final String[] multiLine = text.split(CR + "?" + LF);
        for (final String oneLine : multiLine) {
            frameBuilder.append(LF).append(FRAME_CHAR).append(TAB).append(oneLine);
        }
        return frameBuilder.toString();
    }

}

