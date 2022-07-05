package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IHotelService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.RoomDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
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

	@Autowired
	private RoomDao roomDao;

	@Autowired
	private IHotelService hotelService;

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
		EntityResult insertResult = this.daoHelper.insert(this.roomDao, attrMap);
		if (insertResult.getCode() != EntityResult.OPERATION_SUCCESSFUL) {
			insertResult.setMessage("ERROR_WHILE_INSERTING");
		} else {
			insertResult.setMessage("SUCCESSFUL_INSERTION");
		}
		return insertResult;
	}

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
		EntityResult updateResult = this.daoHelper.update(this.roomDao, attrMap, keyMap);
		if (updateResult.getCode() != EntityResult.OPERATION_SUCCESSFUL) {
			updateResult.setMessage("ERROR_WHILE_UPDATING");
		} else {
			updateResult.setMessage("SUCCESSFUL_UPDATE");
		}
		return updateResult;
	}


}