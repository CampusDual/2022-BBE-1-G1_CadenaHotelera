package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;

import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * This interface defines the queries over the room pics table
 *@since 22/08/2022
 *@version 1.0 
 *
 */
public interface IRoomPicService {

	 // ROOM PICS
	 public byte[] roompicQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;
	 public EntityResult roompicInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
	
	
	}