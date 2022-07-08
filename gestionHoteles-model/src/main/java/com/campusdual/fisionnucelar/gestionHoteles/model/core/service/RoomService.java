package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IHotelService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomTypeService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.RoomDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

import utilities.Control;

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

	public RoomService() {
		super();
		this.control = new Control();
	}

	private Control control;

	@Autowired
	private RoomDao roomDao;

	@Autowired
	private IHotelService hotelService;

	@Autowired
	private IRoomTypeService roomTypeService;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

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
	public EntityResult roomQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {

		EntityResult searchResult = this.daoHelper.query(this.roomDao, keyMap, attrList);
		if (searchResult.getCode() != EntityResult.OPERATION_SUCCESSFUL) {
			searchResult.setMessage("ERROR_WHILE_SEARCHING");
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
	public EntityResult roomInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			if (!checkIfHotelExists(attrMap)) {
				control.setErrorMessage(insertResult, "HOTEL_DOESNT_EXISTS");
			} else if (!checkIfRoomTypeExists(attrMap)) {
				control.setErrorMessage(insertResult, "ROOM_TYPE_DOESNT_EXISTS");
			} else {
				insertResult = this.daoHelper.insert(this.roomDao, attrMap);
			}
		} catch (DuplicateKeyException e) {
			control.setErrorMessage(insertResult, "ROOM_ALREADY_EXISTS");
			
			// a esta no llega
		}catch (DataIntegrityViolationException e) {
			control.setErrorMessage(insertResult, "ALL_FIELDS_REQUIRED");
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
	public EntityResult roomUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			if (!checkIfRoomExists(keyMap)) {
				control.setErrorMessage(updateResult, "ROOM_DOESNT_EXISTS");
			} else if (!checkIfHotelExists(attrMap)) {
				control.setErrorMessage(updateResult, "HOTEL_DOESNT_EXISTS");
			} else if (!checkIfRoomTypeExists(attrMap)) {
				control.setErrorMessage(updateResult, "ROOM_TYPE_DOESNT_EXISTS");
			} else {
				updateResult = this.daoHelper.insert(this.roomDao, attrMap);
			}
		} catch (DuplicateKeyException e) {
			control.setErrorMessage(updateResult, "ROOM_ALREADY_EXISTS");
		}
		return updateResult;
	}

	private boolean checkIfRoomExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_room");
		EntityResult existingRoom = roomQuery(attrMap, attrList);
		return !(existingRoom.isEmpty());
	}

	private boolean checkIfHotelExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_hotel");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_hotel", attrMap.get("rm_hotel"));
		EntityResult existingHotel = hotelService.hotelQuery(keyMap, attrList);
		return !(existingHotel.isEmpty());
	}

	private boolean checkIfRoomTypeExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_room_type");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_room_type", attrMap.get("rm_room_type"));

		EntityResult existingRoomType = roomTypeService.roomtypeQuery(keyMap, attrList);
		return !(existingRoomType.isEmpty());
	}

}
