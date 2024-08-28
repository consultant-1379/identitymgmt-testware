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

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * Class Name: ImportXmlTranscoder
 * Description: This Class should be use to transform Datasource 'field' in XML 'tag' for Import operation. It use a Multimap structure and it
 * return 'Parent', 'Child' and 'Item' string for each given datasource field.
 * </pre>
 */
public final class ImportXmlTranscoder {
    private static final Map<String, Leaves<String, String, String, String>> transcodeXmlTag = new HashMap<>();

    protected ImportXmlTranscoder() {
        transcodeXmlTag.put("user", new Leaves<String, String, String, String>("users", "user", null, null));
        transcodeXmlTag.put("username", new Leaves<String, String, String, String>("user", null, "name", null));
        transcodeXmlTag.put("firstName", new Leaves<String, String, String, String>("user", null, "firstname", null));
        transcodeXmlTag.put("lastName", new Leaves<String, String, String, String>("user", null, "surname", null));
        transcodeXmlTag.put("password", new Leaves<String, String, String, String>("user", null, "password", null));
        transcodeXmlTag.put("roles", new Leaves<String, String, String, String>("privileges", "privilege", "role", null));
        transcodeXmlTag.put("targetgroups", new Leaves<String, String, String, String>("privileges", "privilege", "targetGroup", "ALL"));
        transcodeXmlTag.put("email", new Leaves<String, String, String, String>(null, null, "email", null));
        transcodeXmlTag.put("enabled", new Leaves<String, String, String, String>(null, null, "status", null));
        transcodeXmlTag.put("customizedPasswordAgeingEnable", new Leaves<String, String, String, String>("passwordAgeing", null,
                "customizedPasswordAgeingEnable", "false"));
        transcodeXmlTag.put("passwordAgeingEnable", new Leaves<String, String, String, String>("passwordAgeing", null, "passwordAgeingEnable", null));
        transcodeXmlTag.put("pwdMaxAge", new Leaves<String, String, String, String>("passwordAgeing", null, "pwdMaxAge", null));
        transcodeXmlTag.put("pwdExpireWarning", new Leaves<String, String, String, String>("passwordAgeing", null, "pwdExpireWarning", null));
        transcodeXmlTag.put("graceLoginCount", new Leaves<String, String, String, String>("passwordAgeing", null, "graceLoginCount", null));
        transcodeXmlTag.put("passwordResetFlag", new Leaves<String, String, String, String>("user", null, "changePasswordFlag", "false"));
        transcodeXmlTag.put("description", new Leaves<String, String, String, String>("user", null, "description", null));
    }

    public static String getParentValue(final String dataSourceField) {
        return (transcodeXmlTag.containsKey(dataSourceField))
                ? transcodeXmlTag.get(dataSourceField).getParent() : null;
    }

    public static String getChildValue(final String dataSourceField) {
        return (transcodeXmlTag.containsKey(dataSourceField))
                ? transcodeXmlTag.get(dataSourceField).getChild() : null;
    }

    public static String getItemValue(final String dataSourceField) {
        return (transcodeXmlTag.containsKey(dataSourceField))
                ? transcodeXmlTag.get(dataSourceField).getItem() : null;
    }

    public static String getDefaultValue(final String dataSourceField) {
        return (transcodeXmlTag.containsKey(dataSourceField))
                ? transcodeXmlTag.get(dataSourceField).getDefaultValue() : null;
    }

    public static String enableDisableConversion(final String dataSourceField, final String value) {
        String returnValue = transcodeXmlTag.get(dataSourceField).getDefaultValue();
        if ("true".equalsIgnoreCase(value)) {
            returnValue = "enabled";
        }
        if ("false".equalsIgnoreCase(value)) {
            returnValue = "disabled";
        }
        return returnValue;
    }

    private static class Leaves<A, B, C, D> {
        A parent;
        B child;
        C item;
        D defaultValue;

        Leaves(final A parent, final B child, final C item, final D defaultValue) {
            this.parent = parent;
            this.child = child;
            this.item = item;
            this.defaultValue = defaultValue;
        }

        A getParent() {
            return parent;
        }

        B getChild() {
            return child;
        }

        C getItem() {
            return item;
        }

        D getDefaultValue() {
            return defaultValue;
        }
    }
}
