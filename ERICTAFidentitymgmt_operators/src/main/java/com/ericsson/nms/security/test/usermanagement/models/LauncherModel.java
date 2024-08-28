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
 *----------------------------------------------------------------------------*/

package com.ericsson.nms.security.test.usermanagement.models;

import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;


public class LauncherModel extends GenericViewModel {
    public static final String MODEL = "LauncherModel";

    @UiComponentMapping(selectorType = SelectorType.CSS, selector = "div.eaLauncher-ActionBar")
    private UiComponent launcherPage;

    @UiComponentMapping(selectorType = SelectorType.CSS, selector = "h1.eaLauncher-title")
    private UiComponent launcherTitle;

    public UiComponent getLauncherPage() {
        return launcherPage;
    }
    public UiComponent getLauncherTitle() {
        return launcherTitle;
    }

    // Workaround to resolve TimedOut issue which makes selenium core calls to access a WebElement: TORF-463964
    private static final String launcherPage_SelectorType = "CSS";
    private static final String launcherPage_Selector = "div.eaLauncher-ActionBar";
    private static final String launcherTitle_SelectorType = "CSS";
    private static final String launcherTitle_Selector = "h1.eaLauncher-title";


    public static String getLauncherPage_Selector() {
        return launcherPage_Selector;
    }

    public static String getLauncherPage_SelectorType() {
        return launcherPage_SelectorType;
    }

    public static String getLauncherTitle_SelectorType() {
        return launcherTitle_SelectorType;
    }

    public static String getLauncherTitle_Selector() {
        return launcherTitle_Selector;
    }

}
