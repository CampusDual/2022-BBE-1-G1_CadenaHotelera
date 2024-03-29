package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;

import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * This interface defines the queries over the booking table
 * 
 * @since 27/06/2022
 * @version 1.0
 *
 */
public interface IBookingService {
	// BOOKING
	public EntityResult bookingQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException;

	public EntityResult bookingInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;

//	public EntityResult bookingUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
//			throws OntimizeJEERuntimeException;

	public EntityResult bookingDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException;

	public EntityResult availableroomsQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException;

	public EntityResult clientbookingsQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException;

	public EntityResult clientactivebookingsQuery(Map<String, Object> keyMap, List<String> fields);

	public EntityResult addbookingextraUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException;
	
	public EntityResult cancelbookingextraUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException;
	
	public EntityResult todaycheckoutQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException;
	public EntityResult changedatesUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException;
	


}
