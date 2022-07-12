package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;
/**
 * This class represents the services_hotel
 *@since 12/07/2022
 *@version 1.0 
 *
 */
@Repository("ExtraHotelDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/ExtraHotelDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")

public class ExtraHotelDao extends OntimizeJdbcDaoSupport{
	public static final String ATTR_ID = "ID_EXTRAS_HOTEL";
	public static final String ATTR_HOTEL = "EXH_HOTEL";
	public static final String ATTR_EXTRA = "EXH_EXTRA";
	public static final String ATTR_PRICE = "EXH_PRICE";
	public static final String ATTR_ACTIVE = "EXH_ACTIVE";
}
