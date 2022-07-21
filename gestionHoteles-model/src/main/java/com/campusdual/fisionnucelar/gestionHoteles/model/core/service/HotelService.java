package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IHotelService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.HotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidEmailException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class builds the operations over the hotels table
 * 
 * @since 27/06/2022
 * @version 1.0
 *
 */
@Service("HotelService")
@Lazy
public class HotelService implements IHotelService {

	@Autowired
	private HotelDao hotelDao;
	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	private Control control;

	public HotelService() {
		super();
		this.control = new Control();
	}

	/**
	 * 
	 * Executes a generic query over the hotels table
	 * 
	 * @since 27/06/2022
	 * @param The filters and the fields of the query
	 * @return The columns from the hotels table especified in the params and a
	 *         message with the operation result
	 */
	@Override
	public EntityResult hotelQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = this.daoHelper.query(this.hotelDao, keyMap, attrList);
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
	 * Adds a new register on the hotels table. We assume that we are receiving the
	 * correct fields
	 * 
	 * @since 27/06/2022
	 * @param The fields of the new register
	 * @return The id of the new hotel and a message with the operation result
	 */
	@Override
	public EntityResult hotelInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			if (attrMap.get("htl_email") != null) {
				control.checkIfEmailIsValid(attrMap.get("htl_email").toString());
			}
			insertResult = this.daoHelper.insert(this.hotelDao, attrMap);
			if (insertResult.isEmpty())
				throw new AllFieldsRequiredException("FIELDS_REQUIRED");
			insertResult.setMessage("SUCESSFUL_INSERTION");

		} catch (InvalidEmailException e) {
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			control.setErrorMessage(insertResult, "HOTEL_NAME_OR_EMAIL_ALREADY_EXISTS");
		} catch (DataIntegrityViolationException e) {
			control.setErrorMessage(insertResult, "HOTEL_NAME_AND_EMAIL_REQUIRED");
		} catch (AllFieldsRequiredException e) {
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
	 */
	@Override
	public EntityResult hotelUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			checkIfDataIsEmpty(attrMap);
			checkIfHotelExists(keyMap);
			if (attrMap.get("htl_email") != null) {
				control.checkIfEmailIsValid(attrMap.get("htl_email").toString());
			}
			updateResult = this.daoHelper.update(this.hotelDao, attrMap, keyMap);
			updateResult.setMessage("SUCESSFUL_UPDATE");
		} catch (InvalidEmailException e) {
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			control.setErrorMessage(updateResult, "HOTEL_NAME_OR_EMAIL_ALREADY_EXISTS");
		} catch (RecordNotFoundException e) {
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (EmptyRequestException e) {
			control.setErrorMessage(updateResult, e.getMessage());
		}
		return updateResult;
	}

	private boolean checkIfHotelExists(Map<String, Object> keyMap) {
		if (keyMap.isEmpty()) {
			throw new RecordNotFoundException("ID_HOTEL_REQUIRED");
		}
		List<String> attrList = new ArrayList<>();
		attrList.add("id_hotel");
		EntityResult existingHotel = this.daoHelper.query(hotelDao, keyMap, attrList);
		if (existingHotel.isEmpty())
			throw new RecordNotFoundException("HOTEL_DOESN'T_EXISTS");
		return existingHotel.isEmpty();

	}

	private void checkIfDataIsEmpty(Map<String, Object> attrMap) {
		if (attrMap.get("htl_address") == null && attrMap.get("htl_name") == null && attrMap.get("htl_email") == null
				&& attrMap.get("htl_phone") == null) {
			throw new EmptyRequestException("EMPTY_REQUEST");
		}
	}

}