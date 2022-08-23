package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;

	
	/**
	 * This class represents the room pics
	 *@since 22/08/2022
	 *@version 1.0 
	 *
	 */
	@Repository("RoomPicDao")
	@Lazy
	@ConfigurationFile(configurationFile = "dao/RoomPicDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")
	public class RoomPicDao extends OntimizeJdbcDaoSupport {

	 public static final String ATTR_ID = "ID_ROOM_PIC";
	 public static final String ATTR_ROOM_TYPE = "RP_ROOM_TYPE"; 
	 public static final String ATTR_HOTEL = "RP_HOTEL"; 	 
	 public static final String ATTR_IMAGE = "RP_IMAGE";

}
