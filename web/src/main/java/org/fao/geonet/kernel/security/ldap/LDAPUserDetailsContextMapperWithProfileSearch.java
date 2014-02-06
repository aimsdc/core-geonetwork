//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.kernel.security.ldap;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import jeeves.server.ProfileManager;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;

/**
 * Get all user information from the LDAP user's attributes excluding profiles
 * and groups which are searched in another LDAP location. For profiles and
 * groups, define the search location and the extraction pattern.
 * 
 * @author francois
 */
public class LDAPUserDetailsContextMapperWithProfileSearch extends
        AbstractLDAPUserDetailsContextMapper {

    private String groupAttribute;
    private String groupObject;
    private String groupQuery;
    private String groupQueryPattern;

    private String privilegeAttribute;
    private String privilegeObject;
    private String privilegeQuery;
    private String privilegeQueryPattern;

    private Pattern groupQueryPatternCompiled;

    private Pattern privilegeQueryPatternCompiled;

    protected void setProfilesAndPrivileges(ResourceManager resourceManager,
            ProfileManager profileManager, String defaultProfile,
            String defaultGroup, Map<String, ArrayList<String>> userInfo,
            LDAPUser userDetails) {

        if (!"".equals(groupQuery)) {
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                StringBuffer sb = new StringBuffer("Group and profile search:");
                sb.append("\nGroup attribute: \t" + groupAttribute);
                sb.append("\nGroup query: \t" + groupQuery);
                sb.append("\nGroup attribute: \t" + groupQueryPattern);
                sb.append("\nProfile attribute: \t" + privilegeAttribute);
                sb.append("\nProfile query: \t" + privilegeQuery);
                sb.append("\nProfile attribute: \t" + privilegeQueryPattern);
                Log.debug(Geonet.LDAP, sb.toString());
            }
            // TODO: add more control on values
            NamingEnumeration<?> ldapInfoList;
            try {
                DirContext dc = contextSource.getReadOnlyContext();

                // Extract profile first
                Set<String> profileList = new HashSet<String>();
                // will be the DN of the user as the value, so use CN if you have one.
                String cn = getUserInfo(userInfo, "cn");
                if (cn != null && cn.length() > 0) {
                    Log.debug(Geonet.LDAP, "CN: \t" + cn);
                } else {
                    Log.debug(Geonet.LDAP, "No CN:");
                }
                String groupsQuery = MessageFormat.format(this.privilegeQuery,
                        ((cn != null && cn.length() > 0) ? cn : userDetails.getUsername()));
                Log.debug(Geonet.LDAP, "Set Variable in Profile query: \t" + groupsQuery);
                ldapInfoList = dc.search(privilegeObject, groupsQuery, null);
                while (ldapInfoList.hasMore()) {
                    SearchResult sr = (SearchResult) ldapInfoList.next();
                    String profileName = (String) sr.getAttributes()
                            .get(privilegeAttribute).get();

                    boolean b;
                    String p;
                    if (privilegeQueryPatternCompiled != null) {
                        Matcher m = privilegeQueryPatternCompiled
                                .matcher(profileName);
                        b = m.matches();
                        p = m.group(1);
                    } else {
                        b = true;
                        p = profileName;
                    }
                    if (b) {
                        if (profileMapping != null) {
                            String mapped = profileMapping.get(p);
                            if (mapped != null) {
                                p = mapped;
                            }
                        }
                        profileList.add(p);
                        Log.debug(Geonet.LDAP, "Can Map LDAP profile '" + p);
                    } else {
                        Log.error(Geonet.LDAP, "LDAP profile '" + profileName
                                + "' does not match search pattern '"
                                + privilegeQueryPattern
                                + "'. Information ignored.");
                    }
                }
                String highestUserProfile = profileManager
                        .getHighestProfile(profileList.toArray(new String[profileList.size()]));
                if (highestUserProfile != null) {
                    if (Log.isDebugEnabled(Geonet.LDAP)) {
                        Log.debug(Geonet.LDAP, "  Highest user profile is "
                                + highestUserProfile);
                    }
                    userDetails.setProfile(highestUserProfile);
                }
                
                // If no profile defined, use default profile
                if (userDetails.getProfile() == null) {
                    if (Log.isDebugEnabled(Geonet.LDAP)) {
                        Log.debug(Geonet.LDAP,
                                "  No profile defined in LDAP, using default profile "
                                        + defaultProfile);
                    }
                    userDetails.setProfile(defaultProfile);
                }
                
                
                
                // Get groups
                // will be the DN of the user as the value, so use CN if you have one.
                String groupQuery = MessageFormat.format(this.groupQuery,
                        ((cn != null && cn.length() > 0) ? cn : userDetails.getUsername()));
                Log.debug(Geonet.LDAP, "LDAP search name -> groupObject: \t" + groupObject);
                Log.debug(Geonet.LDAP, "LDAP search filter -> groupQuery: \t" + groupQuery);
                ldapInfoList = dc.search(this.groupObject, groupQuery, null);
                Log.debug(Geonet.LDAP, "LDAP search has results : \t" + ldapInfoList.hasMore());
                while (ldapInfoList.hasMore()) {
                    SearchResult sr = (SearchResult) ldapInfoList.next();
                    String groupName = (String) sr.getAttributes()
                            .get(groupAttribute).get();

                    boolean b;
                    String group;
                    if (groupQueryPatternCompiled != null) {
                        Matcher m = groupQueryPatternCompiled.matcher(groupName);
                        b = m.matches();
                        group = m.group(1);
                    } else {
                        b = true;
                        group = groupName;
                    }
                    if (b) {
                        Log.debug(Geonet.LDAP, "Adding privilege for group: \t" + group);
                        userDetails.addPrivilege(group, userDetails.getProfile());
                    } else {
                        Log.error(Geonet.LDAP, "LDAP group '" + groupName
                                + "' does not match search pattern '"
                                + groupQueryPattern + "'. Information ignored.");
                    }
                }
            } catch (NamingException e) {
                Log.error(Geonet.LDAP, "Failed to extract profiles and groups. Exception: ", e);
                e.printStackTrace();
            }
        }
    }

    public String getGroupQuery() {
        return groupQuery;
    }

    public void setGroupQuery(String groupQuery) {
        this.groupQuery = groupQuery;
    }

    public String getGroupQueryPattern() {
        return groupQueryPattern;
    }

    public void setGroupQueryPattern(String groupQueryPattern) {
        this.groupQueryPattern = groupQueryPattern;
        if (this.groupQueryPattern != null && this.groupQueryPattern.length() > 0) {
            this.groupQueryPatternCompiled = Pattern.compile(groupQueryPattern);
        }
    }

    public String getPrivilegeQuery() {
        return privilegeQuery;
    }

    public void setPrivilegeQuery(String privilegeQuery) {
        this.privilegeQuery = privilegeQuery;
    }

    public String getPrivilegeQueryPattern() {
        return privilegeQueryPattern;
    }

    public void setPrivilegeQueryPattern(String privilegeQueryPattern) {
        this.privilegeQueryPattern = privilegeQueryPattern;
        if (this.privilegeQueryPattern != null && this.privilegeQueryPattern.length() > 0) {
            this.privilegeQueryPatternCompiled = Pattern
                    .compile(privilegeQueryPattern);
        }
    }

    public String getGroupObject() {
        return groupObject;
    }

    public void setGroupObject(String groupObject) {
        this.groupObject = groupObject;
    }

    public String getPrivilegeObject() {
        return privilegeObject;
    }

    public void setPrivilegeObject(String privilegeObject) {
        this.privilegeObject = privilegeObject;
    }

    public String getGroupAttribute() {
        return groupAttribute;
    }

    public void setGroupAttribute(String groupAttribute) {
        this.groupAttribute = groupAttribute;
    }

    public String getPrivilegeAttribute() {
        return privilegeAttribute;
    }

    public void setPrivilegeAttribute(String privilegeAttribute) {
        this.privilegeAttribute = privilegeAttribute;
    }
}