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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import com.ericsson.cifwk.taf.data.DataHandler;

/**
 * <pre>
 * Class Name: TestReportObject
 * Description: This class could be used to generate a formatted report of Suite execution, with Passed. Failed and Skipped configuration and
 *              TestCases. You can Customize [TBD] report format with SuiteReportObjectBuilder, changing order, frame, section visualization, etc.
 * </pre>
 *
 * @author Marco Ippolito - email: marco.ippolito@ericsson.com
 * @version 0.1
 */
public class TestReportObject implements ISuiteListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestReportObject.class);

    // Get number of Stack trace element to display:
    private static final Integer stackTraceElements = DataHandler.getConfiguration().getProperty("stacktrace.element.count", 5, Integer.class);

    // Constants used in this class
    private static final String CR = "\n";
    private static final String TAB = "\t";
    private static final char NULL_CHARACTER = '\0';
    private static final int DEFAULT_HORIZONTAL_WIDTH = 80;
    private static final char DEFAULT_FRAME_HORIZONTAL_CHARACTER = '*';
    private static final int DEFAULT_VERTICAL_WIDTH = 1;
    private static final char DEFAULT_FRAME_VERTICAL_CHARACTER = '|';
    private static final String DEFAULT_FRAME_TITLE = "  Scenario Test Result  ";
    private static final String DEFAULT_SUITE_DESCRIPTION = "Suite Name: ";
    private static final char DEFAULT_SUITE_SEPARATOR = ' ';
    private static final String DEFAULT_TESTTIME_TITLE = "Executing Time (only Test time):  ";
    private static final String DEFAULT_CONF_SECTION_DESCRIPTION = "** Configuration Section **";
    private static final String DEFAULT_CONF_PASS_SECTION_DESCRIPTION = "  PASSED metods:";
    private static final String DEFAULT_CONF_FAIL_SECTION_DESCRIPTION = "  FAILED metods:";
    private static final String DEFAULT_CONF_SKIP_SECTION_DESCRIPTION = "  SKIPPED metods:";
    private static final String DEFAULT_TEST_SECTION_DESCRIPTION = "** Test Section **";
    private static final String DEFAULT_TEST_PASS_SECTION_DESCRIPTION = "  PASSED metods:";
    private static final String DEFAULT_TEST_FAIL_SECTION_DESCRIPTION = "  FAILED metods:";
    private static final String DEFAULT_TEST_SKIP_SECTION_DESCRIPTION = "  SKIPPED metods:";
    private static final String SHOWFIELD_TABLE_FORMAT = "%-35s => %s";

    // Fields used to create suite report (frame formatter and section setup)
    Boolean showExecutionTime;
    TimeFormat executionTimeFormat;
    Boolean showSetupPassed;
    Boolean showSetupFailed;
    Boolean showSetupSkipped;
    Boolean showTestPassed;
    Boolean showTestFailed;
    Boolean showTestSkipped;

    // Frame Configuration Fields
    Boolean showTopFrame;
    String topFrame;
    Boolean showBottomFrame;
    String bottomFrame;
    Boolean showLeftFrame;
    String leftFrame;
    Boolean showRightFrame;
    String rightFrame;
    String suiteSeparator;

    // Description Field configuration
    String frameTitle;
    String suiteNameTitle;
    String executeTimeTitle;
    String sectionConfigurationTitle;
    String subSectionConfigurationPassedTitle;
    String subSectionConfigurationFailedTitle;
    String subSectionConfigurationSkippedTitle;
    String sectionTestCasesTitle;
    String subSectionTestCasePassedTitle;
    String subSectionTestCaseFailedTitle;
    String subSectionTestCaseSkippedTitle;

    // Method information Fields
    Boolean showMethodTestId;
    Boolean showMethodTestTitle;

    /**
     * <pre>
     * Name: TestReportObject()    [public]
     * Description: Using this constructor, we fill instance fields with default values.
     * </pre>
     */
    public TestReportObject() {
        // Fields used to create suite report (frame formatter and section setup)
        this.showExecutionTime = true;
        this.executionTimeFormat = TimeFormat.ELAPSED_HHMMSSMMM;
        this.showSetupPassed = true;
        this.showSetupFailed = true;
        this.showSetupSkipped = true;
        this.showTestPassed = true;
        this.showTestFailed = true;
        this.showTestSkipped = true;

        // Frame Configuration Fields
        this.showTopFrame = true;
        this.topFrame = String.valueOf(DEFAULT_FRAME_HORIZONTAL_CHARACTER);
        this.showBottomFrame = true;
        this.bottomFrame = String.valueOf(DEFAULT_FRAME_HORIZONTAL_CHARACTER);
        this.showLeftFrame = true;
        this.leftFrame = new String(new char[DEFAULT_VERTICAL_WIDTH]).replace(NULL_CHARACTER, DEFAULT_FRAME_VERTICAL_CHARACTER);
        this.showRightFrame = false;
        this.rightFrame = null;
        this.suiteSeparator = String.valueOf(DEFAULT_SUITE_SEPARATOR);

        // Description Field configuration
        this.frameTitle = DEFAULT_FRAME_TITLE;
        this.suiteNameTitle = DEFAULT_SUITE_DESCRIPTION;
        this.executeTimeTitle = DEFAULT_TESTTIME_TITLE;
        this.sectionConfigurationTitle = DEFAULT_CONF_SECTION_DESCRIPTION;
        this.subSectionConfigurationPassedTitle = DEFAULT_CONF_PASS_SECTION_DESCRIPTION;
        this.subSectionConfigurationFailedTitle = DEFAULT_CONF_FAIL_SECTION_DESCRIPTION;
        this.subSectionConfigurationSkippedTitle = DEFAULT_CONF_SKIP_SECTION_DESCRIPTION;
        this.sectionTestCasesTitle = DEFAULT_TEST_SECTION_DESCRIPTION;
        this.subSectionTestCasePassedTitle = DEFAULT_TEST_PASS_SECTION_DESCRIPTION;
        this.subSectionTestCaseFailedTitle = DEFAULT_TEST_FAIL_SECTION_DESCRIPTION;
        this.subSectionTestCaseSkippedTitle = DEFAULT_TEST_SKIP_SECTION_DESCRIPTION;

        // Method information Fields
        this.showMethodTestId = true;
        this.showMethodTestTitle = true;
    }

    /**
     * <pre>
     * Name: onStart()    [public]
     * Description: This method is invoked on Test start, but it never run because the adding of this listener is after test start.
     * </pre>
     *
     * @param iSuite - TestNG object with suite execution result.
     */
    @Override
    public void onStart(final ISuite iSuite) {
    }

    @Override
    public void onFinish(final ISuite iSuite) {
        final TestReportObject report = new TestReportObject();
        LOGGER.trace(report.showFields());
        LOGGER.info(report.createReport(iSuite));
    }

    /**
     * <pre>
     * Name: showFields()    [public]
     * Description: Method to show object field: it could be use  for debug purpose.
     * </pre>
     *
     * @return string with field values.
     */
    public String showFields() {
        final StringBuilder listOfFielsdValues = new StringBuilder();
        listOfFielsdValues.append(CR + CR + TAB + "List of 'TestReportObject' fields:")
        .append(CR + new String(new char[80]).replace(NULL_CHARACTER, '='))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show execution time", this.showExecutionTime))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show time format", this.executionTimeFormat.name())).append(CR)
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show Setup PASSED methods", this.showSetupPassed))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show Setup FAILED methods", this.showSetupFailed))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show Setup SKIPPED methods", this.showSetupSkipped))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show TestCase PASSED methods", this.showTestPassed))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show TestCase FAILED methods", this.showTestFailed))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show TestCase SKIPPED methods", this.showTestSkipped)).append(CR)

        // Frame Configuration Fields
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show TOP frame", this.showTopFrame))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "TOP frame sequence", this.topFrame))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show BOTTOM frame", this.showBottomFrame))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "BOTTOM frame sequence", this.bottomFrame))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show LEFT frame", this.showLeftFrame))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "LEFT frame sequence", this.leftFrame))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show RIGHT frame", this.showRightFrame))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "RIGHT frame sequence", this.rightFrame)).append(CR)

        // Description Field configuration
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Frame Title", this.frameTitle))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Suite name description", this.suiteNameTitle))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Execution Time description", this.executeTimeTitle))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Conf. section description", this.sectionConfigurationTitle))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Conf. PASSED section description", this.subSectionConfigurationPassedTitle))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Conf. FAILED section description", this.subSectionConfigurationFailedTitle))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Conf. SKIPPED section description", this.subSectionConfigurationSkippedTitle))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Test Section description", this.sectionTestCasesTitle))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Test PASSED section description", this.subSectionTestCasePassedTitle))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Test FAILED section description", this.subSectionTestCaseFailedTitle))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Test SKIPPED section description", this.subSectionTestCaseSkippedTitle))
        .append(CR)

        // Method information Fields
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show Test Case ID", this.showMethodTestId = true))
        .append(CR + String.format(SHOWFIELD_TABLE_FORMAT, "Show Test Case Title", this.showMethodTestTitle = true))
        .append(CR + new String(new char[80]).replace(NULL_CHARACTER, '='));
        return listOfFielsdValues.toString();
    }

    /**
     * <pre>
     * Name: createReport()    [private]
     * Description: Method used to prepare table with test execution information.
     * </pre>
     *
     * @param iSuite - TestNG object with suite execution result.
     * @return String with table to print
     */
    private String createReport(final ISuite iSuite) {
        final StringBuilder report = new StringBuilder();

        // Prepare report to print
        report.append(CR);

        // get report content and calculate longest line
        final List<String> reportContent = getReportContent(iSuite);
        final int tableWidth = calculateLargestLine(reportContent) + (this.showRightFrame ? this.rightFrame.length() : 0);

        // Checking for Top Frame required and create
        report.append(CR + frameTopBottom(this.showTopFrame, tableWidth, this.topFrame, this.frameTitle));

        // Adding CORE of table (test results)
        for (final String singleLine : reportContent) {
            report.append(linePrepare(tableWidth, singleLine));
        }

        // Checking for Top Frame required and create
        report.append(CR + frameTopBottom(this.showBottomFrame, tableWidth, this.bottomFrame, ""));

        report.append(CR);
        return report.toString();
    }

    /**
     * <pre>
     * Name: getReportContent()    [private]
     * Description: Method used to iterate through suites and to get suite information (name, annotations, results).
     * </pre>
     *
     * @param iSuites - TestNG object with suite execution result.
     * @return Array with all suites information.
     */
    private List<String> getReportContent(final ISuite iSuites) {
        final List<String> suitesReportContent = new ArrayList<>();
        final Map<String, ISuiteResult> suiteResultMap = iSuites.getResults();
        for (final Map.Entry<String, ISuiteResult> suiteEentryResult : suiteResultMap.entrySet()) {
            for (final String singleLine : getSuiteResult(suiteEentryResult.getValue())) {
                suitesReportContent.add(singleLine);
            }
        }
        return suitesReportContent;
    }

    /**
     * <pre>
     * Name: getSuiteResult()    [private]
     * Description: Method used to create a List of Suite Execution result (Passed/Failed/Skipped Configuration/TestCases).
     * </pre>
     *
     * @param iSuite - Single suite to explore
     * @return - List of rows for output report
     */
    private List<String> getSuiteResult(final ISuiteResult iSuite) {
        final List<String> suiteReportContent = new ArrayList<>();
        final ITestContext suiteTestContext = iSuite.getTestContext();
        Boolean presentFlag = false;

        // Add Suite name:
        suiteReportContent.add(this.suiteNameTitle + "<" + suiteTestContext.getSuite().getName() + "> (Test Name: <"
                + suiteTestContext.getName() + ">)");

        // Add Suite Execution Time:
        if (this.showExecutionTime) {
            suiteReportContent.add(this.executeTimeTitle + timeLinePrepare(suiteTestContext.getStartDate(), suiteTestContext.getEndDate()));
        }

        // Add Passed Configuration Methods
        suiteReportContent.add(this.sectionConfigurationTitle);
        presentFlag = false;
        if (this.showSetupPassed) {
            suiteReportContent.add(this.subSectionConfigurationPassedTitle);
            for (final String singleLine : getMethodList(suiteTestContext.getPassedConfigurations().getAllMethods().iterator(),
                    null)) {
                suiteReportContent.add(singleLine);
                presentFlag = true;
            }
            if (!presentFlag) {
                suiteReportContent.add(" -- none --");
            }
        }

        // Add Failed Configuration Methods
        presentFlag = false;
        if (this.showSetupFailed) {
            suiteReportContent.add(this.subSectionConfigurationFailedTitle);
            for (final String singleLine : getMethodList(suiteTestContext.getFailedConfigurations().getAllMethods().iterator(),
                    suiteTestContext.getFailedConfigurations().getAllResults().iterator())) {
                suiteReportContent.add(singleLine);
                presentFlag = true;
            }
            if (!presentFlag) {
                suiteReportContent.add(" -- none --");
            }
        }

        // Add Skipped Configuration Methods
        presentFlag = false;
        if (this.showSetupFailed) {
            suiteReportContent.add(this.subSectionConfigurationSkippedTitle);
            for (final String singleLine : getMethodList(suiteTestContext.getSkippedConfigurations().getAllMethods().iterator(),
                    null)) {
                suiteReportContent.add(singleLine);
                presentFlag = true;
            }
            if (!presentFlag) {
                suiteReportContent.add(" -- none --");
            }
        }

        // Add Passed Test Methods
        presentFlag = false;
        suiteReportContent.add(this.sectionTestCasesTitle);
        if (this.showTestPassed) {
            suiteReportContent.add(this.subSectionTestCasePassedTitle);
            for (final String singleLine : getMethodList(suiteTestContext.getPassedTests().getAllMethods().iterator(),
                    null)) {
                suiteReportContent.add(singleLine);
                presentFlag = true;
            }
            if (!presentFlag) {
                suiteReportContent.add(" -- none --");
            }
        }

        // Add Failed Configuration Methods
        presentFlag = false;
        if (this.showTestFailed) {
            suiteReportContent.add(this.subSectionTestCaseFailedTitle);
            for (final String singleLine : getMethodList(suiteTestContext.getFailedTests().getAllMethods().iterator(),
                    suiteTestContext.getFailedTests().getAllResults().iterator())) {
                suiteReportContent.add(singleLine);
                presentFlag = true;
            }
            if (!presentFlag) {
                suiteReportContent.add(" -- none --");
            }
        }

        // Add Skipped Configuration Methods
        presentFlag = false;
        if (this.showTestSkipped) {
            suiteReportContent.add(this.subSectionTestCaseSkippedTitle);
            for (final String singleLine : getMethodList(suiteTestContext.getSkippedTests().getAllMethods().iterator(),
                    null)) {
                suiteReportContent.add(singleLine);
                presentFlag = true;
            }
            if (!presentFlag) {
                suiteReportContent.add(" -- none --");
            }
        }

        // Add Suite Separator
        if (this.suiteSeparator != null) {
            suiteReportContent.add(this.suiteSeparator);
        }
        return suiteReportContent;
    }

    /**
     * <pre>
     * Name: getMethodList()    [private]
     * Description: Method used to create a List Method names (Passed/Failed/Skipped) and, if failed, stack trace to find where Test case fail.
     * </pre>
     *
     * @param methodListIterator   - Iterator fot method list
     * @param methodResultIterator result of Execution for a particular suite.
     * @return - List of rows for output report
     */
    private List<String> getMethodList(final Iterator methodListIterator, final Iterator methodResultIterator) {
        final List<String> reportMethodsRows = new ArrayList<>();
        while (methodListIterator.hasNext()) {
            final ITestNGMethod executedMethod = (ITestNGMethod) methodListIterator.next();
            reportMethodsRows.add("    - Method Name: " + executedMethod.getMethodName());

            if (methodResultIterator != null) {
                final ITestResult executedMethodError = (ITestResult) methodResultIterator.next();
                reportMethodsRows.add("      Error type: <" + executedMethodError.getThrowable().getMessage() + ">");
                // Get array from stacktrace

                final StackTraceElement[] stacktraceList = executedMethodError.getThrowable().getStackTrace();

                for (int count = 0; count < stackTraceElements; count++) {
                    if (count == 0) {
                        reportMethodsRows.add(TAB + TAB + TAB + "        Stack trace: " + stacktraceList[count]);
                    } else {
                        reportMethodsRows.add(TAB + TAB + TAB + "                     " + stacktraceList[count]);
                    }
                }
            }
        }
        return reportMethodsRows;
    }

    /**
     * <pre>
     * Name: calculateLargestLine()    [private]
     * Description: Method used to find Largest line for Output formatting..
     * </pre>
     *
     * @param lines - List of lines for output
     * @return - Largest line count.
     */
    private int calculateLargestLine(final List<String> lines) {
        int maxlenght = 0;
        for (final String line : lines) {
            maxlenght = (line.length() > maxlenght) ? line.length() : maxlenght;
        }

        return (maxlenght > DEFAULT_HORIZONTAL_WIDTH) ? maxlenght : DEFAULT_HORIZONTAL_WIDTH;
    }

    /**
     * <pre>
     * Name: linePrepare()    [private]
     * Description: Method used to Format single line for Output.
     * </pre>
     *
     * @param width - Report width (column count)
     * @param value - line content
     * @return - Formatted line
     */
    private String linePrepare(final int width, final String value) {
        final StringBuilder line = new StringBuilder();
        line.append(CR);
        final String lineContent = value;
        final String leftFrameTemp = frameLeftRight(this.showLeftFrame, this.leftFrame);
        line.append(leftFrameTemp).append(lineContent);
        if (this.showRightFrame) {
            final String rightFrameTemp = frameLeftRight(this.showRightFrame, this.rightFrame);
            line.append(new String(new char[(width + leftFrameTemp.length() + rightFrameTemp.length()) - line.length()]).replace("\0", " "))
            .append(rightFrameTemp);
        }

        return line.toString();
    }

    /**
     * <pre>
     * Name: frameTopBottom()    [private]
     * Description: Method to prepare (if enabled) top and bottom frame for formatted report.
     * </pre>
     *
     * @param show        - Flag to enable Top/Bottom visualization
     * @param width       - Frame width
     * @param frameBorder - Character/sequence of character to use for frame.
     * @param frameTitle  - Title to put in Frame
     * @return Formatted Top/Bottom Frame
     */
    private String frameTopBottom(final Boolean show, final int width, final String frameBorder, final String frameTitle) {
        if (show) {
            final int preTitle = (width - frameTitle.length()) / 2;
            final int postTitle = width - frameTitle.length() - preTitle;
            return new String(new char[preTitle]).replace("\0", frameBorder) + frameTitle + new String(new char[postTitle])
                    .replace("\0", frameBorder);
        } else {
            return frameTitle;
        }
    }

    /**
     * <pre>
     * Name: frameLeftRight()    [private]
     * Description: Method to prepare (if enabled) left and right frame for formatted report.
     * </pre>
     *
     * @param show        - Flag to enable Top/Bottom visualization
     * @param frameBorder - Character/sequence of character to use for frame.
     * @return Formatted left/right Frame
     */
    private String frameLeftRight(final Boolean show, final String frameBorder) {
        if (show) {
            return frameBorder;
        } else {
            return "";
        }
    }

    /**
     * <pre>
     * Name: timeLinePrepare()    [private]
     * Description: Method to prepare execution time information..
     * </pre>
     *
     * @param startDate - Start Date/Time of suite
     * @param endDate   - End Date/Time of suite
     * @return - Formatted time information
     */
    private String timeLinePrepare(final Date startDate, final Date endDate) {
        String timeElapsedValue = null;
        SimpleDateFormat dateFormat = null;
        final long diffDateMills = endDate.getTime() - startDate.getTime();
        switch (getExecutionTimeFormat()) {
        case ELAPSED_DDHHMMSS:
            timeElapsedValue = String.format("%d day(s), %2d hour(s), %2d minute(s), %2d second(s)", TimeUnit.MILLISECONDS.toDays(diffDateMills),
                    //Hours
                    TimeUnit.MILLISECONDS.toHours(diffDateMills) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(diffDateMills)),
                    //Minutes
                    TimeUnit.MILLISECONDS.toMinutes(diffDateMills) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diffDateMills)),
                    //Seconds
                    TimeUnit.MILLISECONDS.toSeconds(diffDateMills) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diffDateMills)));
            break;
        case ELAPSED_HHMMSS:
            timeElapsedValue = String.format("%2d hour(s), %2d minute(s), %2d second(s)",
                    //Hours
                    TimeUnit.MILLISECONDS.toHours(diffDateMills),
                    //Minutes
                    TimeUnit.MILLISECONDS.toMinutes(diffDateMills) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diffDateMills)),
                    //Seconds
                    TimeUnit.MILLISECONDS.toSeconds(diffDateMills) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diffDateMills)));
            break;
        case ELAPSED_HHMMSSMMM:
            timeElapsedValue = String.format("%2d hour(s), %2d minute(s), %2d second(s), %3d millisecond(s)",
                    //Hours
                    TimeUnit.MILLISECONDS.toHours(diffDateMills),
                    //Minutes
                    TimeUnit.MILLISECONDS.toMinutes(diffDateMills) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diffDateMills)),
                    //Seconds
                    TimeUnit.MILLISECONDS.toSeconds(diffDateMills) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diffDateMills)),
                    //Milliseconds
                    TimeUnit.MILLISECONDS.toMillis(diffDateMills) - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(diffDateMills)));
            break;
        case STARTEND_DDHHMMSS:
            dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            timeElapsedValue = String.format("Start on %s --> End on %s", dateFormat.format(startDate), dateFormat.format(endDate));
            break;
        case STARTEND_HHMMSS:
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            timeElapsedValue = String.format("Start on %s --> End on %s", dateFormat.format(startDate), dateFormat.format(endDate));
            break;
        case STARTEND_HHMMSSMMM:
            dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
            timeElapsedValue = String.format("Start on %s --> End on %s", dateFormat.format(startDate), dateFormat.format(endDate));
            break;
        default:
            timeElapsedValue = null;
            break;
        }
        return timeElapsedValue;
    }

    private TimeFormat getExecutionTimeFormat() {
        return executionTimeFormat;
    }

    /**
     * Enum to define Text Aligment.
     */
    public enum TimeFormat {
        ELAPSED_HHMMSS, ELAPSED_HHMMSSMMM, ELAPSED_DDHHMMSS, STARTEND_HHMMSS, STARTEND_HHMMSSMMM, STARTEND_DDHHMMSS;
    }
}
