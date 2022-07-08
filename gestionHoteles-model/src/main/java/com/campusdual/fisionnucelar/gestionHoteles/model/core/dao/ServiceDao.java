package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;
/**
 * This class represents the services
 *@since 07/07/2022
 *@version 1.0 
 *
 */
@Repository("ServiceDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/ServiceDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")

public class ServiceDao extends OntimizeJdbcDaoSupport {
	public static final String ATTR_ID = "ID_SERVICE";
	public static final String ATTR_NAME = "SV_NAME";
	public static final String ATTR_DESCRIPTION = "SV_DESCRIPTION";
}
