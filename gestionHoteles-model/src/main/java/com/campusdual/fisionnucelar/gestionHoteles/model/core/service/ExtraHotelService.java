package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IHotelService;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IExtraService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IExtraHotelService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ExtraHotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class listens the incoming requests related with the clients table
 *@since 12/07/2022
 *@version 1.0 
 *
 */
@Service("ExtraHotelService")
@Lazy
public class ExtraHotelService implements IExtraHotelService{
	@Autowired
	private ExtraHotelDao extraHotelDao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;
	
	@Autowired
	private IHotelService hotelService;
	
	@Autowired
	private IExtraService extraService;
	
	private Control control;

	public ExtraHotelService() {
		super();
		this.control = new Control();
	}
	/**
	   * 
	   * Executes a generic query over the extras_hotel table
	   * 
	   * @since 12/07/2022
	   * @param The filters and the fields of the query
	   * @return The columns from the extra-hotel table especified in the params and a
	   *         message with the operation result
	   */
	@Override
	public EntityResult extrahotelQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = daoHelper.query(extraHotelDao, keyMap, attrList);
			control.checkResults(searchResult);
			
		}catch (NoResultsException e) {
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		}
//		if(searchResult.isEmpty()) {
//			searchResult.setMessage("NOT_EXTRAS_IN_HOTEL");
//		}
		return searchResult;
	}
	/**
	 * 
	 * Adds a new register on the extras_hotel table.
	 * 
	 * @since 12/07/2022
	 * @param The fields of the new register
	 * @return The id of the new extra_hotel and a message with the operation result
	 */
	@Override
	public EntityResult extrahotelInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			if (attrMap.containsKey("exh_hotel")) {
				checkIfHotelExists(attrMap);
			}
			if (attrMap.containsKey("exh_extra")) {
				checkIfExtraExists(attrMap);
			}
			insertResult= this.daoHelper.insert(this.extraHotelDao, attrMap);
			if(insertResult.isEmpty()) throw new AllFieldsRequiredException("FIELDS_REQUIRED");
			
			insertResult.setMessage("SUCCESSFUL_INSERTION");
		}catch (DuplicateKeyException e) {
			control.setErrorMessage(insertResult, "DUPLICATED_EXTRAS_IN_HOTEL");
		}catch (RecordNotFoundException e) {
				control.setErrorMessage(insertResult, e.getMessage());
		}catch (DataIntegrityViolationException e) {
			control.setErrorMessage(insertResult, "EXTRA_PRICE_AND_HOTEL_REQUIRED");
		}catch (AllFieldsRequiredException e) {
			control.setErrorMessage(insertResult, e.getMessage());
		}
	return insertResult;
	}
	/**
	 * 
	 * Updates a existing register on the extras_hotel table.
	 * 
	 * @since 12/07/2022
	 * @param The fields to be updated
	 * @return A message with the operation result
	 */
	@Override
	public EntityResult extrahotelUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			checkIfDataIsEmpty(attrMap);
			checkIfExtraHotelExists(keyMap);	
			if (attrMap.containsKey("exh_hotel")) {
				checkIfHotelExists(attrMap);
			}
			if (attrMap.containsKey("exh_service")) {
				checkIfExtraExists(attrMap);
			}
			
			updateResult = this.daoHelper.update(this.extraHotelDao, attrMap, keyMap);
			updateResult.setMessage("SUCCESSFUL_UPDATE");		
		}catch (DuplicateKeyException e) {
			control.setErrorMessage(updateResult, "DUPLICATED_EXTRA_IN_HOTEL");
		}catch (RecordNotFoundException e) {
			control.setErrorMessage(updateResult, e.getMessage());
		}catch (EmptyRequestException e) {
			control.setErrorMessage(updateResult, e.getMessage());
		}
		return updateResult;
	}

	private boolean checkIfExtraHotelExists(Map<String, Object> keyMap) {
		if(keyMap.get("id_extras_hotel")==null) {
			throw new RecordNotFoundException("ID_EXTRA_HOTEL_REQUIRED");
		}
		List<String> fields = new ArrayList<>();
		fields.add("id_extras_hotel");
		EntityResult existingExtrasHotel = daoHelper.query(extraHotelDao, keyMap, fields);	
		if(existingExtrasHotel.isEmpty()) throw new RecordNotFoundException("ERROR_EXTRA_IN_HOTEL_NOT_FOUND");
		return existingExtrasHotel.isEmpty();
	}
	private boolean checkIfHotelExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_hotel");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_hotel", attrMap.get("exh_hotel"));
		EntityResult existingHotel = hotelService.hotelQuery(keyMap, attrList);
		if(existingHotel.isEmpty()) throw new RecordNotFoundException("HOTEL_DOESN'T_EXISTS");
		return existingHotel.isEmpty();
	}

	private void checkIfDataIsEmpty(Map<String, Object> attrMap) {
		if(attrMap.get("exh_hotel")==null && attrMap.get("exh_extra")==null && attrMap.get("exh_price")==null && attrMap.get("exh_active")==null) {
			throw new EmptyRequestException("ANY_FIELDS_REQUIRED");
		}
	}
	private boolean checkIfExtraExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_extra");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_extra", attrMap.get("exh_extra"));
		EntityResult existingExtra = extraService.extraQuery(keyMap, attrList);
		if(existingExtra.isEmpty()) throw new RecordNotFoundException("EXTRA_DOESN'T_EXISTS");
		return existingExtra.isEmpty();
	}
}
