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

import jeeves.guiservices.session.JeevesUser;
import jeeves.server.ProfileManager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class LDAPUser extends JeevesUser {
	
	private static final long serialVersionUID = -5390558007347570517L;
	
	private Multimap<String, String> groupsAndProfile = HashMultimap.create();
    private String cn;
    private String dn;
	
	public LDAPUser(ProfileManager profileManager, String username) {
		super(profileManager);
		setUsername(username);
		// FIXME Should we here populate the LDAP user with LDAP attributes instead of in the GNLDAPUserDetailsMapper ?
		// TODO : populate userId which should be in session
	}
	
	public void addPrivilege(String group, String profile) {
		groupsAndProfile.put(group, profile);
	}
	public void setPrivileges(Multimap<String, String> privileges) {
		groupsAndProfile = privileges;
	}
	public Multimap<String, String> getPrivileges() {
		return groupsAndProfile;
	}

    public String getCn() {
        return cn;
    }

    public LDAPUser setCn(String cn) {
        if (cn == null) cn = "";
        this.cn = cn;
        return this;
    }

    public String getDn() {
        return dn;
    }

    public LDAPUser setDn(String dn) {
        if (dn == null) dn = "";
        this.dn = dn;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("getName:").append(this.getName()).append(", ");
        result.append("getSurname:").append(this.getSurname()).append(", ");
        result.append("getUsername:").append(this.getUsername()).append(", ");
        result.append("getEmail:").append(this.getEmail()).append(", ");
        result.append("getKind:").append(this.getKind()).append(", ");
        result.append("getOrganisation:").append(this.getOrganisation()).append(", ");
        result.append("getCountry:").append(this.getCountry()).append(", ");
        result.append("getCity:").append(this.getCity()).append(", ");
        result.append("getState:").append(this.getState()).append(", ");
        result.append("getAddress:").append(this.getAddress()).append(", ");
        result.append("getProfile:").append(this.getProfile()).append(", ");
        result.append("getKind:").append(this.getKind()).append(", ");
        result.append("getDn:").append(this.getDn()).append(", ");
        result.append("getCn:").append(this.getCn()).append(", ");
        return result.toString();
    }
}
