package com.hurence.logisland.hadoop;

import com.hurence.logisland.logging.ComponentLog;
import org.apache.commons.lang3.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

/**
 * Provides synchronized access to UserGroupInformation to avoid multiple processors/services from
 * interfering with each other.
 */
public class SecurityUtil {
    public static final String HADOOP_SECURITY_AUTHENTICATION = "hadoop.security.authentication";
    public static final String KERBEROS = "kerberos";

    /**
     * Initializes UserGroupInformation with the given Configuration and performs the login for the given principal
     * and keytab. All logins should happen through this class to ensure other threads are not concurrently modifying
     * UserGroupInformation.
     *
     * @param config the configuration instance
     * @param principal the principal to authenticate as
     * @param keyTab the keytab to authenticate with
     *
     * @return the UGI for the given principal
     *
     * @throws IOException if login failed
     */
    public static synchronized UserGroupInformation loginKerberos(final Configuration config, final String principal, final String keyTab)
            throws IOException {
        Validate.notNull(config);
        Validate.notNull(principal);
        Validate.notNull(keyTab);

        UserGroupInformation.setConfiguration(config);
        return UserGroupInformation.loginUserFromKeytabAndReturnUGI(principal.trim(), keyTab.trim());
    }

    /**
     * Initializes UserGroupInformation with the given Configuration and returns UserGroupInformation.getLoginUser().
     * All logins should happen through this class to ensure other threads are not concurrently modifying
     * UserGroupInformation.
     *
     * @param config the configuration instance
     *
     * @return the UGI for the given principal
     *
     * @throws IOException if login failed
     */
    public static synchronized UserGroupInformation loginSimple(final Configuration config) throws IOException {
        Validate.notNull(config);
        UserGroupInformation.setConfiguration(config);
        return UserGroupInformation.getLoginUser();
    }

    /**
     * Initializes UserGroupInformation with the given Configuration and returns UserGroupInformation.isSecurityEnabled().
     *
     * All checks for isSecurityEnabled() should happen through this method.
     *
     * @param config the given configuration
     *
     * @return true if kerberos is enabled on the given configuration, false otherwise
     *
     */
    public static boolean isSecurityEnabled(final Configuration config) {
        Validate.notNull(config);
        return KERBEROS.equalsIgnoreCase(config.get(HADOOP_SECURITY_AUTHENTICATION));
    }

    /**
     * Start a thread that periodically attempts to renew the current Kerberos user's ticket.
     *
     * Callers of this method should store the reference to the KerberosTicketRenewer and call stop() to stop the thread.
     *
     * @param id
     *          The unique identifier to use for the thread, can be the class name that started the thread
     *              (i.e. PutHDFS, etc)
     * @param ugi
     *          The current Kerberos user.
     * @param renewalPeriod
     *          The amount of time between attempting renewals.
     * @param logger
     *          The logger to use with in the renewer
     *
     * @return the KerberosTicketRenewer Runnable
     */
    public static KerberosTicketRenewer startTicketRenewalThread(final String id, final UserGroupInformation ugi, final long renewalPeriod, final ComponentLog logger) {
        final KerberosTicketRenewer renewer = new KerberosTicketRenewer(ugi, renewalPeriod, logger);

        final Thread t = new Thread(renewer);
        t.setName("Kerberos Ticket Renewal [" + id + "]");
        t.start();

        return renewer;
    }

}
