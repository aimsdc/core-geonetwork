//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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

package org.fao.geonet.guiservices.templates;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.SchemaMatchConflictException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.jdom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A simple service that add all metadata templates available from schemas being
 * handled in the SchemaManager
 * 
 */
public class AddDefault implements Service {

	public void init(String appPath, ServiceConfig params) throws Exception {
	}

	/**
	 * 
	 * schemaList is a list of comma separated schemas to load
	 * 
	 * @return A report on the template import with information about the status
	 *         of the insertion operation (failed|loaded).
	 */
	public Element exec(Element params, ServiceContext context)
			throws Exception {

		String schemaList = Util.getParam(params, Params.SCHEMA);
		String serviceStatus = "true";

		Element result = new Element(Jeeves.Elem.RESPONSE);
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);

		DataManager dataMan = gc.getDataManager();
		SchemaManager schemaMan = gc.getSchemamanager();

		String siteId = gc.getSiteId();
		int owner = context.getUserSession().getUserIdAsInt();

		Log.info(Geonet.DATA_MANAGER, "Loading templates for schemas "
				+ schemaList);
		String schemas[] = schemaList.split(",");

		for (String schemaName : schemas) {

			Element schema = new Element(schemaName);

			String schemaDir = schemaMan.getSchemaTemplatesDir(schemaName);
			if (schemaDir == null) {
				Log.error(Geonet.DATA_MANAGER, "Skipping - No templates?");
				continue;
			}

			File templateFiles[] = new File(schemaDir).listFiles();
			List<File> templateFilesList = new ArrayList<File>();

			if (templateFiles != null) {
				for (File file : templateFiles)
					if (file.getName().endsWith(".xml"))
						templateFilesList.add(file);
			}

			for (File temp : templateFilesList) {
				String status = "failed";
				String templateName = temp.getName();

				Element template = new Element("template");
				template.setAttribute("name", templateName);

                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER,
						" - Adding template file (for schema " + schemaName + "): " + templateName);

				try {
					Element xml = Xml.loadFile(temp);
					String uuid = UUID.randomUUID().toString();
					String isTemplate = "y";
					String title = null;
					boolean skip = false;

					if (templateName.startsWith("sub-")) {
						isTemplate = "s";
						String tryUuid = xml.getAttributeValue("uuid");
						if (tryUuid != null && tryUuid.length() > 0) uuid = tryUuid;
						title = xml.getAttributeValue("title");
						if (title == null || title.length() == 0) {
							title = templateName.substring(4,
								templateName.length() - 4);
						}
						
						// throw away the uuid and title attributes if present as they
						// cause problems for validation
						xml.removeAttribute("uuid");
						xml.removeAttribute("title");
					} else {
						try {
							String trySchemaName = schemaMan.autodetectSchema(xml); // fail if we can't find a schema
							if (!trySchemaName.equals(schemaName)) {
								status = "Template "+templateName+" identifies as schema "+trySchemaName+", but should be "+schemaName;
								skip = true;
							} else {
								String tryUuid = dataMan.extractUUID(schemaName, xml);
								if (tryUuid != null && tryUuid.length() > 0) uuid = tryUuid;
						  }
					  } catch (SchemaMatchConflictException smce) {
							status = "Schema match conflict for template "+templateName;
							skip = true;
						} catch (NoSchemaMatchesException nsme) {
							status = "Unable to match schema for template "+templateName;
							skip = true;
						} catch (Exception e) {
							status = "Problem processing schema/uuid for template "+templateName+": "+e.getMessage();
							skip = true;
						}
					}

					// Check and see whether the metadata is already present, if it is
					// then don't try and insert as some dbs (postgres) will abort
					// the transaction....
					if (dataMan.existsMetadataUuid(dbms, uuid)) {
						status = "skipped, already loaded";
					} else if (!skip) {
          	//
          	// insert metadata
          	//
          	String groupOwner = "1";
          	String docType = null, category = null, createDate = null, changeDate = null; boolean ufo = true, indexImmediate = true;
						dataMan.insertMetadata(context, dbms, schemaName, xml, context.getSerialFactory().getSerial(dbms, "Metadata"), uuid, owner, groupOwner, siteId,
                                           	isTemplate, docType, title, category, createDate, changeDate, ufo, indexImmediate);
	
						dbms.commit();
						status = "loaded";
					}
				} catch (Exception e) {
					serviceStatus = "false";
					Log.error(Geonet.DATA_MANAGER, "Error loading template: "
							+ e.getMessage());
				}
				template.setAttribute("status", status);
				schema.addContent(template);
			}
			result.addContent(schema);
		}
		result.setAttribute("status", serviceStatus);
		return result;
	}
}
