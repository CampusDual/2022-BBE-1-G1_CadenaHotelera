package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;

@Repository("RoomDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/RoomDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")
public class RoomDao extends OntimizeJdbcDaoSupport {

 public static final String ATTR_ID = "ID_ROOM";
 public static final String ATTR_ROOM_TYPE = "RM_ROOM_TYPE"; 
 public static final String ATTR_HOTEL = "RM_HOTEL";
 public static final String ATTR_NUMBER = "RM_NUMBER";


}