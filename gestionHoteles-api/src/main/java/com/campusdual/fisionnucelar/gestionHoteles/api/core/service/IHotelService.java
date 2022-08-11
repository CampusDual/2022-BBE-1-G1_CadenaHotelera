package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;

import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * This interface defines the queries over the hotel table
 * 
 * @since 27/06/2022
 * @version 1.0
 *
 */
public interface IHotelService {

	// HOTEL
	public EntityResult hotelQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException;

	public EntityResult hotelInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;

	public EntityResult hotelUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException;

	public EntityResult hotelsbyservicesQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException;

	public EntityResult searchbylocationQuery(Map<String, Object> keyMap,List<String> attrList)
			throws OntimizeJEERuntimeException;
	
	public EntityResult searchbycityQuery(Map<String, Object> keyMap,List<String> attrList) throws OntimizeJEERuntimeException;

	EntityResult searchnearbyservicesQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException; 
}
