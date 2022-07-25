package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.ArrayList;
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
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
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
	
	private Validator dataValidator;
	
	private Logger log;

	public ExtraHotelService() {
		super();
		this.control = new Control();
		this.dataValidator=new Validator();
		this.log = LoggerFactory.getLogger(this.getClass());
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
			log.error("unable to retrieve an extra hotel. Request : {} {} ",keyMap,attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve an extra hotel. Request : {} {} ",keyMap,attrList, e);
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
			dataValidator.checkIfMapIsEmpty(attrMap);
			insertResult = this.daoHelper.insert(this.extraHotelDao, attrMap);
			insertResult.setMessage("SUCCESSFUL_INSERTION");
		} catch (DuplicateKeyException e) {
			log.error("unable to insert an extra hotel. Request : {} ",attrMap, e);
			control.setErrorMessage(insertResult, "DUPLICATED_EXTRAS_IN_HOTEL");
		} catch (EmptyRequestException e) {
			log.error("unable to insert an extra hotel. Request : {} ",attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert an extra hotel. Request : {} ",attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to insert an extra hotel. Request : {} ",attrMap, e);
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
	 * 
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
			dataValidator.checkIfMapIsEmpty(attrMap);
			checkIfExtraHotelExists(keyMap);

			if (attrMap.containsKey("exh_active")) {
				checkCorrectBoolean(attrMap);
			}

			updateResult = this.daoHelper.update(this.extraHotelDao, attrMap, keyMap);
			updateResult.setMessage("SUCCESSFUL_UPDATE");
		} catch (DuplicateKeyException e) {
			log.error("unable to update an extra hotel. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, "DUPLICATED_EXTRA_IN_HOTEL");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to update an extra hotel. Request : {} {} ",keyMap,attrMap, e);
			control.setMessageFromException(updateResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to update an extra hotel. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, "PRICE_MUST_BE_NUMERIC");
		} catch (RecordNotFoundException | IncorrectBooleanException | EmptyRequestException e) {
			log.error("unable to update an extra hotel. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		}
		return updateResult;
	}

	/**
	 * Search a concrete extraHotel. It throws an exception if it doesn't exists
	 * 
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
	 * Checks if the user has introduced a valid value for the exh_active field
	 * 
	 * @param attrMap A numeric boolean to indicate if the extraHotel is active (1)
	 *                or not (0)
	 * @throws IncorrectBooleanException if the value is different than 1 or 0
	 */
	private void checkCorrectBoolean(Map<String, Object> attrMap) throws IncorrectBooleanException {
		if ((int) attrMap.get("exh_active") > 1 || (int) attrMap.get("exh_active") < 0) {
			throw new IncorrectBooleanException("EXH_ACTIVE_MUST_BE_1_OR_0");
		}
	}
}
