package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomTypeService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.RoomTypeDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class builds the operations over the roomtypes table
 * 
 * @since 27/06/2022
 * @version 1.0
 *
 */
@Service("RoomTypeService")
@Lazy
public class RoomTypeService implements IRoomTypeService {

	@Autowired
	private RoomTypeDao roomTypeDao;
	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	private Control control;
	private Validator validator;
	private Logger log;


	public RoomTypeService() {
		super();
		this.control = new Control();
		this.validator=new Validator();
		this.log = LoggerFactory.getLogger(this.getClass());
		
	}

	/**
	 * 
	 * Executes a generic query over the roomtypes table
	 * 
	 * @since 27/06/2022
	 * @param The filters and the fields of the query
	 * @return The columns from the roomtypes table especified in the params and a
	 *         message with the operation result
	 *@exception BadSqlGrammarException when it introduces a string instead of a numeric on id
	 *@exception NoResultsException when the query doesn´t return results
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult roomtypeQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {

		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = daoHelper.query(roomTypeDao, keyMap, attrList);
			control.checkResults(searchResult);
		} catch (NoResultsException e) {
			log.error("unable to retrieve a room type. Request : {} {} ",keyMap,attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve a room type. Request : {} {} ",keyMap,attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		}
		return searchResult;
	}

	/**
	 * 
	 * Adds a new register on the hotels table. We assume that we are receiving the
	 * correct fields
	 * 
	 * @since 27/06/2022
	 * @param The fields of the new register
	 * @return The id of the new register and a message with the operation result
	 * 
	 * @exception BadSqlGrammarException when it introduces a string instead of a numeric
	 * @exception DuplicateKeyException when it introduces a roomType that it exists
	 * @exception DataIntegrityViolationException when it doesn´t introduce a not null field 
	 * @exception AllFieldsRequiredException when it doesn´t introduce all not null field 
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult roomtypeInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			insertResult = this.daoHelper.insert(this.roomTypeDao, attrMap);
			if (insertResult.isEmpty())
				throw new AllFieldsRequiredException("FIELDS_REQUIRED");
			insertResult.setMessage("SUCESSFUL_INSERTION");
		} catch (BadSqlGrammarException e) {
			log.error("unable to insert a room type. Request : {} ",attrMap, e);
			control.setErrorMessage(insertResult, "PRICE_MUST_BE_NUMERIC");
		} catch (DuplicateKeyException e) {
			log.error("unable to insert a room type. Request : {} ",attrMap, e);
			control.setErrorMessage(insertResult, "ROOM_TYPE_ALREADY_EXISTS");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert a room type. Request : {} ",attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (AllFieldsRequiredException e) {
			log.error("unable to insert a room type. Request : {} ",attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		}
		return insertResult;
	}

	/**
	 * 
	 * Updates a existing register on the hotels table. We assume that we are
	 * receiving the correct fields
	 * 
	 * @since 27/06/2022
	 * @param The fields to be updated
	 * @return A message with the operation result
	 * 
	 * @exception BadSqlGrammarException when it introduces a string instead of a numeric
	 * @exception DuplicateKeyException when it introduces a roomType that it exists in other register
	 * @exception RecordNotFoundException when it doesn´t introduce a not null field 
	 * @exception EmptyRequestException when it doesn´t introduce any field
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult roomtypeUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			validator.checkIfMapIsEmpty(attrMap);
			checkIfRoomTypeExists(keyMap);
		
			updateResult = this.daoHelper.update(this.roomTypeDao, attrMap, keyMap);

			updateResult.setMessage("SUCESSFUL_UPDATE");
		} catch (BadSqlGrammarException e) {
			log.error("unable to update a room type. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, "PRICE_MUST_BE_NUMERIC");
		} catch (DuplicateKeyException e) {
			log.error("unable to update a room type. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, "ROOM_TYPE_ALREADY_EXISTS");
		} catch (RecordNotFoundException e) {
			log.error("unable to update a room type. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (EmptyRequestException e) {
			log.error("unable to update a room type. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		}catch (DataIntegrityViolationException e) {
			log.error("unable to update a room type. Request : {} {} ",keyMap,attrMap, e);
			control.setMessageFromException(updateResult, e.getMessage());}
		return updateResult;
	}

	
	/**
	   * 
	   * In update, check the roomType id exist in a roomType table
	   * 
	   * @since 27/06/2022
	   * @param The field roomType
	   * @return true if roomType is in a roomType table or false if it isn´t
	   * @exception RecordNotFoundException when it doesn´t introduce a not null field 
	   */
	private boolean checkIfRoomTypeExists(Map<String, Object> attrMap) {
		if (attrMap.get("id_room_type") == null) {
			throw new RecordNotFoundException("ID_ROOM_TYPE_REQUIRED");
		}
		List<String> attrList = new ArrayList<>();
		attrList.add("id_room_type");
		EntityResult existingRoomType = daoHelper.query(roomTypeDao, attrMap, attrList);
		if (existingRoomType.isEmpty())
			throw new RecordNotFoundException("ROOM_TYPE_DOESN'T_EXISTS");
		return !(existingRoomType.isEmpty());
	}

		

}