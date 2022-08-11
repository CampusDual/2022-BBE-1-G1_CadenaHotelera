package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;

/**
 * This class represents the seasons
 *@since 05/08/2022
 *@version 1.0 
 */

@Repository("SeasonDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/SeasonDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")

public class SeasonDao extends OntimizeJdbcDaoSupport  {
	
	public static final String ATTR_ID = "ID_SEASON";
	 public static final String ATTR_HOTEL = "SS_HOTEL"; 
	 public static final String ATTR_MULTIPLIER = "SS_MULTIPLIER";
	 public static final String ATTR_START_DATE = "SS_START_DATE";
	 public static final String ATTR_END_DATE = "SS_END_DATE";
	 public static final String ATTR_END_NAME = "SS_NAME";
}
