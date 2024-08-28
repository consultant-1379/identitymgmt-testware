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

import java.util.HashMap;

/**
 * <pre>
 * Class Name: TestInfo
 * Description: This class should be use to get TestId and Test Name of selected Test Case. Optionally could give also Test description...
 * </pre>
 */
public class TestInfo {
    static final String ID_USERMNG_CREATEUSER = "AGAT_US_UM_CreateUsers";
    static final String ID_USERMNG_USERCANLOGIN = "AGAT_US_UM_UserCanLogin";
    static final String ID_USERMNG_DELETEUSER = "AGAT_US_UM_DeleteUsers";
    static final String ID_USERMNG_UPDATEUSER = "AGAT_US_UM_UpdateUsersInformation";
    static final String ID_USERMNG_CHANGEPASSWORD = "AGAT_US_UM_ChangeUserPassword";
    static final String ID_USERMNG_CREATEROLE = "AGAT_US_RM_CreateRoles";
    static final String ID_USERMNG_DELETEROLE = "AGAT_US_RM_DeleteRoles";
    static final String ID_USERMNG_ASSIGN_TARGETGROUP_ROLE = "AGAT_US_TGM_AssignTargetToTargetGroup";
    static final String ID_USERMNG_UNASSIGN_TARGETGROUP_ROLE = "AGAT_US_TGM_UnassignTargetFromTargetGroup";
    static final String ID_USERMNG_CREATETARGETGROUP = "AGAT_US_TGM_Create_Target_Groups";
    static final String ID_USERMNG_DELETETARGETGROUP = "AGAT_US_TGM_Delete_Target_Groups";
    static final String ID_USERMNG_CREATEUSER_AGEING = "AGAT_US_UM_CreateUserWithCustomPasswordAgeing";
    static final String ID_USERMNG_UPDATEUSER_AGEING = "AGAT_US_UM_UpdateExistingUserPasswordAgeing";
    static final String ID_USERMNG_CONFIGURELOGINLOCK = "AGAT_US_SSC_ConfigurableLoginLock";
    static final String ID_USERMNG_IMPORTUSERFROMXML = "AGAT_US_MMUD_ImportUserFromXmlFile";
    static final String ID_USERMNG_EXPORTUSERTOXML = "AGAT_US_MMUD_ExportUsersToXmlFile";

    static final String NAME_USERMNG_CREATEUSER = "Create a User";
    static final String NAME_USERMNG_USERCANLOGIN = "Created User can login for the first time";
    static final String NAME_USERMNG_DELETEUSER = "Delete User(s)";
    static final String NAME_USERMNG_UPDATEUSER = "Update Existing User information";
    static final String NAME_USERMNG_CHANGEPASSWORD = "Change a User’s password";
    static final String NAME_USERMNG_CREATEROLE = "Create a Role";
    static final String NAME_USERMNG_DELETEROLE = "Delete Role(s)";
    static final String NAME_USERMNG_ASSIGN_TARGETGROUP_ROLE = "Assign a Role and a Target Group to User";
    static final String NAME_USERMNG_UNASSIGN_TARGETGROUP_ROLE = "Unassign a Role and a Target Group from a User";
    static final String NAME_USERMNG_CREATETARGETGROUP = "Create a Target Group";
    static final String NAME_USERMNG_DELETETARGETGROUP = "Delete a Target Group";
    static final String NAME_USERMNG_CREATEUSER_AGEING = "Create a User with customized Password Ageing";
    static final String NAME_USERMNG_UPDATEUSER_AGEING = "Update existing User information for customized Password Ageing";
    static final String NAME_USERMNG_CONFIGURELOGINLOCK = "Configurable Login Lock";
    static final String NAME_USERMNG_IMPORTUSERFROMXML = "Import users from XML file";
    static final String NAME_USERMNG_EXPORTUSERTOXML = "Export selected users to an XML file";

    private final HashMap<String, String> testId;
    private final HashMap<String, String> testName;
    private final HashMap<String, String> testDescription;

    TestInfo() {
        testId = new HashMap<>();
        testName = new HashMap<>();
        testDescription = new HashMap<>();
        String testCase = "";

        // Initializing Hash Map Fields...
        testCase = "createRoles";
        testId.put(testCase, ID_USERMNG_CREATEROLE);
        testName.put(testCase, NAME_USERMNG_CREATEROLE);
        testDescription.put(testCase, "Demonstrate that a user with SECURITY_ADMIN role is able to create a role via CLI interface.");

        testCase = "deleteRoles";
        testId.put(testCase, ID_USERMNG_DELETEROLE);
        testName.put(testCase, NAME_USERMNG_DELETEROLE);
        testDescription.put(testCase, "Demonstrate that a user with SECURITY_ADMIN role is able to delete a role via CLI interface.");

        testCase = "createTargetGroups";
        testId.put(testCase, ID_USERMNG_CREATETARGETGROUP);
        testName.put(testCase, NAME_USERMNG_CREATETARGETGROUP);
        testDescription.put(testCase, "Demonstrate that a user with SECURITY_ADMIN role is able to create a target Group via CLI interface.");

        testCase = "deleteTargetGroups";
        testId.put(testCase, ID_USERMNG_DELETETARGETGROUP);
        testName.put(testCase, NAME_USERMNG_DELETETARGETGROUP);
        testDescription.put(testCase, "Demonstrate that a user with SECURITY_ADMIN role is able to delete a target Group via CLI interface.");

        testCase = "createUser";
        testId.put(testCase, ID_USERMNG_CREATEUSER);
        testName.put(testCase, NAME_USERMNG_CREATEUSER);
        testDescription.put(testCase, "Demonstrate that a user with SECURITY_ADMIN role is able to create a user on the system via CLI interface.");

        testCase = "checkUserCanLogin";
        testId.put(testCase, ID_USERMNG_USERCANLOGIN);
        testName.put(testCase, NAME_USERMNG_USERCANLOGIN);
        testDescription.put(testCase, "Confirm that a user created with User Management can login to ENM.\n (Show how the user is prompt to change "
                + "his password the first time he logins to ENM [TODO])");

        testCase = "deleteUsers";
        testId.put(testCase, ID_USERMNG_DELETEUSER);
        testName.put(testCase, NAME_USERMNG_DELETEUSER);
        testDescription.put(testCase, "Show how a single or multiple users can be deleted from the systemDemonstrate how a user can be updated .");

        testCase = "assignRoleAndTargeGroupToUser";
        testId.put(testCase, ID_USERMNG_ASSIGN_TARGETGROUP_ROLE);
        testName.put(testCase, NAME_USERMNG_ASSIGN_TARGETGROUP_ROLE);
        testDescription.put(testCase, "Verify that Security Admin can edit target group and assign targets.");

        testCase = "unassignRoleAndTargeGroupToUser";
        testId.put(testCase, ID_USERMNG_UNASSIGN_TARGETGROUP_ROLE);
        testName.put(testCase, NAME_USERMNG_UNASSIGN_TARGETGROUP_ROLE);
        testDescription.put(testCase, "Verify that Security Admin can edit target group and unassign targets.");

        testCase = "updateExistingUserInformation";
        testId.put(testCase, ID_USERMNG_UPDATEUSER);
        testName.put(testCase, NAME_USERMNG_UPDATEUSER);
        testDescription.put(testCase, "Demonstrate how a user information can be updated via CLI interface.");

        testCase = "changeUserPassword";
        testId.put(testCase, ID_USERMNG_CHANGEPASSWORD);
        testName.put(testCase, NAME_USERMNG_CHANGEPASSWORD);
        testDescription.put(testCase, "Show how Security Administrator can change the password of a user.");

        testCase = "createUserWithPasswordAgeing";
        testId.put(testCase, ID_USERMNG_CREATEUSER_AGEING);
        testName.put(testCase, NAME_USERMNG_CREATEUSER_AGEING);
        testDescription.put(testCase, "Demonstrate that a user with SECURITY_ADMIN role is able to create a user via CLI interface: the user will "
                + "have customized Password Ageing");

        testCase = "updateExistingUserAgeingInformation";
        testId.put(testCase, ID_USERMNG_UPDATEUSER_AGEING);
        testName.put(testCase, NAME_USERMNG_UPDATEUSER_AGEING);
        testDescription.put(testCase, "Demonstrate how customized Password Ageing information for a user can be updated via CLI interface");

        testCase = "configureLoginLock";
        testId.put(testCase, ID_USERMNG_CONFIGURELOGINLOCK);
        testName.put(testCase, NAME_USERMNG_CONFIGURELOGINLOCK);
        testDescription.put(testCase, "Demonstrates that a user with SECURITY_ADMIN role is able to configure account lockout attributes with the "
                + "System Security Configuration CLI");

        testCase = "importUsersFromXml";
        testId.put(testCase, ID_USERMNG_IMPORTUSERFROMXML);
        testName.put(testCase, NAME_USERMNG_IMPORTUSERFROMXML);
        testDescription.put(testCase, "Demonstrate that a user with SECURITY_ADMIN role is able to import bunch of users from an XML file to the ENM"
                + " system.");

        testCase = "exportUsersToXlm";
        testId.put(testCase, ID_USERMNG_EXPORTUSERTOXML);
        testName.put(testCase, NAME_USERMNG_EXPORTUSERTOXML);
        testDescription.put(testCase, "Demonstrate that a user with the SECURITY_ADMIN role is able to export selected users from User Management"
                + " application to an XML file");
    }

    public String getTestId(final String testCase) {
        return testId.get(testCase);
    }

    public String getTestName(final String testCase) {
        return testName.get(testCase);
    }

    public String getTestDescription(final String testCase) {
        return testDescription.get(testCase);
    }
}
