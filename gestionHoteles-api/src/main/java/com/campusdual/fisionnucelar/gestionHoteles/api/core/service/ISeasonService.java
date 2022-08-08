package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;

import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * This interface defines the queries over the seasons table
 * 
 * @since 05/08/2022
 * @version 1.0
 *
 */
public interface ISeasonService {
	// Season
	public EntityResult seasonQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException;

	public EntityResult seasonInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;

	public EntityResult seasonUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException;
	
	public EntityResult seasonDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException;
	
	 
	public EntityResult hotelseasonsDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException;
	 
}
