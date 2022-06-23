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
 public static final String ATTR_NOMBRE = "HTL_NOMBRE"; 
 public static final String ATTR_DIRECCION = "HTL_DIRECCION";
 public static final String ATTR_TELEFONO = "HTL_TELEFONO";
 public static final String ATTR_EMAIL = "HTL_EMAIL";



}