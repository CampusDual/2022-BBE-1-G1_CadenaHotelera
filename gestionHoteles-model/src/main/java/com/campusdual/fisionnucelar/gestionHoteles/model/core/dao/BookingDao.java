package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;

/**
 * This class represents the hotel bookings
 *@since 27/06/2022
 *@version 1.0 
 *
 */

@Repository("BookingDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/BookingDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")
public class BookingDao extends OntimizeJdbcDaoSupport {

	public static final String ATTR_ID = "ID_BOOKING";
	public static final String ATTR_CHECK_IN = "BK_CHECK_IN";
	public static final String ATTR_CHECK_OUT = "BK_CHECK_OUT";
	public static final String ATTR_ROOM = "BK_ROOM";
	public static final String ATTR_CLIENT = "BK_CLIENT";

}