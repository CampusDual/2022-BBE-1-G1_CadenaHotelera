package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;

/**
 * This class represents the extras associated with the bookings
 *@since 20/07/2022
 *@version 1.0 
 *
 */

@Repository("BookingExtraHistDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/BookingExtraHistDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")
public class BookingExtraHistDao extends OntimizeJdbcDaoSupport {

	public static final String ATTR_ID = "ID_BOOKING_EXTRA";
	public static final String ATTR_BOOKING = "BKE_BOOKING";
	public static final String ATTR_NAME = "BKE_NAME";
	public static final String ATTR_QUANTITY = "BKE_QUANTITY";
	public static final String ATTR_UNIT_PRICE = "BKE_UNIT_PRICE";
	public static final String ATTR_TOTAL_PRICE = "BKE_TOTAL_PRICE";


}
