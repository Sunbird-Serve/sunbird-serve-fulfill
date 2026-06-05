package com.sunbird.serve.fulfill.config;

/**
 * ThreadLocal-based holder for tenant/user context extracted from JWT.
 * Populated by JwtTenantFilter after authentication.
 */
public class TenantContext {

    private static final ThreadLocal<String> agencyId = new ThreadLocal<>();
    private static final ThreadLocal<String> agencyType = new ThreadLocal<>();
    private static final ThreadLocal<String> userId = new ThreadLocal<>();
    private static final ThreadLocal<String> userEmail = new ThreadLocal<>();

    public static String getAgencyId() {
        return agencyId.get();
    }

    public static void setAgencyId(String value) {
        agencyId.set(value);
    }

    public static String getAgencyType() {
        return agencyType.get();
    }

    public static void setAgencyType(String value) {
        agencyType.set(value);
    }

    public static String getUserId() {
        return userId.get();
    }

    public static void setUserId(String value) {
        userId.set(value);
    }

    public static String getUserEmail() {
        return userEmail.get();
    }

    public static void setUserEmail(String value) {
        userEmail.set(value);
    }

    public static boolean isTenantScoped() {
        return agencyId.get() != null;
    }

    public static void clear() {
        agencyId.remove();
        agencyType.remove();
        userId.remove();
        userEmail.remove();
    }
}
