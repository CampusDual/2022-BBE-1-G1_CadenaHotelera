package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;

/**
 * This class represents the discount codes
 *@since 09/08/2022
 *@version 1.0 
 *
 */
@Repository("DiscountCodeDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/DiscountCodeDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")

public class DiscountCodeDao extends OntimizeJdbcDaoSupport {
	public static final String ATTR_ID = "ID_CODE";
	public static final String ATTR_NAME = "DC_NAME";
	public static final String ATTR_MULTIPLIER = "DC_MULTIPLIER";
	public static final String ATTR_LEAVING_DATE = "DC_LEAVING_DATE";
	
}
