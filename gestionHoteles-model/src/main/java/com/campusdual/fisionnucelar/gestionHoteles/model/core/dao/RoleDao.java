package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;

/**
 * This class represents the roles 
 *@since 09/08/2022
 *@version 1.0 
 *
 */
@Repository(value = "RoleDao")
@Lazy
@ConfigurationFile(
	configurationFile = "dao/RoleDao.xml",
	configurationFilePlaceholder = "dao/placeholders.properties")
public class RoleDao  extends OntimizeJdbcDaoSupport {
	public static final String ID_ROLENAME = "id_rolename";
	public static final String ROLENAME = "rolename";
	public static final String XMLCLIENTPERMISSION = "xmlclientpermission";
}
