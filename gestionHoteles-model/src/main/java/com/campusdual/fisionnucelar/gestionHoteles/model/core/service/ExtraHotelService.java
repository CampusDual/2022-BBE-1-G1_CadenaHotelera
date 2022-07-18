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

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IExtraHotelService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IExtraService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IHotelService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ExtraDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ExtraHotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.HotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.IncorrectBooleanException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class listens the incoming requests related with the clients table
 * 
 * @since 12/07/2022
 * @version 1.0
 *
 */
@Service("ExtraHotelService")
@Lazy
public class ExtraHotelService implements IExtraHotelService {
	@Autowired
	private ExtraHotelDao extraHotelDao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;


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
	 * @exception NoResultsException     when there are not matching results on the
	 *                                   extraHotel table
	 * @exception BadSqlGrammarException when it receives an incorrect type in the
	 *                                   params
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

		} catch (NoResultsException e) {
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		}
		return searchResult;
	}

	/**
	 * 
	 * Adds a new register on the extras_hotel table.
	 * 
	 * @since 12/07/2022
	 * @param The fields of the new register
	 * @exception DuplicateKeyException           when receives an existing
	 *                                            extraHotel
	 * 
	 * @exception DataIntegrityViolationException when the params don't include the
	 *                                            not null fields or include a non
	 *                                            existing hotel or extra
	 * 
	 * @exception EmptyRequestException           when the params are empty
	 * @return The id of the new extra_hotel and a message with the operation result
	 */
	
	@Override
	public EntityResult extrahotelInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			checkIfDataIsEmpty(attrMap);
			insertResult = this.daoHelper.insert(this.extraHotelDao, attrMap);
			insertResult.setMessage("SUCCESSFUL_INSERTION");
		} catch (DuplicateKeyException e) {
			control.setErrorMessage(insertResult, "DUPLICATED_EXTRAS_IN_HOTEL");
		} catch (EmptyRequestException e) {
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DataIntegrityViolationException e) {
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			control.setErrorMessage(insertResult, "PRICE_MUST_BE_NUMERIC");
		}
		return insertResult;
	}

	/**
	 * 
	 * Updates a existing register on the extras_hotel table.
	 * 
	 * @since 12/07/2022
	 * @param The fields to be updated
	 
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
	 * @exception IncorrectBooleanException       when receive an incorrect value
	 *                                            for the exh_active field (only 1
	 *                                            for true and 0 for false admitted)
	 * @return A message with the operation result
	 */
	@Override
	public EntityResult extrahotelUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			checkIfDataIsEmpty(attrMap);
			checkIfExtraHotelExists(keyMap);

			if (attrMap.containsKey("exh_active")) {
				checkCorrectBoolean(attrMap);
			}

			updateResult = this.daoHelper.update(this.extraHotelDao, attrMap, keyMap);
			updateResult.setMessage("SUCCESSFUL_UPDATE");
		} catch (DuplicateKeyException e) {
			control.setErrorMessage(updateResult, "DUPLICATED_EXTRA_IN_HOTEL");
		} catch (DataIntegrityViolationException e) {
			control.setMessageFromException(updateResult, e.getMessage());
		} catch (RecordNotFoundException | IncorrectBooleanException | EmptyRequestException e) {
			control.setErrorMessage(updateResult, e.getMessage());
		}
		return updateResult;
	}

	
	
	/**
	 * Search a concrete extraHotel. It throws an exception if it doesn't exists
	 * @param keyMap The extraHotel to search
	 * @exception RecordNotFoundException If it doesn't find any result
	 */
	private boolean checkIfExtraHotelExists(Map<String, Object> keyMap) {
		if (keyMap.get("id_extras_hotel") == null) {
			throw new RecordNotFoundException("ID_EXTRA_HOTEL_REQUIRED");
		}
		List<String> fields = new ArrayList<>();
		fields.add("id_extras_hotel");
		EntityResult existingExtrasHotel = daoHelper.query(extraHotelDao, keyMap, fields);
		if (existingExtrasHotel.isEmpty())
			throw new RecordNotFoundException("ERROR_EXTRA_IN_HOTEL_NOT_FOUND");
		return existingExtrasHotel.isEmpty();
	}

	/**
	 * Throws an exception if the params introduce by the users are empty
	 * 
	 * @param attrMap The params of the user
	 * @exception EmptyRequestException if the params are empty
	 */

	private void checkIfDataIsEmpty(Map<String, Object> attrMap) {
		if (attrMap.isEmpty()) {
			throw new EmptyRequestException("ANY_FIELDS_REQUIRED");
		}
	}
	/**
	 * Checks if the user has introduced a valid value for the exh_active field
	 * 
	 * @param attrMap A numeric boolean to indicate if the extraHotel is active (1) or not (0)
	 * @throws IncorrectBooleanException if the value is different than 1 or 0
	 */
	private void checkCorrectBoolean(Map<String, Object> attrMap) throws IncorrectBooleanException {
		if ((int) attrMap.get("exh_active") > 1 || (int) attrMap.get("exh_active") < 0) {
			throw new IncorrectBooleanException("EXH_ACTIVE_MUST_BE_1_OR_0");
		}
	}
}
//	private boolean checkIfExtraExists(Map<String, Object> attrMap) {
//		List<String> attrList = new ArrayList<>();
//		attrList.add("id_extra");
//		Map<String, Object> keyMap = new HashMap<>();
//		keyMap.put("id_extra", attrMap.get("exh_extra"));
//		EntityResult existingExtra = daoHelper.query(extraDao, keyMap, attrList);
//		if (existingExtra.isEmpty())
//			throw new RecordNotFoundException("EXTRA_DOESN'T_EXISTS");
//		return existingExtra.isEmpty();
//	}
//	
//	private boolean checkIfHotelExists(Map<String, Object> attrMap) {
//		List<String> attrList = new ArrayList<>();
//		attrList.add("id_hotel");
//		Map<String, Object> keyMap = new HashMap<>();
//		keyMap.put("id_hotel", attrMap.get("exh_hotel"));
//		EntityResult existingHotel = daoHelper.query(hotelDao, keyMap, attrList);
//		if (existingHotel.isEmpty())
//			throw new RecordNotFoundException("HOTEL_DOESN'T_EXISTS");
//		return existingHotel.isEmpty();
//	}
//}
