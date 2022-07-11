package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * This interface defines the queries over the extra table
 *@since 08/07/2022
 *@version 1.0 
 *
 */
public interface IExtraService {
	 // Extra
	 public EntityResult extraQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;
	 public EntityResult extraInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
	 public EntityResult extraUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap) throws OntimizeJEERuntimeException;
	// public EntityResult extraDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException;

}
