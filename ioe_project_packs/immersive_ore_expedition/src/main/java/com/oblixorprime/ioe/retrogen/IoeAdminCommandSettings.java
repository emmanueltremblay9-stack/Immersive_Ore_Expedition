package com.oblixorprime.ioe.retrogen;

record IoeAdminCommandSettings(
        boolean locateProvinceEnabled,
        boolean locateAnchorEnabled,
        boolean retrogenStatusEnabled,
        boolean retrogenStartEnabled,
        boolean retrogenPauseEnabled,
        boolean adminRadiusModeAllowed
) {
    static IoeAdminCommandSettings fromConfig() {
        return new IoeAdminCommandSettings(
                IoeRetrogenAdminConfig.commandLocateProvinceEnabled(),
                IoeRetrogenAdminConfig.commandLocateAnchorEnabled(),
                IoeRetrogenAdminConfig.commandRetrogenStatusEnabled(),
                IoeRetrogenAdminConfig.commandRetrogenStartEnabled(),
                IoeRetrogenAdminConfig.commandRetrogenPauseEnabled(),
                IoeRetrogenAdminConfig.modeAllowed(RetrogenMode.ADMIN_RADIUS)
        );
    }

    boolean anyLocateCommandEnabled() {
        return locateProvinceEnabled || locateAnchorEnabled;
    }

    boolean adminRadiusStartEnabled() {
        return retrogenStartEnabled && adminRadiusModeAllowed;
    }

    boolean anyRetrogenCommandEnabled() {
        return retrogenStatusEnabled || retrogenPauseEnabled || adminRadiusStartEnabled();
    }
}
