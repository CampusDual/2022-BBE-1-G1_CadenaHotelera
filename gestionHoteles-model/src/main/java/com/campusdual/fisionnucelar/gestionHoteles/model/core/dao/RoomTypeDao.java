package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;

@Repository("RoomTypeDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/RoomTypeDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")
public class RoomTypeDao extends OntimizeJdbcDaoSupport {

 public static final String ATTR_ID = "ID_ROOM_TYPE";
 public static final String ATTR_NAME = "RMT_NAME"; 
 public static final String ATTR_CAPACITY = "RMT_CAPACITY";
 public static final String ATTR_PRICE = "RMT_PRICE";


}