package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * This interface defines the queries over the service table
 *@since 07/07/2022
 *@version 1.0 
 *
 */
public interface IServiceService {
	 // Service
	 public EntityResult serviceQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;
	 public EntityResult serviceInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
	 public EntityResult serviceUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap) throws OntimizeJEERuntimeException;
	// public EntityResult serviceDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException;

}
