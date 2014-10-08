package org.fao.geonet.kernel.security;

import jeeves.utils.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by kgunn on 8/10/2014.
 */
public class CustomUserDetailsByNameServiceWrapper<T extends Authentication> implements AuthenticationUserDetailsService<T>, InitializingBean {
    private List<UserDetailsService> userDetailsServiceList = null;

    /**
     * Constructs an empty wrapper for compatibility with Spring Security 2.0.x's method of using a setter.
     */
    public CustomUserDetailsByNameServiceWrapper() {
        // constructor for backwards compatibility with 2.0
    }

    /**
     * Constructs a new wrapper using the supplied {@link org.springframework.security.core.userdetails.UserDetailsService}
     * as the service to delegate to.
     *
     * @param userDetailsServiceList the List of UserDetailsService to delegate to.
     */
    public CustomUserDetailsByNameServiceWrapper(final List<UserDetailsService> userDetailsServiceList) {
        Assert.notNull(userDetailsServiceList, "userDetailsServiceList cannot be null.");
        this.userDetailsServiceList = userDetailsServiceList;
    }

    /**
     * Check whether all required properties have been set.
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.userDetailsServiceList, "userDetailsServiceList must be set");
    }

    /**
     * Get the UserDetails object from the wrapped UserDetailsService
     * implementation
     */
    public UserDetails loadUserDetails(T authentication) throws UsernameNotFoundException {
        UserDetails userDetails = null;
        for (UserDetailsService userDetailsService: this.userDetailsServiceList) {
            Log.info(Log.JEEVES, " --- Fetching user details for name:" + authentication.getName() +
                    " from service:" + userDetailsService.getClass().getCanonicalName());
            try {
                userDetails = userDetailsService.loadUserByUsername(authentication.getName());
            } catch (Exception e) {
                Log.info(Log.JEEVES, " --- Exception Fetching user details for name:" + authentication.getName() +
                        " from service:" + userDetailsService.getClass().getCanonicalName() + " - " + e.getMessage() + "\n" + Log.getStackTrace(e));

            }
            if (userDetails != null) {
                Log.info(Log.JEEVES, " --- Found user details for name:" + userDetails.getUsername() +
                        " from service:" + userDetailsService.getClass().getCanonicalName());
                break;
            }
        }
        if (userDetails == null) {
            throw new UsernameNotFoundException(authentication.getName() + " is not a valid username in any of the configured UserDetailsService");
        }
        Assert.notNull(userDetails, "userDetailsServiceList did not provide any UserDetails");
        return userDetails;
    }

    /**
     * Set the wrapped UserDetailsService implementation
     *
     * @param aUserDetailsServiceList
     *            The wrapped UserDetailsService to set
     */
    public void setUserDetailsService(List<UserDetailsService> aUserDetailsServiceList) {
        this.userDetailsServiceList = aUserDetailsServiceList;
    }
}
