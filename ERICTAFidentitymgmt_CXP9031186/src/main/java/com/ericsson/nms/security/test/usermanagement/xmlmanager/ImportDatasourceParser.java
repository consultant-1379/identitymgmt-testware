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

package com.ericsson.nms.security.test.usermanagement.xmlmanager;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;

/**
 * <pre>
 * Class Name: ImportDatasourceParser
 * Description: This class contains methods to transpose Datasources to XML structure.
 * </pre>
 **/
public final class ImportDatasourceParser {
    static final String LF = "\n";
    static final String TAB = "\t";
    static final String DATASOURCESTRUCTUREHEADER = LF + "*** %s DataSource Structure (%s) ***" + LF;
    static final String DATARECORDELEMENT = LF + "%3d) datarecord item ---";
    static final String FIRSTLEVELFIELDELEMENT = LF + TAB + "%3d) Field item: Name <%s>, Value <%s>";
    static final String SECONDLEVELFIELDELEMENT = LF + TAB + TAB + TAB + "%3d) Field item: Name <%s>, Value <%s>";
    static final String SECONDLEVELFIELDTITLECOUNT = LF + TAB + TAB + "%3d) Field block: Name <%s>";
    static final String SECONDLEVELFIELDTITLE = LF + TAB + TAB + "---)Field block: Name <%s>";
    static final String[] CUSTOMIZEDPASSWORDFIELD = new String[]{"customizedPasswordAgeingEnable",
            "passwordAgeingEnable", "pwdMaxAge", "pwdExpireWarning", "graceLoginCount"};

    /* Sequence used to prepare XML file (easiest way to create child object)
     username,firstName,lastName,password,roles,email,enabled,customizedPasswordAgeingEnable,passwordAgeingEnable,pwdMaxAge,pwdExpireWarning,
 graceLoginCount,passwordResetFlag,description,specialAction
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportDatasourceParser.class);

    private ImportDatasourceParser() {
    }

    /**
     * Name: printUserDatasourceStructure()             [public]
     * Description: This method could be use to explore User datasource structure: it is useful to prepare datasource to XML conversion.
     *
     * @param uersDataSource- User Datasource to use for XML creation
     * @return - XML object created
     */
    public static DOMSource userDataSourceToXml(final TestDataSource<DataRecord> uersDataSource) {
        final Iterator<DataRecord> originalDatasourceItarator = uersDataSource.iterator();
        final ImportXmlTranscoder transCoder = new ImportXmlTranscoder();
        try {
            // Initialize objects for XML creation.
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            // Create root and Node Elements
            final Element rootUserNode = doc.createElement(transCoder.getParentValue("user"));
            doc.appendChild(rootUserNode);

            // Iterate through Datarecords
            LOGGER.trace("Start to iterate throught DataRecords of selected DataSource");
            Integer recordCount = 0;
            while (originalDatasourceItarator.hasNext()) {
                final DataRecord thisDataRecord = originalDatasourceItarator.next();
                LOGGER.trace("Elaborate {}° datarecord", ++recordCount);
                rootUserNode.appendChild(getUserInfo(doc, thisDataRecord));
            }

            // write the content into xml file
            return new DOMSource(doc);
        } catch (final Exception exc) {
            LOGGER.error("\nError in XML file creation: {}\n{}", exc.getMessage(), exc.getStackTrace());
            assertThat(false).as("Error in XML file creation").isTrue();
        }
        return null;
    }

    /**
     * Name: getUserInfo()             [private]
     * Description: This Method get information from Datarecord and prepare Node element with user information.
     *
     * @param doc            - 'Document' object for element creation
     * @param thisDataRecord - Datarecord to get user information
     * @return Node with New User informations
     */
    private static Node getUserInfo(final Document doc, final DataRecord thisDataRecord) {
        final ImportXmlTranscoder transCoder = new ImportXmlTranscoder();
        final Element userInfo = doc.createElement(transCoder.getChildValue("user"));

        // Iterate through fielo of selected DataRecord
        Integer elementCount = 0;
        boolean changePasswordFlagFieldFound = false;
        final String targetGroup = (thisDataRecord.getFieldValue("targetgroups") != null)
                ? (String) thisDataRecord.getFieldValue("targetgroups") : transCoder.getDefaultValue("targetgroups");

        // Replace this loop with DataSource sequence
        for (final Map.Entry fieldName : thisDataRecord.getAllFields().entrySet()) {
            LOGGER.trace("\tElaborate {}° field ({})", ++elementCount, fieldName.getKey());
            boolean elementFound = false;

            if ("roles".equals(fieldName.getKey())) {
                LOGGER.trace("\t\tPrepare second level item: TAG <{}>, VALUE <{}>", fieldName.getKey(), fieldName.getValue());
                if (userInfo.getElementsByTagName(transCoder.getParentValue(fieldName.getKey().toString())).getLength() == 0) {
                    LOGGER.trace("\t\tAdding new Sub Element: {}", transCoder.getParentValue(fieldName.getKey().toString()));
                    final Element item = doc.createElement(transCoder.getParentValue(fieldName.getKey().toString()));
                    userInfo.appendChild(item);
                }
                final String[] userRoles = (fieldName.getValue() instanceof String[])
                        ? (String[]) fieldName.getValue() : (fieldName.getValue().toString()).split(",");
                for (final String singleRole : userRoles) {
                    final Element item = doc.createElement(transCoder.getChildValue(fieldName.getKey().toString()));
                    final Node privilegeNode = userInfo.getElementsByTagName(transCoder.getParentValue(fieldName.getKey().toString())).item(0)
                            .appendChild(item);
                    privilegeNode.appendChild(getUserElement(doc, transCoder.getItemValue(fieldName.getKey().toString()), singleRole));
                    privilegeNode.appendChild(getUserElement(doc, transCoder.getItemValue("targetgroups"), targetGroup));
                }
                elementFound = true;
            }

            if (ArrayUtils.contains(CUSTOMIZEDPASSWORDFIELD, fieldName.getKey())) {
                if (thisDataRecord.getFieldValue("customizedPasswordAgeingEnable") != null && "true"
                        .equals(thisDataRecord.getFieldValue("customizedPasswordAgeingEnable"))) {
                    LOGGER.trace("\t\tPrepare second level item: TAG <{}>, VALUE <{}>", fieldName.getKey(), fieldName.getValue());
                    if (userInfo.getElementsByTagName(transCoder.getParentValue(fieldName.getKey().toString())).getLength() == 0) {
                        LOGGER.trace("\t\tAdding new Sub Element: {}", transCoder.getParentValue(fieldName.getKey().toString()));
                        final Element item = doc.createElement(transCoder.getParentValue(fieldName.getKey().toString()));
                        userInfo.appendChild(item);
                    }
                    userInfo.getElementsByTagName(transCoder.getParentValue(fieldName.getKey().toString())).item(0)
                            .appendChild(getUserElement(doc, fieldName.getKey().toString(), fieldName.getValue()));
                }
                elementFound = true;
            }

            if (!elementFound && transCoder.getItemValue(fieldName.getKey().toString()) != null
                    && !"targetgroups".equals(fieldName.getKey().toString())) {
                LOGGER.trace("\t\tPrepare first level item: TAG <{}>, VALUE <{}>", fieldName.getKey(), fieldName.getValue());
                if ("passwordResetFlag".equals(fieldName.getKey())) {
                    changePasswordFlagFieldFound = true;
                }
                String valueToWrite = null;
                if ("enabled".equals(fieldName.getKey().toString())) {
                    valueToWrite = transCoder.enableDisableConversion(fieldName.getKey().toString(), fieldName.getValue().toString());
                } else if (fieldName.getValue() != null) {
                    valueToWrite = fieldName.getValue().toString();
                } else {
                    valueToWrite = transCoder.getDefaultValue(fieldName.getKey().toString());
                }

                userInfo.appendChild(getUserElement(doc, transCoder.getItemValue(fieldName.getKey().toString()), valueToWrite));
            }
        }

        // This mandatory fieeld was not found: putting default value
        if (!changePasswordFlagFieldFound) {
            userInfo.appendChild(getUserElement(doc, transCoder.getItemValue("passwordResetFlag"),
                    transCoder.getDefaultValue("passwordResetFlag")));
        }

        return userInfo;
    }

    private static Node getUserElement(final Document doc, final String nodeTag, final Object nodevalue) {
        LOGGER.trace("\t\tAdding Item: TAG <{}>, VALUE <{}>", nodeTag, nodevalue);
        final Element item = doc.createElement(nodeTag);
        item.appendChild(doc.createTextNode((nodevalue instanceof Integer) ? Integer.toString((Integer) nodevalue) : ((String) nodevalue)));
        return item;
    }

    /**
     * Name: printUserDatasourceStructure()             [public]
     * Description: This method could be use to explore User datasource structure: it is useful to prepare datasource to XML conversion.
     *
     * @param uersDataSource - User Datasource to print
     * @param dataSourceName - DataSource name for documantation
     * @return - SataSource Structure.
     */
    public static String printUserDatasourceStructure(final TestDataSource<DataRecord> uersDataSource, final String dataSourceName) {
        final Iterator<DataRecord> originalDatasourceItarator = uersDataSource.iterator();
        final StringBuilder printDataFields = new StringBuilder();

        // Title for DataSource structure
        printDataFields.append(String.format(DATASOURCESTRUCTUREHEADER, "Users", dataSourceName));

        // Loop throught DataSource and get DataRecord
        Integer recordCount = 0;
        while (originalDatasourceItarator.hasNext()) {
            printDataFields.append(String.format(DATARECORDELEMENT, ++recordCount));
            final DataRecord thisDataRecord = originalDatasourceItarator.next();

            // Loop trough DataRecord elements
            Integer elementCount = 0;
            for (final Map.Entry fieldName : thisDataRecord.getAllFields().entrySet()) {
                boolean elementFound = false;

                // Searching for Roles
                if ("roles".equals(fieldName.getKey())) {
                    printDataFields.append(String.format(SECONDLEVELFIELDTITLECOUNT, ++elementCount, fieldName.getKey()));
                    final String[] userRoles = (fieldName.getValue() instanceof String[])
                            ? (String[]) fieldName.getValue() : (fieldName.getValue().toString()).split(",");
                    // Loop through roles fields
                    Integer subElementCount = 0;
                    for (final String singleRole : userRoles) {
                        printDataFields.append(String.format(SECONDLEVELFIELDELEMENT, ++subElementCount, fieldName.getKey(), singleRole));
                    }
                    elementFound = true;
                }

                // Searching for Password ageing
                if (ArrayUtils.contains(CUSTOMIZEDPASSWORDFIELD, fieldName.getKey())) {
                    // Check if customizedPasswordAgeingEnable is enable (otherwise no underling fields should be include)
                    if (thisDataRecord.getFieldValue("customizedPasswordAgeingEnable") != null && "true"
                            .equals(thisDataRecord.getFieldValue("customizedPasswordAgeingEnable"))) {
                        printDataFields.append(String.format(SECONDLEVELFIELDTITLE, fieldName.getKey()));
                        printDataFields.append(String.format(SECONDLEVELFIELDELEMENT, ++elementCount, fieldName.getKey(), fieldName.getValue()));
                    }
                    elementFound = true;
                }

                // Other elements
                if (!elementFound) {
                    printDataFields.append(String.format(FIRSTLEVELFIELDELEMENT, ++elementCount, fieldName.getKey(), fieldName.getValue()));
                }
            }
        }

        return printDataFields.toString();
    }
}
