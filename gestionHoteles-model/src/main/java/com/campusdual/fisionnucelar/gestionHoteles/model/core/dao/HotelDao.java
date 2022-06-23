package com.campusdual.fisionnucelar.gestionHoteles.model.core.dao;


import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.common.ConfigurationFile;
import com.ontimize.jee.server.dao.jdbc.OntimizeJdbcDaoSupport;

@Repository("HotelDao")
@Lazy
@ConfigurationFile(configurationFile = "dao/HotelDao.xml", configurationFilePlaceholder = "dao/placeholders.properties")
public class HotelDao extends OntimizeJdbcDaoSupport {

 public static final String ATTR_ID = "ID_HOTEL";
 public static final String ATTR_NAME = "HTL_NAME"; 
 public static final String ATTR_ADDRESS = "HTL_ADDRESS";
 public static final String ATTR_PHONE = "HTL_PHONE";
 public static final String ATTR_EMAIL = "HTL_EMAIL";


}