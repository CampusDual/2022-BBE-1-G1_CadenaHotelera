package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;
/**
 * This class represents the clients
 *@since 27/06/2022
 *@version 1.0 
 *
 */
@Repository("ClientDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/ClientDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")
public class ClientDao extends OntimizeJdbcDaoSupport {

	public static final String ATTR_ID = "ID_CLIENT";
	public static final String ATTR_NIF = "CL_NIF";
	public static final String ATTR_NAME = "CL_NAME";
	public static final String ATTR_EMAIL = "CL_EMAIL";
	public static final String ATTR_PHONE = "CL_PHONE";

}