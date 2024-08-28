/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.nms.security.test.usermanagement.datasource;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.google.common.base.Predicate;

public final class PredicatesExt {
    private PredicatesExt() {
    }

    public static Predicate<DataRecord> extLdapContextFilter(final String i) {
        final Predicate<DataRecord> getIthRecord = new Predicate<DataRecord>() {
            @Override
            public boolean test(final DataRecord dataRecord) {
                final String context = dataRecord.getFieldValue("context");
                return context.equals(i);
            }

            @Override
            public boolean apply(final DataRecord dataRecord) {
                final String context = dataRecord.getFieldValue("context");
                return context.equals(i);
            }
        };
        return getIthRecord;
    }

    public static Predicate<DataRecord> testCaseIdFilter(final String i) {

        final Predicate<DataRecord> getIthRecord = new Predicate<DataRecord>() {
            @Override
            public boolean test(final DataRecord dataRecord) {
                final String testId = dataRecord.getFieldValue("testId");
                return testId.equals(i);
            }

            @Override
            public boolean apply(final DataRecord dataRecord) {
                final String testId = dataRecord.getFieldValue("testId");
                return testId.equals(i);
            }
        };
        return getIthRecord;
    }
}