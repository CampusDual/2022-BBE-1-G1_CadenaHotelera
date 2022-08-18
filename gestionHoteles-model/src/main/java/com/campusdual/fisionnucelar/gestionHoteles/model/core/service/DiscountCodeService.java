package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
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

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IDiscountCodeService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.DiscountCodeDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

@Service("DiscountCodeService")
@Lazy
public class DiscountCodeService implements IDiscountCodeService {

	private Logger log;

	private Control control;
	private Validator validator;

	@Autowired
	private DiscountCodeDao discountCodeDao;

	@Autowired
	private IDiscountCodeService discountCodeService;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	public DiscountCodeService() {
		super();
		this.control = new Control();
		this.validator = new Validator();
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * 
	 * Executes a query over the discount code table
	 * 
	 * @since 10/08/2022
	 * @param The id of the discount code to search (optional) and the columns to
	 *            show
	 * 
	 * @exception NoResultsException     when there are no discount codes
	 * 
	 * @exception BadSqlGrammarException when it receives an incorrect param type
	 * 
	 * @return The columns from the discount code table especified in the params and
	 *         a message with the operation result
	 */
	@Secured({ PermissionsProviderSecured.SECURED })
	@Override
	public EntityResult discountcodeQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = daoHelper.query(discountCodeDao, keyMap, attrList);
			control.checkResults(searchResult);

		} catch (BadSqlGrammarException e) {
			log.error("unable to search a discount code. Request : {} {} ", keyMap, attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		} catch (NoResultsException e) {
			log.error("unable to search a discount code. Request : {} {} ", keyMap, attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		}
		return searchResult;
	}

	/**
	 * 
	 * Adds a new register on the discount_codes table.
	 * 
	 * @since 10/08/2022
	 * @param The name and the multiplier of the discount code
	 * @return The id of the new discount_code and a message with the operation
	 *         result
	 * 
	 * @exception EmptyRequestException           when it doesn't receive none of
	 *                                            the register fields
	 * @exception DataIntegrityViolationException when it doesn't receive any of the
	 *                                            register fields
	 * 
	 * @exception DuplicateKeyException           when it receives a discount_code
	 *                                            with a already registered name
	 * 
	 * @exception BadSqlGrammarException          when it receives an incorrect type
	 * 
	 * @exception InvalidRequestException         when it receives a multiplier
	 *                                            below or equal to 0
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	@Secured({ PermissionsProviderSecured.SECURED })
	@Override
	public EntityResult discountcodeInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			validator.checkIfMapIsEmpty(attrMap);
			checkPositiveMultiplier(attrMap);			
			insertResult = this.daoHelper.insert(discountCodeDao, attrMap);
			insertResult.setMessage("SUCCESSFUL_INSERTION");
		} catch (DuplicateKeyException e) {
			log.error("unable to insert a discount code. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, "DISCOUNT_CODE_NAME_ALREADY_EXISTS");
		} catch (EmptyRequestException | InvalidRequestException e) {
			log.error("unable to insert a discount code. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (BadSqlGrammarException|ClassCastException e) {
			log.error("unable to insert a discount code : {} ", attrMap, e);
			control.setErrorMessage(insertResult, "INCORRECT_TYPE");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert a discount code. Request : {}", attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		}

		return insertResult;
	}



	/**
	 * 
	 * Updates a register on the discount_codes table.
	 * 
	 * @since 10/08/2022
	 * @param The fields to update
	 * @return A message with the result of the update
	 * 
	 * @exception EmptyRequestException   when it doesn't receive none of the
	 *                                    register fields
	 * 
	 * @exception DuplicateKeyException   when it receives a discount_date with a
	 *                                    already registered name
	 * 
	 * 
	 * @exception RecordNotFoundException when it doesn't receive an id_code or
	 *                                    receive an id_code that doesn't exists
	 * 
	 * @exception BadSqlGrammarException  when it receives an incorrect type
	 * 
	 * 
	 * 
	 */
	@Secured({ PermissionsProviderSecured.SECURED })
	@Override
	public EntityResult discountcodeUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			validator.checkIfMapIsEmpty(attrMap);
			checkIfDiscountCodeExists(keyMap);
			checkPositiveMultiplier(attrMap);
			updateResult = this.daoHelper.update(discountCodeDao, attrMap, keyMap);
			updateResult.setMessage("SUCESSFUL_UPDATE");
		} catch (DuplicateKeyException e) {
			log.error("unable to update an discount code. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, "DISCOUNT_CODE_NAME_ALREADY_EXISTS");
		} catch (RecordNotFoundException|InvalidRequestException e) {
			log.error("unable to update an discount code. Request : {} {} ", keyMap, attrMap, e);
			control.setMessageFromException(updateResult, e.getMessage());
		} catch (EmptyRequestException e) {
			log.error("unable to update an discount code. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (BadSqlGrammarException|ClassCastException e) {
			log.error("unable to update an discount code. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, "INCORRECT_REQUEST");
		}catch (DataIntegrityViolationException e) {
			log.error("unable to update a discount code. Request : {}", attrMap, e);
			control.setMessageFromException(updateResult, e.getMessage());
		}
		return updateResult;
	}

	/**
	 * 
	 * Put a leaving_date on a register of the discount code table.
	 * 
	 * @since 10/08/2022
	 * @param The id of the discount_code
	 * @return A message with the result of the operation
	 * 
	 * @exception RecordNotFoundException when it doesn't receive the id_code or
	 *                                    receive an id_code that doesn't exists
	 * 
	 * 
	 */
	@Secured({ PermissionsProviderSecured.SECURED })
	@Override
	public EntityResult discountcodeDelete(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult deleteResult = new EntityResultMapImpl();
		try {
			checkIfDiscountCodeExists(attrMap);
			Map<String, Object> deleteMap = new HashMap<>();
			deleteMap.put("dc_leaving_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));
			deleteResult = daoHelper.update(discountCodeDao, deleteMap, attrMap);
			deleteResult.setMessage("SUCCESSFUL_DELETE");
		} catch (RecordNotFoundException e) {
			log.error("unable to delete an discount code. Request : {} {} ", attrMap, e);
			control.setMessageFromException(deleteResult, e.getMessage());
		}
		return deleteResult;
	}

	/**
	 * Check if it has received an id_code and if it exists in the database
	 * 
	 * @param The param with the id_code
	 */
	private void checkIfDiscountCodeExists(Map<String, Object> attrMap) {
		if (attrMap.get("id_code") == null) {
			throw new RecordNotFoundException("ID_CODE_REQUIRED");
		}
		EntityResult existingCode = discountcodeQuery(attrMap, Arrays.asList("id_code"));
		if (existingCode.isEmpty())
			throw new RecordNotFoundException("CODE_DOESN'T_EXISTS");
	}
	
	/**
	 * It throws an exception if the received multiplier is equal or below zero
	 * 
	 * @param attrMap with a multiplier
	 * @throws InvalidRequestException
	 */
	private void checkPositiveMultiplier(Map<String, Object> attrMap) throws InvalidRequestException {
		if (attrMap.containsKey("dc_multiplier")) {
			Double multiplier = (Double) attrMap.get("dc_multiplier");
			if (multiplier <= 0) {
				throw new InvalidRequestException("DC_MULTIPLIER_MUST_BE_HIGHER_THAN_0");
			}
		}
		
	}
}
