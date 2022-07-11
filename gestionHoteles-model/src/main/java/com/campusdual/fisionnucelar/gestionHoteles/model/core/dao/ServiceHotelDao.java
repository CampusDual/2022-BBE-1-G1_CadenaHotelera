package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;
/**
 * This class represents the services_hotel
 *@since 08/07/2022
 *@version 1.0 
 *
 */
@Repository("ServiceHotelDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/ServiceHotelDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")

public class ServiceHotelDao extends OntimizeJdbcDaoSupport{
	public static final String ATTR_ID = "ID_SERVICES_HOTEL";
	public static final String ATTR_HOTEL = "SVH_HOTEL";
	public static final String ATTR_SERVICE = "SVH_SERVICE";
	public static final String ATTR_ACTIVE = "SVH_ACTIVE";
}
