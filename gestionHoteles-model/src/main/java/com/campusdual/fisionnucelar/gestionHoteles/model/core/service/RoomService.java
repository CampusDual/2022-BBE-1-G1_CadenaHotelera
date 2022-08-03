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
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult roomQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {

		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = daoHelper.query(roomDao, keyMap, attrList);
			userControl.controlAccess((int) searchResult.getRecordValues(0).get("rm_hotel"));
			control.checkResults(searchResult);

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
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult roomInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {			
			userControl.controlAccess((int) attrMap.get("rm_hotel"));
			
			if (attrMap.containsKey("rm_hotel")) {
				checkIfHotelExists(attrMap);
			}
			if (attrMap.containsKey("rm_room_type")) {
				checkIfRoomTypeExists(attrMap);
			}
			insertResult = this.daoHelper.insert(this.roomDao, attrMap);
			if (insertResult.isEmpty())
				throw new AllFieldsRequiredException("FIELDS_REQUIRED");

		} catch (DuplicateKeyException e) {
			log.error("unable to insert a room. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, "ROOM_ALREADY_EXISTS");
		} catch (RecordNotFoundException|NotAuthorizedException e) {
			log.error("unable to insert a room. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (AllFieldsRequiredException e) {
			log.error("unable to insert a room. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
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
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult roomUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		EntityResult hotelResult = new EntityResultMapImpl();
		try {
			checkIfRoomExists(keyMap);
			validator.checkIfMapIsEmpty(attrMap);
			hotelResult=daoHelper.query(roomDao, keyMap, Arrays.asList("rm_hotel"));
			userControl.controlAccess((int) hotelResult.getRecordValues(0).get("rm_hotel"));
						
			if (attrMap.containsKey("rm_hotel")) {
				checkIfHotelExists(attrMap);
			}
			if (attrMap.containsKey("rm_room_type")) {
				checkIfRoomTypeExists(attrMap);
			}
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
		}
		return updateResult;
	}

	private boolean checkIfRoomExists(Map<String, Object> attrMap) {
		if (attrMap.get("id_room") == null) {
			throw new RecordNotFoundException("ID_ROOM_REQUIRED");
		}
		List<String> attrList = new ArrayList<>();
		attrList.add("id_room");
		EntityResult existingRoom = roomQuery(attrMap, attrList);
		if (existingRoom.isEmpty())
			throw new RecordNotFoundException("ROOM_DOESN'T_EXISTS");
		return existingRoom.isEmpty();
	}

	private boolean checkIfHotelExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_hotel");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_hotel", attrMap.get("rm_hotel"));
		EntityResult existingHotel = hotelService.hotelQuery(keyMap, attrList);
		if (existingHotel.isEmpty())
			throw new RecordNotFoundException("HOTEL_DOESN'T_EXISTS");
		return existingHotel.isEmpty();
	}

	private boolean checkIfRoomTypeExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_room_type");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_room_type", attrMap.get("rm_room_type"));
		EntityResult existingRoomType = roomTypeService.roomtypeQuery(keyMap, attrList);
		if (existingRoomType.isEmpty())
			throw new RecordNotFoundException("ROOMTYPE_DOESN'T_EXISTS");
		return existingRoomType.isEmpty();
	}

}
