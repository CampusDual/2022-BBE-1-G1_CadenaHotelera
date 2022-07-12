package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;

import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

public interface IExtraHotelService {
	 // Extra_hotel
	 public EntityResult extrahotelQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;
	 public EntityResult extrahotelInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
	 public EntityResult extrahotelUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap) throws OntimizeJEERuntimeException;

}
