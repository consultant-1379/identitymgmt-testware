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
 *----------------------------------------------------------------------------
 * */

package com.ericsson.nms.security.test.predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * <pre>
 * Name: GenericPredicate.
 * Description: This class is used implement generic predicate for many customized prediactes.
 * </pre>
 **/
public abstract class GenericPredicate {

    // Logger definition
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericPredicate.class);

    /**
     * <pre>
     * Name: builder().
     * Description: This method is a Generic builder to prepare predicates: it ghet some parameters to configure check.
     * </pre>
     *
     * @param fieldName          - Column (field) used in check.
     * @param fieldValue         - Value to check in DataSource.
     * @param trueFalseCondition - Configuration for True/False condition.
     * @param nullSetting        - How to handle Null (empty) field: it should consider false or true.
     * @return genericPredicate
     */
    protected static Predicate builder(final String fieldName, final String fieldValue, final boolean trueFalseCondition, final boolean nullSetting) {
        final Predicate genericPredicate = new Predicate<DataRecord>() {
            @Override
            public boolean apply(final DataRecord dataRecord) {
                final boolean checkResult = checkPredicateResult(dataRecord, fieldName, fieldValue, trueFalseCondition, nullSetting);
                LOGGER.debug("Check Result <{}> ==>> Searching <{}> in field <{}> (Condition <{}>; null as <{}>) - Record field "
                                + "contents: <{}> ",
                        checkResult, fieldValue, fieldName, trueFalseCondition, nullSetting, dataRecord.getFieldValue(fieldName));
                return checkResult;
            }
        };
        return genericPredicate;
    }

    /**
     * <pre>
     * Name: checkPredicateResult().
     * Description: This method check given DataRecord (record) with selected fieldValue in fieldName; you can select true check or false check,
     *              and consider null value as true or false result.
     * </pre>
     *
     * @param record             - record to check
     * @param fieldName          - Column (field) used in check.
     * @param fieldValue         - Value to check in DataSource.
     * @param trueFalseCondition - Configuration for True/False condition.
     * @param nullSetting        - How to handle Null (empty) field: it should consider false or true.
     * @return - Check result.
     */
    private static boolean checkPredicateResult(final DataRecord record, final String fieldName, final String fieldValue,
                                                final boolean trueFalseCondition, final boolean nullSetting) {
        /* Set initial result value with 'nullSetting': if selected fiel is empty or null, check result is 'nullSetting'. */
        boolean find = nullSetting;
        if (record.getFieldValue(fieldName) instanceof String || record.getFieldValue(fieldName) instanceof String[]) {
            /* check if selected field name is an Array or String, and convert it in Array. */
            final String[] recordValue;
            if (record.getFieldValue(fieldName) instanceof String[]) {
                recordValue = ((String[]) record.getFieldValue(fieldName)).clone();
            } else {
                recordValue = record.getFieldValue(fieldName).toString().split(",");
            }

            /* Check if FieldValue is present in Array */
            find = Lists.newArrayList(recordValue).contains(fieldValue);

            /* Chenge Check result as 'trueFalseCondition' parameter says */
            find = trueFalseCondition ? find : !find;
        }
        return additionalCheck(find, record);
    }

    protected static boolean additionalCheck(final boolean find, final DataRecord record) {
        return find;
    }
}
