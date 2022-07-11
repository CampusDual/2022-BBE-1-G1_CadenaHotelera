package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;

/**
 * This class represents the extras
 *@since 08/07/2022
 *@version 1.0 
 *
 */
@Repository("ExtraDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/ExtraDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")

public class ExtraDao extends OntimizeJdbcDaoSupport {
	public static final String ATTR_ID = "ID_EXTRA";
	public static final String ATTR_NAME = "EX_NAME";
	public static final String ATTR_DESCRIPTION = "EX_DESCRIPTION";
}
