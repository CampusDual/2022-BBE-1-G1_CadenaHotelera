package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IHotelService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomTypeService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.RoomDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.IncorrectBooleanException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
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
 * This class builds the operations over the rooms table
 * 
 * @since 27/06/2022
 * @version 1.0
 *
 */
@Service("RoomService")
@Lazy
public class RoomService implements IRoomService {

	private Logger log;

	private Control control;
	private UserControl userControl;

	@Autowired
	private RoomDao roomDao;

	@Autowired
	private IHotelService hotelService;

	@Autowired
	private IRoomTypeService roomTypeService;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;
	Validator validator;
	
	public RoomService() {
		super();
		this.control = new Control();
		this.validator = new Validator();
		this.userControl=new UserControl();
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * 
	 * Executes a generic query over the rooms table
	 * 
	 * @since 27/06/2022
	 * @param The filters and the fields of the query
	 * @return The columns from the room table especified in the params and a
	 *         message with the operation result
	 * @exception NoResultsException     when there are not matching results on the
	 *                                   extraHotel table
	 * @exception BadSqlGrammarException when it receives an incorrect type in the
	 *                                   params
	 * @exception NotAuthorizedException
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult roomQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {

		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = daoHelper.query(roomDao, keyMap, attrList);
			control.checkResults(searchResult);
			userControl.controlAccess((int) searchResult.getRecordValues(0).get("rm_hotel"));
			

		} catch (NoResultsException | NotAuthorizedException e) {
			log.error("unable to retrieve a room. Request : {} {} ", keyMap, attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve a room. Request : {} {} ", keyMap, attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		}
		return searchResult;
	}

	/**
	 * 
	 * Adds a new register on the rooms table.We assume that we are receiving the
	 * correct fields
	 * 
	 * @since 27/06/2022
	 * @param The fields of the new register
	 * @return The id of the new register and a message with the operation result
	 * @exception DuplicateKeyException           when receives an existing
	 *                                            extraHotel
	 * 
	 * @exception DataIntegrityViolationException when the params don't include the
	 *                                            not null fields or include a non
	 *                                            existing hotel or extra
	 * 
	 * @exception EmptyRequestException           when the params are empty
	 * @exception NotAuthorizedException
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult roomInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			validator.checkIfMapIsEmpty(attrMap);
			userControl.controlAccess((int) attrMap.get("rm_hotel"));
			insertResult = this.daoHelper.insert(this.roomDao, attrMap);
			insertResult.setMessage("SUCCESSFUL_INSERTION");
		} catch (DuplicateKeyException e) {
			log.error("unable to insert a room. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, "ROOM_ALREADY_EXISTS");
		} catch (EmptyRequestException|NotAuthorizedException e) {
			log.error("unable to insert a room. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert a room. Request : {} ",attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve a room : {} ",attrMap, e);
			control.setErrorMessage(insertResult, "FIELDS_MUST_BE_NUMERIC");
		}
		
		return insertResult;
	}

//	
	/**
	 * 
	 * Updates a existing register on the rooms table. We assume that we are
	 * receiving the correct fields
	 * 
	 * @since 27/06/2022
	 * @param The fields to be updated
	 * @return A message with the operation result
	 * @exception DuplicateKeyException           when trying to change a not null
	 *                                            field of an extraHotel for an
	 *                                            existing one
	 * 
	 * @exception EmptyRequestException           when the params are empty
	 * 
	 * @exception RecordNotFoundException         when receives a non existing
	 *                                            extraHotel to update
	 * 
	 * @exception DataIntegrityViolationException when the params don't include the
	 *                                            not null fields or include a non
	 *                                            existing hotel or extra
	 * 
	 * @exception NotAuthorizedException
	 * @exception BadSqlGrammarException
	 *
	 *                                      
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult roomUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		EntityResult hotelResult = new EntityResultMapImpl();
		try {
			validator.checkIfMapIsEmpty(attrMap);
			checkIfRoomExists(keyMap);
			hotelResult=daoHelper.query(roomDao, keyMap, Arrays.asList("rm_hotel"));
			userControl.controlAccess((int) hotelResult.getRecordValues(0).get("rm_hotel"));
			
			updateResult = this.daoHelper.update(this.roomDao, attrMap, keyMap);
			updateResult.setMessage("SUCCESSFUL_UPDATE");
		} catch (DuplicateKeyException e) {
			log.error("unable to update a room. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, "ROOM_ALREADY_EXISTS");
		} catch (RecordNotFoundException|NotAuthorizedException e) {
			log.error("unable to update a room. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (EmptyRequestException e) {
			log.error("unable to update a room. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		}catch (DataIntegrityViolationException e) {
			log.error("unable update a room. Request : {} ",attrMap, e);
			control.setMessageFromException(updateResult, e.getMessage());
		}catch (BadSqlGrammarException e) {
			log.error("unable to retrieve a room : {} ",attrMap, e);
			control.setErrorMessage(updateResult, "FIELDS_MUST_BE_NUMERIC");
		}
		return updateResult;
	}

	private boolean checkIfRoomExists(Map<String, Object> attrMap) {
		if (attrMap.get("id_room") == null) {
			throw new RecordNotFoundException("ID_ROOM_REQUIRED");
		}
		List<String> attrList = new ArrayList<>();
		attrList.add("id_room");
		EntityResult existingRoom = daoHelper.query(roomDao, attrMap, attrList);
		if (existingRoom.isEmpty())
			throw new RecordNotFoundException("ROOM_DOESN'T_EXISTS");
		return existingRoom.isEmpty();
	}

}
