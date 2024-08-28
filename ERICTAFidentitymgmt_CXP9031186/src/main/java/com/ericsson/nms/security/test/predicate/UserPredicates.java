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

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * <pre>
 * Name: UserPredicates.
 * Description: This class is used to define some predicates to filter UserDataSource.
 * </pre>
 */
public final class UserPredicates extends GenericPredicate {
    // Constant used to select column for filtering operations
    public static final String COLUMN_ROLES = "roles";
    public static final String COLUMN_CUSTOMAEGINGPASSWORDENABLE = "customizedPasswordAgeingEnable";
    public static final String COLUMN_USER = "username";
    public static final String COLUMN_ENABLED = "enabled";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_SPECIALACTION = "specialAction";
    @SuppressWarnings({"checkstyle:declarationorder"})
    public static final Predicate ageingFieldStatus = ageingFieldValue();
    @SuppressWarnings({"checkstyle:declarationorder"})
    public static final Predicate newImportedUserfromXml = newImportedUsers();
    // Constant used to compare item for filtering
    private static final String ADMIN = "ADMINISTRATOR";
    // Basic Predicate definition
    @SuppressWarnings({"checkstyle:declarationorder"})
    public static final Predicate adminUser = rolePredicate(ADMIN);
    private static final String SECURITY_ADMIN = "SECURITY_ADMIN";
    @SuppressWarnings({"checkstyle:declarationorder"})
    public static final Predicate securityAdmin = rolePredicate(SECURITY_ADMIN);
    // Specific value for checking Imported Users (in description field)
    private static final String IMPORTED_USERS_DESCRIPTION = DataHandler.getConfiguration().getProperty("import.users.description",
            "User imported from XML file", String.class);
    @SuppressWarnings({"checkstyle:declarationorder"})
    public static final Predicate importedUserfromXml = importedUsers();
    // Combined predicate definition
    public static final Predicate ageingUser =
            Predicates.and(
                    Predicates.not(adminUser),
                    Predicates.not(securityAdmin),
                    Predicates.not(importedUserfromXml),
                    ageingFieldStatus);
    public static final Predicate genericUser =
            Predicates.and(
                    Predicates.not(adminUser),
                    Predicates.not(securityAdmin),
                    Predicates.not(ageingFieldStatus),
                    Predicates.not(importedUserfromXml));
    public static final Predicate notAdminUsers =
            Predicates.and(
                    Predicates.not(adminUser),
                    Predicates.not(securityAdmin));

    // Default constructor
    private UserPredicates() {
    }

    protected static boolean additionalCheck(final boolean find, final DataRecord record) {
        return find && record.getFieldValue(COLUMN_ENABLED).equals(true);
    }

    /**
     * <pre>
     * Name: ageingFieldValue.
     * Description: This predicate check if 'COLUMN_CUSTOMAEGINGPASSWORDENABLE': this mean user should be use for Ageing Test Case.
     * </pre>
     *
     * @return - predicate used to filter users for Ageing Test
     */
    public static Predicate<DataRecord> ageingFieldValue() {
        return userPredicateBuilder(COLUMN_CUSTOMAEGINGPASSWORDENABLE, "true", true, false);
    }

    /**
     * <pre>
     * Name: importedUsers.
     * Description: This predicate check if 'COLUMN_DESCRIPTION' contain specific value (from property 'import.users.description'): this could be use
     * to filter imported User.
     * </pre>
     *
     * @return - predicate used to filter users for Ageing Test
     */
    public static Predicate<DataRecord> importedUsers() {
        return userPredicateBuilder(COLUMN_DESCRIPTION, IMPORTED_USERS_DESCRIPTION, true, false);
    }

    /**
     * <pre>
     * Name: importedUsers.
     * Description: This predicate check if 'COLUMN_DESCRIPTION' contain specific value (from property 'import.users.description'): this could be use
     * to filter imported User.
     * </pre>
     *
     * @return - predicate used to filter users for Ageing Test
     */
    public static Predicate<DataRecord> newImportedUsers() {
        return userPredicateBuilder(COLUMN_SPECIALACTION, "new", true, false);
    }

    /**
     * <pre>
     * Name: rolePredicate().
     * Description: This predicate could be use to filter users selecting ones with 'COLUMN_ROLES' field filed with 'roleValue' value
     * </pre>
     *
     * @param roleValue - value to compare with 'COLUMN_ROLES' field.
     * @return - predicate used to filter users for Ageing Test
     */
    public static Predicate rolePredicate(final String roleValue) {
        return userPredicateBuilder(COLUMN_ROLES, roleValue, true, false);
    }

    /**
     * <pre>
     * Name: nodePredicateBuilder().
     * Description: This method is a User Predicate Builder and could be used to prepare predicates to filter Users. It is a shield for
     *              generic predicate ad it is Public.
     * </pre>
     *
     * @param fieldName          - Column (field) used in check.
     * @param fieldValue         - Value to check in DataSource.
     * @param trueFalseCondition - Configuration for True/False condition.
     * @param nullSetting        - How to handle Null (empty) field: it should consider false or true.
     * @return - Created Predicate
     */
    public static Predicate userPredicateBuilder(final String fieldName,
            final String fieldValue,
            final boolean trueFalseCondition,
            final boolean nullSetting) {
        return builder(fieldName, fieldValue, trueFalseCondition, nullSetting);
    }

    /**
     * <pre>
     * Name: underIdentifier().
     * Description: This method could be use to select only fewer element (users) from a datasources. You can split username with separator
     *      'splitter' and, if last element of array is number, you can choos only ones with value under 'lastElement' value..
     * </pre>
     *
     * @param fieldName   - Field to use for filtering
     * @param lastElement - Integer to select elements under this value
     * @return predicate
     */
    public static Predicate underIdentifier(final String fieldName, final Integer lastElement) {
        return underIdentifier(fieldName, lastElement, "_");
    }

    public static Predicate underIdentifier(final String fieldName, final Integer lastElement, final String splitter) {
        return new Predicate<DataRecord>() {
            @Override
            public boolean apply(final DataRecord datarecord) {
                final boolean result = true;
                final String fieldValue = datarecord.getFieldValue(fieldName);
                final String[] fieldElements = fieldValue.split(splitter);
                for (final String singleElement : fieldElements) {
                    if (singleElement.matches("\\d{1,3}") && Integer.parseInt(singleElement) > lastElement) {
                        return false;
                    }
                }
                return result;
            }
        };
    }
}
