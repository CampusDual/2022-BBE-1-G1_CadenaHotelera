package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;

/**
 * This class represents the reviews
 *@since 19/08/2022
 *@version 1.0 
 *
 */
@Repository("ReviewDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/ReviewDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")
public class ReviewDao extends OntimizeJdbcDaoSupport {

	public static final String ATTR_ID = "ID_REVIEW";
	public static final String ATTR_HOTEL = "RV_HOTEL";
	public static final String ATTR_CLIENT = "RV_CLIENT";
	public static final String ATTR_RATING = "RV_RATING";
	public static final String ATTR_COMMENT = "RV_COMMENT";	
	public static final String ATTR_DATE = "RV_DATE";

	
}