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

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.PasswordUtil;
import jeeves.utils.SerialFactory;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

public class LDAPSynchronizerJob extends QuartzJobBean {

    private ApplicationContext applicationContext;

    private DefaultSpringSecurityContextSource contextSource;

    private Map<String, String> userAttributeMapping;

    private JobDataMap jdm;

    @Override
    protected void executeInternal(JobExecutionContext jobExecContext)
            throws JobExecutionException {
        try {
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                Log.debug(Geonet.LDAP, "LDAPSynchronizerJob starting ...");
            }

            // Retrieve application context. A default SpringBeanJobFactory
            // will not provide the application context to the job. Use
            // AutowiringSpringBeanJobFactory.
            applicationContext = (ApplicationContext) jobExecContext
                    .getJobDetail().getJobDataMap().get("applicationContext");

            if (applicationContext == null) {
                Log.error(
                        Geonet.LDAP,
                        "  Application context is null. Be sure to configure SchedulerFactoryBean job factory property with AutowiringSpringBeanJobFactory.");
            }

            // Get LDAP information defining which users to sync
            jdm = jobExecContext.getJobDetail()
                    .getJobDataMap();
            contextSource = (DefaultSpringSecurityContextSource) jdm
                    .get("contextSource");

            String ldapUserSearchFilter = (String) jdm
                    .get("ldapUserSearchFilter");
            String ldapUserSearchBase = (String) jdm.get("ldapUserSearchBase");
            String ldapUserSearchAttribute = (String) jdm
                    .get("ldapUserSearchAttribute");
            userAttributeMapping = (Map<String, String>) jdm.get("userAttributeMapping");
            boolean ldapCreateMissingAccounts = false;
            try {
                ldapCreateMissingAccounts = Boolean.parseBoolean((String) jdm.get("ldapCreateMissingAccounts"));
            } catch (Exception e) {
            }
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                Log.debug(Geonet.LDAP, "LDAPSynchronizerJob ldapUserSearchBase: " + ldapUserSearchBase);
                Log.debug(Geonet.LDAP, "LDAPSynchronizerJob ldapUserSearchFilter: " + ldapUserSearchFilter);
                Log.debug(Geonet.LDAP, "LDAPSynchronizerJob ldapUserSearchAttribute: " + ldapUserSearchAttribute);
                Log.debug(Geonet.LDAP, "LDAPSynchronizerJob ldapCreateMissingAccounts: " + ldapCreateMissingAccounts);
            }

            DirContext dc = contextSource.getReadOnlyContext();

            // Get database
            ResourceManager resourceManager = applicationContext
                    .getBean(ResourceManager.class);
            Dbms dbms = null;
            // Get serial factory
            SerialFactory serialFactory = applicationContext
                    .getBean(SerialFactory.class);

            try {
                dbms = (Dbms) resourceManager.openDirect(Geonet.Res.MAIN_DB);

                // Users
                synchronizeUser(ldapUserSearchFilter, ldapUserSearchBase,
                        ldapUserSearchAttribute, dc, dbms, serialFactory, ldapCreateMissingAccounts);

                // And optionaly groups
                String createNonExistingLdapGroup = (String) jdm
                        .get("createNonExistingLdapGroup");

                if ("true".equals(createNonExistingLdapGroup)) {

                    String ldapGroupSearchFilter = (String) jdm
                            .get("ldapGroupSearchFilter");
                    String ldapGroupSearchBase = (String) jdm
                            .get("ldapGroupSearchBase");
                    String ldapGroupSearchAttribute = (String) jdm
                            .get("ldapGroupSearchAttribute");
                    String ldapGroupSearchPattern = (String) jdm
                            .get("ldapGroupSearchPattern");
                    String ldapGroupDescAttribute = (String) jdm
                            .get("ldapGroupDescAttribute");
                    userAttributeMapping = (Map<String, String>) jdm.get("userAttributeMapping");
                    if (Log.isDebugEnabled(Geonet.LDAP)) {
                        Log.debug(Geonet.LDAP, "LDAPSynchronizerJob ldapGroupSearchFilter: " + ldapGroupSearchFilter);
                        Log.debug(Geonet.LDAP, "LDAPSynchronizerJob ldapGroupSearchBase: " + ldapGroupSearchBase);
                        Log.debug(Geonet.LDAP, "LDAPSynchronizerJob ldapGroupSearchAttribute: " + ldapGroupSearchAttribute);
                        Log.debug(Geonet.LDAP, "LDAPSynchronizerJob ldapGroupSearchPattern: " + ldapGroupSearchPattern);
                        Log.debug(Geonet.LDAP, "LDAPSynchronizerJob ldapGroupDescAttribute: " + ldapGroupDescAttribute);
                    }

                    synchronizeGroup(ldapGroupSearchFilter,
                            ldapGroupSearchBase, ldapGroupSearchAttribute,
                            ldapGroupSearchPattern, ldapGroupDescAttribute,
                            ldapUserSearchFilter, ldapUserSearchBase,
                            ldapUserSearchAttribute,
                            dc, dbms, serialFactory);
                }
            } catch (NamingException e1) {
                Log.error(
                        Geonet.LDAP,
                        "LDAP error while synchronizing LDAP user in database",
                        e1);
                e1.printStackTrace();
            } catch (Exception e) {
                try {
                    resourceManager.abort(Geonet.Res.MAIN_DB, dbms);
                    dbms = null;
                } catch (Exception e2) {
                    e.printStackTrace();
                    Log.error(Geonet.LDAP, "Error closing dbms" + dbms, e2);
                }
                Log.error(
                        Geonet.LDAP,
                        "Unexpected error while synchronizing LDAP user in database",
                        e);
            } finally {
                try {
                    dc.close();
                } catch (NamingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (dbms != null) {
                    try {
                        resourceManager.close(Geonet.Res.MAIN_DB, dbms);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.error(Geonet.LDAP, "Error closing dbms" + dbms, e);
                    }
                }
            }
        } catch (Exception e) {
            Log.error(
                    Geonet.LDAP,
                    "Unexpected error while synchronizing LDAP user in database",
                    e);
            e.printStackTrace();
        }

        if (Log.isDebugEnabled(Geonet.LDAP)) {
            Log.debug(Geonet.LDAP, "LDAPSynchronizerJob done.");
        }
    }

    private void synchronizeUser(String ldapUserSearchFilter,
                                 String ldapUserSearchBase, String ldapUserSearchAttribute,
                                 DirContext dc, Dbms dbms, SerialFactory serialFactory,
                                 boolean ldapCreateMissingAccounts) throws NamingException, SQLException {
        // Do something for LDAP users ? Currently user is updated on log
        NamingEnumeration<?> userList = dc.search(ldapUserSearchBase,
                ldapUserSearchFilter, null);
        if (Log.isDebugEnabled(Geonet.LDAP)) {
            Log.debug(Geonet.LDAP, "synchronizeUser search has results: " + userList.hasMore());
        }

        // Build a list of LDAP users
        StringBuffer usernames = new StringBuffer();
        int appendedCnt = 0;
        try {
            while (userList.hasMore()) {
                Object obj = userList.next();
                if (obj != null) {
                    SearchResult sr = (SearchResult) obj;
                    if (sr.getAttributes() != null && sr.getAttributes().get(ldapUserSearchAttribute) != null) {
                        Attribute attribute = sr.getAttributes().get(ldapUserSearchAttribute);
                        String attributeValue = (String) attribute.get();
                        usernames.append("'");
                        usernames.append(attributeValue);
                        usernames.append("', ");
                        String profile = null;
                        String cnAttributeValue =
                                ((sr.getAttributes().get("cn") != null) ? ((String) (sr.getAttributes().get("cn").get())) : "");
                        if (cnAttributeValue.length() > 0) {
                            profile = this.getHighestProfile(attributeValue, cnAttributeValue);
                        }
                        if (ldapCreateMissingAccounts) {
                            Log.info(Geonet.LDAP, "synchronizeUser checking username: " + attributeValue);
                            String query = "SELECT id FROM Users WHERE authtype=? AND username = '" + attributeValue + "'";
                            if (Log.isDebugEnabled(Geonet.LDAP)) {
                                Log.debug(Geonet.LDAP, "synchronizeUser single db Query: " + query.replaceAll("\\?", LDAPConstants.LDAP_FLAG));
                            }
                            Element e = dbms.select(query, LDAPConstants.LDAP_FLAG);
                            if (e == null || e.getChildren("record") == null || e.getChildren("record").size() == 0) {
                                if (Log.isDebugEnabled(Geonet.LDAP)) {
                                    Log.debug(Geonet.LDAP, "  - Create LDAP user " + attributeValue + " in local database.");
                                }
                                //PasswordUtil.encode(context, password);
                                String id = applicationContext.getBean(SerialFactory.class).getSerial(dbms, "Users") + "";
                                String insert_query = "INSERT INTO Users (id, username, password, profile, authtype) " +
                                        "VALUES (?, ?, ?, ?, ?)";
                                try {
                                    dbms.execute(insert_query, Integer.valueOf(id), attributeValue, "LDAP_PASSWORD",
                                            ((profile != null) ? profile : Geonet.Profile.REGISTERED_USER), LDAPConstants.LDAP_FLAG);
                                    Log.debug(Geonet.LDAP, "  - Created LDAP user " + attributeValue + " in local database.");
                                } catch (Exception e1) {
                                    Log.error(Geonet.LDAP, "Exception on user insertion for username:" + attributeValue + " - " + e1.getMessage());
                                    Log.debug(Geonet.LDAP, Log.getStackTrace(e1));
                                }
                            } else {
                                Element r = (Element) e.getChildren("record").get(0);
                                int userId = Integer.valueOf(r.getChildText("id"));
                                Log.debug(Geonet.LDAP, "Record with username:" + attributeValue + " has id:" + userId);
                            }
                        }
                        if (userAttributeMapping != null) {
                            try {
                                ProfileManager profileManager = applicationContext
                                        .getBean(ProfileManager.class);
                                Map<String, ArrayList<String>> userInfo = LDAPUtils
                                        .convertAttributes(sr.getAttributes().getAll());
                                LDAPUser userDetails = new LDAPUser(profileManager, attributeValue);
                                userDetails.setName(getUserInfo(userInfo, "name"))
                                        .setSurname(getUserInfo(userInfo, "surname"))
                                        .setEmail(getUserInfo(userInfo, "mail"))
                                        .setOrganisation(getUserInfo(userInfo, "organisation"))
                                        .setAddress(getUserInfo(userInfo, "address"))
                                        .setState(getUserInfo(userInfo, "state"))
                                        .setZip(getUserInfo(userInfo, "zip"))
                                        .setCity(getUserInfo(userInfo, "city"))
                                        .setCountry(getUserInfo(userInfo, "country"));
                                // these need type LDAPUser, so separate call for new cast
                                userDetails.setCn(getUserInfo(userInfo, "cn"))
                                        .setDn(getUserInfo(userInfo, "dn"));
                                userDetails.setProfile(((profile != null) ? profile : Geonet.Profile.REGISTERED_USER));
                                Log.debug(Geonet.LDAP, "  - To Update Mapped User Details - " + userDetails.toString());
                                // got some details, should be able to update user
                                LDAPUtils.saveUser(userDetails, dbms, serialFactory, false, false, false);
                                Log.info(Geonet.LDAP, "  - Updated LDAP user " + attributeValue + " in local database.");
                            } catch (Exception e1) {
                                Log.error(Geonet.LDAP, "Exception updating user " + attributeValue + " - " + e1.getMessage());
                                Log.debug(Geonet.LDAP, Log.getStackTrace(e1));
                            }
                        } else {
                            Log.info(Geonet.LDAP, "Could not set userAttributeMapping from job details map");
                        }
                        Log.debug(Geonet.LDAP, "synchronizeUser appending username: " + attributeValue);
                        ++appendedCnt;
                    } else {
                        Log.debug(Geonet.LDAP, "synchronizeUser ldap object does not contain required attribute: " + ldapUserSearchAttribute);
                    }
                } else {
                    Log.debug(Geonet.LDAP, "synchronizeUser userLost contained null object");
                }
            }
        } catch (javax.naming.SizeLimitExceededException slee) {
            Log.error(
                    Geonet.LDAP,
                    "Unexpected error while processing LDAP user list; appendedCnt:" + appendedCnt,
                    slee);
        }

        // Remove LDAP user available in db and not in LDAP if not linked to
        // metadata
        String query = "SELECT id FROM Users WHERE authtype=? AND username NOT IN ("
                + usernames.toString() + "'')";
        if (Log.isDebugEnabled(Geonet.LDAP)) {
            Log.debug(Geonet.LDAP, "synchronizeUser db Query: " + query.replaceAll("\\?", LDAPConstants.LDAP_FLAG));
        }
        Element e = dbms.select(query, LDAPConstants.LDAP_FLAG);
        if (Log.isDebugEnabled(Geonet.LDAP)) {
            Log.debug(Geonet.LDAP, "synchronizeUser db Query result size: " + e.getChildren("record").size());
        }
        for (Object record : e.getChildren("record")) {
            Element r = (Element) record;
            int userId = Integer.valueOf(r.getChildText("id"));
            Log.debug(Geonet.LDAP, "  - Removing user: " + userId);
            try {
                dbms.execute("DELETE FROM UserGroups WHERE userId=?", userId);
                dbms.execute("DELETE FROM Users WHERE authtype=? AND id=?",
                        LDAPConstants.LDAP_FLAG, userId);
            } catch (Exception ex) {
                Log.error(Geonet.LDAP, "Failed to remove LDAP user with id "
                        + userId
                        + " in database. User is probably a metadata owner."
                        + " Transfer owner first.", ex);
            }
        }
    }

    private void synchronizeGroup(String ldapGroupSearchFilter,
                                  String ldapGroupSearchBase, String ldapGroupSearchAttribute,
                                  String ldapGroupSearchPattern, String ldapGroupDescAttribute,
                                  String ldapUserSearchFilter, String ldapUserSearchBase, String ldapUserSearchAttribute,
                                  DirContext dc, Dbms dbms,
                                  SerialFactory serialFactory) throws NamingException, SQLException {

        NamingEnumeration<?> groupList = dc.search(ldapGroupSearchBase,
                ldapGroupSearchFilter, null);
        if (Log.isDebugEnabled(Geonet.LDAP)) {
            Log.debug(Geonet.LDAP, "synchronizeGroup search has results: " + groupList.hasMore());
        }
        Pattern ldapGroupSearchPatternCompiled = null;
        if (!"".equals(ldapGroupSearchPattern)) {
            ldapGroupSearchPatternCompiled = Pattern
                    .compile(ldapGroupSearchPattern);
        }

        while (groupList.hasMoreElements()) {
            SearchResult sr = (SearchResult) groupList.next();

            // TODO : should we retrieve LDAP group id and do an update of group
            // name
            // This will require to store in local db the remote id
            String groupName = (String) sr.getAttributes()
                    .get(ldapGroupSearchAttribute).get();
            String groupDesc = "LDAP group with CN:" + groupName;
            if (null != sr.getAttributes().get(ldapGroupDescAttribute)) {
                groupDesc = (String) sr.getAttributes().get(ldapGroupDescAttribute).get();
            }

            if (!"".equals(ldapGroupSearchPattern)) {
                Matcher m = ldapGroupSearchPatternCompiled.matcher(groupName);
                boolean b = m.matches();
                if (b) {
                    groupName = m.group(1);
                }
            }
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                Log.debug(Geonet.LDAP, "synchronizeGroup syncing group: " + groupName);
            }

            Element groupIdRequest = dbms.select(
                    "SELECT id FROM Groups WHERE name = ?", groupName);
            Element groupRecord = groupIdRequest.getChild("record");
            String groupId = null;

            if (groupRecord == null) {
                if (Log.isDebugEnabled(Geonet.LDAP)) {
                    Log.debug(Geonet.LDAP, "  - Add non existing group '"
                            + groupName + "' in local database.");
                }
                // If LDAP group does not exist in local database, create it
                groupId = serialFactory.getSerial(dbms, "Groups") + "";
                String query = "INSERT INTO GROUPS(id, name, description) VALUES(?,?,?)";
                dbms.execute(query, Integer.valueOf(groupId), groupName, groupDesc);
                Lib.local.insert(dbms, "Groups", Integer.valueOf(groupId),
                        groupName);
            } else if (groupRecord != null) {
                if (Log.isDebugEnabled(Geonet.LDAP)) {
                    Log.debug(Geonet.LDAP, "  - Update existing group '"
                            + groupName + "' in local database.");
                }
                groupId = groupRecord.getChildText("id");
                // Update description from configured attribute value
                String query = "UPDATE GROUPS set description = ? where id = ? and name = ?";
                dbms.execute(query, groupDesc, Integer.valueOf(groupId), groupName);
            }
            Double groupIdDbl = Double.parseDouble(groupId);
            Integer groupIdInt = groupIdDbl.intValue();
            // update usergroups??
            List<String> userMemberDns = new ArrayList<String>();
            if (null != sr.getAttributes().get("member")) {
                // NamingEnumeration<Attribute> memberList = (NamingEnumeration<Attribute>) sr.getAttributes().get("member").getAll();
                //while (allAttr.hasMore()) {
                for (NamingEnumeration allAttr = sr.getAttributes().getAll(); allAttr.hasMoreElements(); ) {
                    Attribute attr = (Attribute) allAttr.next();
                    //Log.debug(Geonet.LDAP, " attribute ID: " + attr.getID());
                    if (attr.getID().equalsIgnoreCase("member")) {
                        for (NamingEnumeration vals = attr.getAll(); vals.hasMoreElements(); ) {
                            String memberDN = (String) vals.next();
                            Log.debug(Geonet.LDAP, "synchronizeGroup memberDN: " + memberDN);
                            // Get this member's username to query Geo DB
                            String memberUserFilter = "(&(objectClass=user)(distinguishedName=" + memberDN + "))";
                            if (Log.isDebugEnabled(Geonet.LDAP)) {
                                Log.debug(Geonet.LDAP, "LDAPSynchronizerJob ldapUserSearchBase: " + ldapUserSearchBase);
                                Log.debug(Geonet.LDAP, "LDAPSynchronizerJob memberUserFilter: " + memberUserFilter);
                                Log.debug(Geonet.LDAP, "LDAPSynchronizerJob ldapUserSearchAttribute: " + ldapUserSearchAttribute);
                            }
                            NamingEnumeration<?> userList = dc.search(ldapUserSearchBase, memberUserFilter, null);
                            if (Log.isDebugEnabled(Geonet.LDAP)) {
                                Log.debug(Geonet.LDAP, "synchronizeUser search has results: " + userList.hasMore());
                            }
                            // Build a list of LDAP users
                            Map<String, List<String>> userMemberOfMap = new HashMap<String, List<String>>();
                            Map<String, String> userProfileMap = new HashMap<String, String>();
                            int appendedCnt = 0;
                            try {
                                while (userList.hasMore()) {
                                    Object obj2 = userList.next();
                                    if (obj2 != null) {
                                        SearchResult sr2 = (SearchResult) obj2;
                                        if (sr2.getAttributes() != null && sr2.getAttributes().get(ldapUserSearchAttribute) != null) {
                                            Attribute attribute = sr2.getAttributes().get(ldapUserSearchAttribute);
                                            String attributeValue = (String) attribute.get();
                                            // get the profile attribute using the users CN
                                            userMemberOfMap.put(attributeValue, null);
                                            String cnAttributeValue = ((sr2.getAttributes().get("cn") != null) ? ((String) (sr2.getAttributes().get("cn").get())) : "");
                                            String profile = null;
                                            if (cnAttributeValue.length() > 0) {
                                                profile = this.getHighestProfile(attributeValue, cnAttributeValue);
                                                if (profile != null && profile.length() > 0) {
                                                    userProfileMap.put(attributeValue, profile);
                                                } else {
                                                    userProfileMap.put(attributeValue, Geonet.Profile.REGISTERED_USER);
                                                }
                                            }
                                            Log.debug(Geonet.LDAP, "synchronizeGroup member with username: " + attributeValue);
                                            ++appendedCnt;
                                            // what is this user a member of
                                            if (sr2.getAttributes().get("memberOf") != null && sr2.getAttributes().get("memberOf").getAll() != null) {
                                                List<String> memberOfDNList = new ArrayList<String>();
                                                for (NamingEnumeration memberOfAttrVals = sr2.getAttributes().get("memberOf").getAll(); memberOfAttrVals.hasMoreElements(); ) {
                                                    Object obj3 = memberOfAttrVals.next();
                                                    if (obj3 != null) {
                                                        String memberOfDN = (String) obj3;
                                                        if (memberOfDN.indexOf(ldapGroupSearchBase) != -1) {
                                                            Log.debug(Geonet.LDAP, "synchronizeGroup check group membership memberOfDN: " + memberOfDN);
                                                            memberOfDNList.add(memberOfDN);
                                                        }
                                                    }
                                                }
                                                if (memberOfDNList.size() > 0)
                                                    userMemberOfMap.put(attributeValue, memberOfDNList);
                                            }
                                        } else {
                                            Log.debug(Geonet.LDAP, "synchronizeGroup member ldap object does not contain required attribute: " + ldapUserSearchAttribute);
                                        }
                                    } else {
                                        Log.debug(Geonet.LDAP, "synchronizeGroup member userLost contained null object");
                                    }
                                }
                            } catch (javax.naming.SizeLimitExceededException slee) {
                                Log.error(
                                        Geonet.LDAP,
                                        "  - Unexpected error while processing LDAP user list; appendedCnt:" + appendedCnt,
                                        slee);
                            }
                            if (userMemberOfMap.size() == 0) {
                                Log.error(Geonet.LDAP, "No usernames found using search memberUserFilter: " + memberUserFilter);
                            } else if (userMemberOfMap.size() > 1) {
                                Log.error(Geonet.LDAP, "Multiple usernames found using search memberUserFilter: " + memberUserFilter);
                            } else {
                                String userName = userMemberOfMap.keySet().iterator().next();
                                Element userIdRequest = dbms.select(
                                        "SELECT id FROM Users WHERE authtype=? AND username=?", LDAPConstants.LDAP_FLAG, userName);
                                Element userRecord = userIdRequest.getChild("record");
                                Integer userId = null;
                                if (userRecord == null) {
                                    // not creating accounts here, wait for user to initiate logon attempt for auto account creation.
                                    Log.error(Geonet.LDAP, "  - No LDAP user found with username: " + userName);
                                } else {
                                    Double userIdDbl = Double.parseDouble(userRecord.getChildText("id"));
                                    userId = userIdDbl.intValue();
                                    Element userGroupsRequest = dbms.select(
                                            "SELECT groupid, profile FROM UserGroups WHERE userid=?", userId);
                                    List<Element> userGroupsRecords = userGroupsRequest.getChildren("record");
                                    List<Integer> memberOfGroupIds = new ArrayList<Integer>();
                                    List<String> profiles = new ArrayList<String>();
                                    if (userGroupsRecords != null && userGroupsRecords.size() > 0) {
                                        boolean alreadyMember = false;
                                        for (Element userGroupsRecord : userGroupsRecords) {
                                            String profile = userGroupsRecord.getChildText("profile");
                                            if (!profiles.contains(profile)) profiles.add(profile);
                                            Double memberGroupIdDbl = Double.parseDouble(userGroupsRecord.getChildText("groupid"));
                                            Integer memberGroupIdInt = memberGroupIdDbl.intValue();
                                            if (!memberOfGroupIds.contains(memberGroupIdInt))
                                                memberOfGroupIds.add(memberGroupIdInt);
                                            if (memberGroupIdInt.equals(groupIdInt)) {
                                                // This is the group we're sync'ing. So already a member.
                                                alreadyMember = true;
                                            }
                                        }
                                        if (!alreadyMember) {
                                            try {
                                                Log.info(Geonet.LDAP, "  - Creating UserGroups entry for user: " + userName
                                                        + ", group:" + groupName + ", profile:" + userProfileMap.get(userName));
                                                String query = "INSERT INTO USERGROUPS(userId, groupId, profile) VALUES(?,?,?)";
                                                dbms.execute(query, userId, groupIdInt, userProfileMap.get(userName));
                                            } catch (Exception e) {
                                                Log.error(Geonet.LDAP, "  - Exception creating UserGroups entry for user: " + userName
                                                        + ", group:" + groupName + ", profile:" + userProfileMap.get(userName) + " - " + e.getMessage());
                                            }
                                        }
                                    }
                                    // check existing against existing ldap group membership
                                    for (String memberOfDN : userMemberOfMap.get(userName)) {
                                        int startIndex = memberOfDN.toLowerCase().lastIndexOf("cn=") + 3;
                                        int endIndex = memberOfDN.substring(startIndex).toLowerCase().indexOf(",");
                                        String cn = memberOfDN.substring(startIndex).substring(0, endIndex);
                                        boolean matchedGroupMembership = false;
                                        for (Integer memberOfGroupId : memberOfGroupIds) {
                                            Element memberOfGroupRequest = dbms.select(
                                                    "SELECT id, name FROM Groups WHERE id = ?", memberOfGroupId);
                                            String name = memberOfGroupRequest.getChild("record").getChildText("name");
                                            if (cn.equalsIgnoreCase(name)) {
                                                matchedGroupMembership = true;
                                                break;
                                            }
                                        }
                                        if (!matchedGroupMembership) {
                                            Element cnInDbGroupRequest = dbms.select(
                                                    "SELECT id, name FROM Groups WHERE name = ?", cn);
                                            Element cnInDbGroupRecord = cnInDbGroupRequest.getChild("record");
                                            if (cnInDbGroupRecord != null) {
                                                try {
                                                    Integer cnInDbGroupId = Integer.parseInt(cnInDbGroupRequest.getChild("record").getChildText("id"));
                                                    Log.info(Geonet.LDAP, "Creating secondary UserGroups entry for user: "
                                                            + userName + ", group:" + groupName + ", profile:" + userProfileMap.get(userName));
                                                    String query = "INSERT INTO USERGROUPS(userId, groupId, profile) VALUES(?,?,?)";
                                                    dbms.execute(query, userId, cnInDbGroupId, userProfileMap.get(userName));
                                                } catch (Exception e) {
                                                    Log.error(Geonet.LDAP, "  - Exception creating secondary UserGroups entry for user: " + userName
                                                            + ", group:" + groupName + ", profile:" + userProfileMap.get(userName) + " - " + e.getMessage());
                                                }
                                            } else {
                                                // don't create the group here, that's done earlier.
                                                Log.info(Geonet.LDAP, "LDAP group with CN:" + cn + " not yet in Geonetwork DB");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Pattern privilegeQueryPatternCompiled;
    Map<String, String> profileMapping;

    private String getHighestProfile(String username, String cn) {
        NamingEnumeration<?> ldapInfoList;
        String highestUserProfile = Geonet.Profile.REGISTERED_USER;
        try {
            ProfileManager profileManager = applicationContext
                    .getBean(ProfileManager.class);
            DirContext dc = contextSource.getReadOnlyContext();

            String privilegeQuery = (String) jdm.get("privilegeQuery");
            String privilegeObject = (String) jdm.get("privilegeObject");
            String privilegeAttribute = (String) jdm.get("privilegeAttribute");
            String privilegeQueryPattern = (String) jdm.get("privilegeQueryPattern");
            if (privilegeQueryPattern != null && privilegeQueryPattern.length() > 0) {
                this.privilegeQueryPatternCompiled = Pattern.compile(privilegeQueryPattern);
            }
            String privilegeQueryPrefix = (String) jdm.get("privilegeQueryPrefix");
            String privilegeQueryPostfix = (String) jdm.get("privilegeQueryPostfix");

            // Extract profile first
            Set<String> profileList = new HashSet<String>();
            // will be the DN of the user as the value, so use CN if you have one.
            if (cn != null && cn.length() > 0) {
                Log.debug(Geonet.LDAP, "CN: \t" + cn);
            } else {
                Log.debug(Geonet.LDAP, "No CN:");
            }
            String groupsQuery = MessageFormat.format(privilegeQuery,
                    ((cn != null && cn.length() > 0) ? cn : username));
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
                    if (privilegeQueryPrefix != null && privilegeQueryPrefix.length() > 0 &&
                            p.indexOf(privilegeQueryPrefix) != -1) {
                        p = p.substring(p.indexOf(privilegeQueryPrefix) + privilegeQueryPrefix.length());
                    }
                    if (privilegeQueryPostfix != null && privilegeQueryPostfix.length() > 0 &&
                            p.indexOf(privilegeQueryPostfix) != -1) {
                        p = p.substring(0, p.indexOf(privilegeQueryPostfix));
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
            highestUserProfile = profileManager
                    .getHighestProfile(profileList.toArray(new String[profileList.size()]));
            if (highestUserProfile != null) {
                if (Log.isDebugEnabled(Geonet.LDAP)) {
                    Log.debug(Geonet.LDAP, "  Highest user profile is "
                            + highestUserProfile);
                }
            }
        } catch (Exception e) {
            Log.warning(Geonet.LDAP, "Exception checking LDAP profile roles for username:" + username);
        }
        return highestUserProfile;
    }

    protected String getUserInfo(Map<String, ArrayList<String>> userInfo,
            String attributeName) {
        return getUserInfo(userInfo, attributeName, "");
    }

    /**
     * Return the first element of userInfo corresponding to the attribute name.
     * If attributeName mapping is not defined, return empty string. If no value
     * found in LDAP user info, return default value.
     *
     * @param userInfo
     * @param attributeName
     * @param defaultValue
     * @return
     */
    protected String getUserInfo(Map<String, ArrayList<String>> userInfo,
            String attributeName, String defaultValue) {
        String attributeMappingList = (String) userAttributeMapping.get(attributeName);
        String[] attributeMapping = attributeMappingList.split(",");
        String value = "";

        if (attributeMapping != null) {
            String ldapAttributeName = ((attributeMapping.length > 0) ? attributeMapping[0] : null);
            String configDefaultValue = ((attributeMapping.length > 1) ? attributeMapping[1] : null);

            if (ldapAttributeName != null
                    && userInfo.get(ldapAttributeName) != null
                    && userInfo.get(ldapAttributeName).get(0) != null) {
                value = userInfo.get(ldapAttributeName).get(0);
            } else if (configDefaultValue != null) {
                value = configDefaultValue;
            } else {
                value = defaultValue;
            }
        } else {
            value = defaultValue;
        }

        if (Log.isDebugEnabled(Geonet.LDAP)) {
            Log.debug(Geonet.LDAP, "LDAP attribute '" + attributeName + "' = "
                    + value);
        }
        return value;
    }

    public DefaultSpringSecurityContextSource getContextSource() {
        return contextSource;
    }

    public void setContextSource(
            DefaultSpringSecurityContextSource contextSource) {
        this.contextSource = contextSource;
    }

    public Map<String, String> getUserAttributeMapping() {
        return userAttributeMapping;
    }

    public void setUserAttributeMapping(Map<String, String> userAttributeMapping) {
        this.userAttributeMapping = userAttributeMapping;
    }
}